package com.shindaq.NerrikaTG.telegram;

import com.mojang.logging.LogUtils;
import com.shindaq.NerrikaTG.commands.CommandRegistry;
import com.shindaq.NerrikaTG.utils.ChatUtils;
import com.shindaq.NerrikaTG.utils.JsonUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Обработчик Telegram updates по образцу tgbridge
 * Предотвращает дублирование и обеспечивает корректную обработку
 */
public class UpdateProcessor {
    private static final Logger LOGGER = LogUtils.getLogger();
    
    private final AtomicLong lastUpdateId = new AtomicLong(0);
    private final Map<Long, Long> processedUpdates = new ConcurrentHashMap<>();
    private final long botUserId;
    private final MinecraftServer server;
    private final IMessageSender messageSender;
    
    // Оптимизация: более частая очистка для экономии памяти
    private static final long CLEANUP_INTERVAL = 180000; // 3 минуты
    private static final long UPDATE_EXPIRY = 300000; // 5 минут
    private long lastCleanup = System.currentTimeMillis();
    
    public UpdateProcessor(long botUserId, MinecraftServer server, IMessageSender messageSender) {
        this.botUserId = botUserId;
        this.server = server;
        this.messageSender = messageSender;
    }
    
    /**
     * Обрабатывает массив updates из Telegram API
     */
    public void processUpdates(String response) {
        if (!response.contains("\"ok\":true")) {
            return;
        }
        
        // Периодическая очистка
        cleanupOldUpdatesIfNeeded();
        
        String[] updateParts = response.split("\"update_id\":");
        long maxUpdateId = lastUpdateId.get();
        
        for (int i = 1; i < updateParts.length; i++) {
            try {
                long updateId = extractUpdateId(updateParts[i]);
                if (updateId <= lastUpdateId.get()) {
                    continue; // Уже обработанный update
                }
                
                if (updateId > maxUpdateId) {
                    maxUpdateId = updateId;
                }
                
                // Проверяем, не обрабатывали ли мы уже этот update
                if (processedUpdates.containsKey(updateId)) {
                    continue;
                }
                
                processedUpdates.put(updateId, System.currentTimeMillis());
                processSingleUpdate(updateParts[i], updateId);
                
            } catch (Exception e) {
                LOGGER.error("Ошибка при обработке update: {}", e.getMessage());
            }
        }
        
        // Обновляем lastUpdateId
        if (maxUpdateId > lastUpdateId.get()) {
            lastUpdateId.set(maxUpdateId);
        }
    }
    
    private void processSingleUpdate(String updateJson, long updateId) {
        // Ищем сообщение в update
        if (!updateJson.contains("\"message\":")) {
            return;
        }
        
        String messageJson = extractMessageJson(updateJson);
        if (messageJson == null) {
            return;
        }
        
        // Извлекаем данные сообщения
        String messageText = JsonUtils.extractJsonValue(messageJson, "text");
        if (messageText == null || messageText.trim().isEmpty()) {
            return;
        }
        
        // Проверяем отправителя
        Long fromId = extractFromId(messageJson);
        if (fromId == null || fromId == botUserId) {
            return; // Игнорируем сообщения от бота
        }
        
        // Проверяем, не бот ли отправитель
        if (isBotSender(messageJson)) {
            return;
        }
        
        // Обрабатываем сообщение
        if (CommandRegistry.isCommand(messageText)) {
            handleCommand(messageText, updateId);
        } else {
            // Передаем также messageJson для извлечения username
            handleRegularMessage(messageText, fromId, messageJson);
        }
    }
    
    private void handleCommand(String command, long updateId) {
        try {
            String result = CommandRegistry.executeCommand(command, server);
            if (result != null) {
                messageSender.sendMessage(result);
            }
        } catch (Exception e) {
            LOGGER.error("Ошибка при обработке команды {}: {}", command, e.getMessage());
            messageSender.sendMessage("❌ Произошла ошибка при выполнении команды");
        }
    }
    
    private void handleRegularMessage(String message, Long fromId, String messageJson) {
        // Отправляем обычные сообщения в Minecraft чат
        if (server != null) {
            // Получаем имя пользователя из JSON
            String username = extractUsername(messageJson);
            if (username == null) {
                username = "TG_" + fromId; // Fallback имя
            }
            
            // Делаем username final для использования в lambda
            final String finalUsername = username;
            
            server.execute(() -> {
                Component component = ChatUtils.createTelegramMessage(finalUsername, message);
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    player.sendSystemMessage(component);
                }
            });
        }
    }
    
    private long extractUpdateId(String updatePart) {
        int commaIndex = updatePart.indexOf(",");
        if (commaIndex == -1) {
            commaIndex = updatePart.indexOf("}");
        }
        if (commaIndex != -1) {
            return Long.parseLong(updatePart.substring(0, commaIndex).trim());
        }
        throw new NumberFormatException("Cannot extract update_id");
    }
    
    private String extractMessageJson(String updateJson) {
        int messageStart = updateJson.indexOf("\"message\":");
        if (messageStart == -1) {
            return null;
        }
        
        // Находим конец объекта message
        int bracketCount = 0;
        int messageContentStart = updateJson.indexOf("{", messageStart);
        if (messageContentStart == -1) {
            return null;
        }
        
        for (int i = messageContentStart; i < updateJson.length(); i++) {
            char c = updateJson.charAt(i);
            if (c == '{') {
                bracketCount++;
            } else if (c == '}') {
                bracketCount--;
                if (bracketCount == 0) {
                    return updateJson.substring(messageContentStart, i + 1);
                }
            }
        }
        
        return null;
    }
    
    private Long extractFromId(String messageJson) {
        if (!messageJson.contains("\"from\":")) {
            return null;
        }
        
        String fromPart = messageJson.substring(messageJson.indexOf("\"from\":"));
        return JsonUtils.extractJsonLong(fromPart, "id");
    }
    
    private boolean isBotSender(String messageJson) {
        if (!messageJson.contains("\"from\":")) {
            return false;
        }
        
        String fromPart = messageJson.substring(messageJson.indexOf("\"from\":"));
        String isBot = JsonUtils.extractJsonValue(fromPart, "is_bot");
        return "true".equals(isBot);
    }
    
    private String extractUsername(String messageJson) {
        if (!messageJson.contains("\"from\":")) {
            return null;
        }
        
        String fromPart = messageJson.substring(messageJson.indexOf("\"from\":"));
        
        // Сначала пытаемся получить username
        String username = JsonUtils.extractJsonValue(fromPart, "username");
        if (username != null && !username.isEmpty()) {
            return username;
        }
        
        // Если username нет, используем first_name
        String firstName = JsonUtils.extractJsonValue(fromPart, "first_name");
        if (firstName != null && !firstName.isEmpty()) {
            return firstName;
        }
        
        // Если и first_name нет, используем last_name
        String lastName = JsonUtils.extractJsonValue(fromPart, "last_name");
        if (lastName != null && !lastName.isEmpty()) {
            return lastName;
        }
        
        return null;
    }
    
    private void cleanupOldUpdatesIfNeeded() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCleanup > CLEANUP_INTERVAL) {
            processedUpdates.entrySet().removeIf(entry -> 
                currentTime - entry.getValue() > UPDATE_EXPIRY);
            lastCleanup = currentTime;
        }
    }
    
    public void setLastUpdateId(long updateId) {
        this.lastUpdateId.set(updateId);
    }
    
    public long getLastUpdateId() {
        return lastUpdateId.get();
    }
}