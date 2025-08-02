package com.shindaq.NerrikaTG.telegram;

import com.mojang.logging.LogUtils;
import com.shindaq.NerrikaTG.Config;
import com.shindaq.NerrikaTG.utils.JsonUtils;
import com.shindaq.NerrikaTG.utils.NetworkUtils;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Отвечает за отправку сообщений в Telegram
 * Обеспечивает надежную доставку без дублирования
 */
public class MessageSender implements IMessageSender {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot";
    private static final String SEND_MESSAGE_METHOD = "/sendMessage";
    
    private final AtomicLong lastMessageTime = new AtomicLong(0);
    private static final long MIN_MESSAGE_INTERVAL = 50; // 50мс между сообщениями для быстрого ответа
    
    /**
     * Отправляет сообщение в Telegram с защитой от спама
     */
    public CompletableFuture<Boolean> sendMessage(String message) {
        return CompletableFuture.supplyAsync(() -> {
            if (Config.botToken.equals("your bot token") || Config.chatId == 0) {
                LOGGER.warn("Telegram not configured properly. Skipping message: {}", message);
                return false;
            }
            
            // Защита от спама
            long currentTime = System.currentTimeMillis();
            long timeSinceLastMessage = currentTime - lastMessageTime.get();
            if (timeSinceLastMessage < MIN_MESSAGE_INTERVAL) {
                try {
                    Thread.sleep(MIN_MESSAGE_INTERVAL - timeSinceLastMessage);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
            
            lastMessageTime.set(System.currentTimeMillis());
            
            return sendMessageInternal(message);
        });
    }
    
    private boolean sendMessageInternal(String message) {
        try {
            String url = TELEGRAM_API_URL + Config.botToken + SEND_MESSAGE_METHOD;
            String requestBody = buildRequestBody(message);
            
            String response = NetworkUtils.makePostRequest(url, requestBody)
                .orTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .get();
                
            return response != null && response.contains("\"ok\":true");
            
        } catch (Exception e) {
            LOGGER.error("Ошибка при отправке сообщения в Telegram: {}", e.getMessage());
            return false;
        }
    }
    
    private String buildRequestBody(String message) {
        String chatIdStr = String.valueOf(Config.chatId);
        StringBuilder jsonBody = new StringBuilder();
        
        jsonBody.append("{");
        
        // Обработка chat_id
        if (chatIdStr.startsWith("@")) {
            jsonBody.append("\"chat_id\":\"").append(chatIdStr).append("\",");
        } else {
            jsonBody.append("\"chat_id\":").append(chatIdStr).append(",");
        }
        
        // Добавляем текст сообщения
        jsonBody.append("\"text\":\"").append(JsonUtils.escapeJsonString(message)).append("\"");
        
        // Добавляем topic_id если настроено
        if (Config.topicId > 0) {
            jsonBody.append(",\"message_thread_id\":").append(Config.topicId);
        }
        
        jsonBody.append("}");
        
        return jsonBody.toString();
    }
    
    /**
     * Отправляет сообщение синхронно (для случаев, когда нужен быстрый ответ)
     */
    public boolean sendMessageSync(String message) {
        if (Config.botToken.equals("your bot token") || Config.chatId == 0) {
            return false;
        }
        
        return sendMessageInternal(message);
    }
}