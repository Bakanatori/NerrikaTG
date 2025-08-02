package com.shindaq.NerrikaTG.telegram;

import java.util.concurrent.CompletableFuture;

/**
 * Интерфейс для отправки сообщений в Telegram
 */
public interface IMessageSender {
    CompletableFuture<Boolean> sendMessage(String message);
    boolean sendMessageSync(String message);
}