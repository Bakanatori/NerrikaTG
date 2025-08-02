package com.shindaq.NerrikaTG;

import com.mojang.logging.LogUtils;
import com.shindaq.NerrikaTG.telegram.MessageSender;
import com.shindaq.NerrikaTG.telegram.UpdateProcessor;
import com.shindaq.NerrikaTG.utils.JsonUtils;
import com.shindaq.NerrikaTG.utils.NetworkUtils;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.io.IOException;

public class TelegramService {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot";
    private static final String GET_UPDATES_METHOD = "/getUpdates";
    
    private static TelegramService instance;
    private static MinecraftServer minecraftServer;
    private ScheduledExecutorService scheduler;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    
    // Новая архитектура
    private UpdateProcessor updateProcessor;
    private MessageSender messageSender;
    private long botUserId = -1;

    public TelegramService() {
        instance = this;
        this.messageSender = new MessageSender();
        fetchBotUserId();
    }

    private void fetchBotUserId() {
        String url = TELEGRAM_API_URL + Config.botToken + "/getMe";
        NetworkUtils.makeGetRequest(url)
            .thenAccept(response -> {
                Long botId = JsonUtils.extractJsonLong(response, "id");
                if (botId != null) {
                    botUserId = botId;
                    LOGGER.info("Telegram Bot user ID получен: {}", botUserId);
                    // Инициализируем UpdateProcessor только если сервер уже доступен
                    initializeUpdateProcessor();
                }
            })
            .exceptionally(throwable -> {
                LOGGER.error("Ошибка при получении ID бота", throwable);
                return null;
            });
    }
    
    public static TelegramService getInstance() {
        return instance;
    }
    
    public static void setMinecraftServer(MinecraftServer server) {
        minecraftServer = server;
        // Если экземпляр сервиса уже создан, инициализируем UpdateProcessor
        if (instance != null) {
            instance.initializeUpdateProcessor();
        }
    }
    
    private void initializeUpdateProcessor() {
        if (botUserId != -1 && minecraftServer != null && updateProcessor == null) {
            this.updateProcessor = new UpdateProcessor(botUserId, minecraftServer, messageSender);
            LOGGER.info("UpdateProcessor инициализирован с сервером");
        }
    }

    public void startPolling() {
        if (Config.botToken.equals("your bot token") || Config.chatId == 0) {
            LOGGER.warn("Telegram not configured properly. Polling disabled!");
            return;
        }

        if (isRunning.compareAndSet(false, true)) {
            scheduler = Executors.newScheduledThreadPool(2);
            
            // Инициализируем lastUpdateId асинхронно
            CompletableFuture.runAsync(this::initializeLastUpdateId)
                .thenRun(() -> {
                    // Запускаем опрос каждые 1 секунду
                    scheduler.scheduleAtFixedRate(this::pollTelegramMessages, 0, 800, TimeUnit.MILLISECONDS); // Быстрее отклик
                    LOGGER.info("Telegram polling started with 800ms interval");
                });
        }
    }
    
    public void stopPolling() {
        if (isRunning.compareAndSet(true, false)) {
            if (scheduler != null) {
                scheduler.shutdown();
                try {
                    if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                        scheduler.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    scheduler.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
            NetworkUtils.shutdown();
            LOGGER.info("Telegram polling stopped");
        }
    }

    private void pollTelegramMessages() {
        if (!isRunning.get()) {
            return;
        }
        
        if (updateProcessor == null) {
            // Пытаемся инициализировать UpdateProcessor если он еще не создан
            initializeUpdateProcessor();
            if (updateProcessor == null) {
                return;
            }
        }

        try {
            String url = TELEGRAM_API_URL + Config.botToken + GET_UPDATES_METHOD + 
                        "?offset=" + (updateProcessor.getLastUpdateId() + 1) + "&limit=100";
            
            NetworkUtils.makeGetRequest(url)
                .thenAccept(response -> {
                    if (response != null && !response.trim().isEmpty()) {
                        updateProcessor.processUpdates(response);
                    }
                })
                .exceptionally(throwable -> {
                    // Проверяем на timeout ошибки
                    Throwable cause = throwable.getCause();
                    if (cause instanceof java.net.SocketTimeoutException || 
                        cause instanceof java.util.concurrent.TimeoutException) {
                        // Игнорируем timeout ошибки
                        return null;
                    }
                    
                    // Проверяем, является ли это ошибкой 409 (Conflict)
                    if (throwable.getCause() instanceof RuntimeException) {
                        RuntimeException runtimeEx = (RuntimeException) throwable.getCause();
                        if (runtimeEx.getCause() instanceof IOException) {
                            IOException ioEx = (IOException) runtimeEx.getCause();
                            if (ioEx.getMessage() != null && ioEx.getMessage().contains("HTTP 409")) {
                                return null;
                            }
                        }
                    }
                    
                    // Логируем только серьезные ошибки
                    if (cause instanceof IOException) {
                        LOGGER.warn("Временная проблема с сетью при опросе Telegram API: {}", cause.getMessage());
                    } else {
                        LOGGER.error("Ошибка при опросе Telegram API", throwable);
                    }
                    return null;
                });
        } catch (Exception e) {
            LOGGER.error("Исключение при опросе Telegram API", e);
        }
    }

    private void initializeLastUpdateId() {
        try {
            String url = TELEGRAM_API_URL + Config.botToken + GET_UPDATES_METHOD + "?limit=1&offset=0";

            NetworkUtils.makeGetRequest(url)
                .thenAccept(response -> {
                    if (response != null && response.contains("\"ok\":true")) {
                        long maxId = 0;
                        String[] parts = response.split("\"update_id\":");
                        for (int i = 1; i < parts.length; i++) {
                            int end = parts[i].indexOf(",");
                            if (end != -1) {
                                try {
                                    long id = Long.parseLong(parts[i].substring(0, end).trim());
                                    if (id > maxId) maxId = id;
                                } catch (NumberFormatException e) {
                                    // игнорируем ошибки парсинга
                                }
                            }
                        }
                        if (updateProcessor != null) {
                            updateProcessor.setLastUpdateId(maxId);
                        }
                        LOGGER.info("Инициализирован lastUpdateId = {}", maxId);
                    }
                })
                .exceptionally(throwable -> {
                    LOGGER.error("Error initializing lastUpdateId", throwable);
                    return null;
                });
        } catch (Exception e) {
            LOGGER.error("Error initializing lastUpdateId", e);
        }
    }





    public void sendMessage(String message) {
        if (messageSender != null) {
            messageSender.sendMessage(message);
        }
    }

    /**
     * Отправляет сообщение из Minecraft в Telegram
     */
    public void sendMinecraftMessage(String playerName, String message) {
        if (Config.botToken.equals("your bot token") || Config.chatId == 0) {
            LOGGER.warn("Telegram not configured properly. Skipping message: {}", message);
            return;
        }

        String url = TELEGRAM_API_URL + Config.botToken + "/sendMessage";
        String chatIdStr = String.valueOf(Config.chatId);
        
        // Форматирование сообщения: жирный ник + обычный текст
        String formattedMessage = "*<" + playerName + ">:* " + message;
        
        StringBuilder jsonBody = new StringBuilder();
        jsonBody.append("{");

        // Если chatId строка с @, тогда в JSON - строка, иначе число
        if (chatIdStr.startsWith("@")) {
            jsonBody.append("\"chat_id\":\"").append(chatIdStr).append("\",");
        } else {
            jsonBody.append("\"chat_id\":").append(chatIdStr).append(",");
        }

        jsonBody.append("\"text\":\"").append(JsonUtils.escapeJsonString(formattedMessage)).append("\",");
        jsonBody.append("\"parse_mode\":\"Markdown\"");

        if (Config.topicId > 0) {
            jsonBody.append(",\"message_thread_id\":").append(Config.topicId);
        }

        jsonBody.append("}");

        String requestBody = jsonBody.toString();

        NetworkUtils.makePostRequest(url, requestBody)
            .thenAccept(response -> {})
            .exceptionally(throwable -> {
                LOGGER.error("Error sending Minecraft message to Telegram", throwable);
                return null;
            });
    }

    // Методы для уведомлений о статусе сервера
    public void sendServerStartedNotification() {
        sendMessage("🚀 Сервер запущен");
    }

    public void sendServerStoppedNotification() {
        sendMessage("🚨 Сервер выключен");
    }
} 