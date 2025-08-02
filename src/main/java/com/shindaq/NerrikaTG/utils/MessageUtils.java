package com.shindaq.NerrikaTG.utils;

import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

/**
 * Утилиты для работы с сообщениями
 */
public class MessageUtils {
    
    /**
     * Создает сообщение о подключении игрока
     */
    public static Component createJoinMessage(String playerName) {
        return Component.literal("§a" + playerName + " присоединился к серверу")
            .withStyle(ChatFormatting.GREEN);
    }
    
    /**
     * Создает сообщение об отключении игрока
     */
    public static Component createLeaveMessage(String playerName) {
        return Component.literal("§c" + playerName + " покинул сервер")
            .withStyle(ChatFormatting.RED);
    }
    
    /**
     * Создает сообщение о количестве игроков
     */
    public static Component createPlayerCountMessage(int current, int max) {
        return Component.literal("§bИгроков онлайн: " + current + "/" + max)
            .withStyle(ChatFormatting.AQUA);
    }
    
    /**
     * Создает информационное сообщение
     */
    public static Component createInfoMessage(String message) {
        return Component.literal("§9" + message)
            .withStyle(ChatFormatting.BLUE);
    }
    
    /**
     * Создает предупреждающее сообщение
     */
    public static Component createWarningMessage(String message) {
        return Component.literal("§e" + message)
            .withStyle(ChatFormatting.YELLOW);
    }
    
    /**
     * Создает сообщение об ошибке
     */
    public static Component createErrorMessage(String message) {
        return Component.literal("§c" + message)
            .withStyle(ChatFormatting.RED);
    }
    
    /**
     * Создает сообщение об успехе
     */
    public static Component createSuccessMessage(String message) {
        return Component.literal("§a" + message)
            .withStyle(ChatFormatting.GREEN);
    }
} 