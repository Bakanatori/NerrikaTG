package com.shindaq.NerrikaTG.utils;

import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

public class ChatUtils {
    
    /**
     * Создает компонент сообщения с синим цветом для серверных сообщений
     */
    public static Component createServerMessage(String message) {
        return Component.literal(message).withStyle(ChatFormatting.BLUE);
    }
    
    /**
     * Создает компонент сообщения с указанным цветом
     */
    public static Component createColoredMessage(String message, ChatFormatting color) {
        return Component.literal(message).withStyle(color);
    }
    
    /**
     * Создает компонент сообщения из Telegram с синим ником и белым текстом
     */
    public static Component createTelegramMessage(String username, String message) {
        // Создаем синий компонент для ника
        Component nicknameComponent = Component.literal("[" + username + "]: ")
            .withStyle(ChatFormatting.BLUE);
        
        // Создаем белый компонент для сообщения
        Component messageComponent = Component.literal(message)
            .withStyle(ChatFormatting.WHITE);
        
        // Объединяем компоненты
        return nicknameComponent.copy().append(messageComponent);
    }
    
    /**
     * Создает компонент сообщения от игрока с белым цветом
     */
    public static Component createPlayerMessage(String playerName, String message) {
        return Component.literal("[" + playerName + "]: " + message)
            .withStyle(ChatFormatting.WHITE);
    }
    
    /**
     * Создает компонент системного сообщения с желтым цветом
     */
    public static Component createSystemMessage(String message) {
        return Component.literal(message).withStyle(ChatFormatting.YELLOW);
    }
    
    /**
     * Создает компонент сообщения об ошибке с красным цветом
     */
    public static Component createErrorMessage(String message) {
        return Component.literal(message).withStyle(ChatFormatting.RED);
    }
} 