package com.shindaq.NerrikaTG.utils;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Утилиты для работы с игроками
 */
public class PlayerUtils {
    
    /**
     * Получает список всех игроков на сервере
     */
    public static List<ServerPlayer> getAllPlayers(MinecraftServer server) {
        return server.getPlayerList().getPlayers();
    }
    
    /**
     * Получает количество игроков на сервере
     */
    public static int getPlayerCount(MinecraftServer server) {
        return server.getPlayerList().getPlayers().size();
    }
    
    /**
     * Получает максимальное количество игроков на сервере
     */
    public static int getMaxPlayers(MinecraftServer server) {
        return server.getPlayerList().getMaxPlayers();
    }
    
    /**
     * Находит игрока по имени
     */
    public static ServerPlayer findPlayerByName(MinecraftServer server, String name) {
        return server.getPlayerList().getPlayers().stream()
            .filter(player -> player.getName().getString().equalsIgnoreCase(name))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Получает список имен всех игроков
     */
    public static List<String> getAllPlayerNames(MinecraftServer server) {
        return server.getPlayerList().getPlayers().stream()
            .map(player -> player.getName().getString())
            .collect(Collectors.toList());
    }
    
    /**
     * Фильтрует игроков по имени
     */
    public static List<ServerPlayer> filterPlayersByName(MinecraftServer server, String filter) {
        return server.getPlayerList().getPlayers().stream()
            .filter(player -> player.getName().getString().toLowerCase().contains(filter.toLowerCase()))
            .collect(Collectors.toList());
    }
    
    /**
     * Проверяет, онлайн ли игрок
     */
    public static boolean isPlayerOnline(MinecraftServer server, String playerName) {
        return findPlayerByName(server, playerName) != null;
    }
    
    /**
     * Получает безопасное имя игрока
     */
    public static String getSafePlayerName(ServerPlayer player) {
        try {
            return player.getName().getString();
        } catch (Exception e) {
            return player.getUUID().toString();
        }
    }
} 