package com.shindaq.NerrikaTG.commands;

import com.shindaq.NerrikaTG.utils.PlayerUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class ListCommand {
    
    private static final Map<String, CachedListResult> listCache = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION = 3000; // 3 секунды кэш для оптимизации
    
    private static class CachedListResult {
        final String result;
        final long timestamp;
        final int playerCount;
        
        CachedListResult(String result, int playerCount) {
            this.result = result;
            this.timestamp = System.currentTimeMillis();
            this.playerCount = playerCount;
        }
        
        boolean isValid(int currentPlayerCount) {
            // Кэш действителен только если количество игроков не изменилось
            return System.currentTimeMillis() - timestamp < CACHE_DURATION && 
                   this.playerCount == currentPlayerCount;
        }
    }
    
    public static String execute(MinecraftServer server) {
        if (server == null) {
            return "❌ Сервер недоступен";
        }

        List<ServerPlayer> players = PlayerUtils.getAllPlayers(server);
        int playerCount = players.size();
        
        // Проверяем кэш
        String cacheKey = "list_" + playerCount;
        CachedListResult cached = listCache.get(cacheKey);
        if (cached != null && cached.isValid(playerCount)) {
            return cached.result;
        }
        
        if (players.isEmpty()) {
            String result = "🟢 Онлайн: 0 из " + PlayerUtils.getMaxPlayers(server) + "\n📃 Игроки: нет игроков";
            listCache.put(cacheKey, new CachedListResult(result, playerCount));
            return result;
        }

        StringBuilder response = new StringBuilder();
        response.append("🟢 Онлайн: ").append(playerCount).append(" из ").append(PlayerUtils.getMaxPlayers(server)).append("\n");
        response.append("📃 Игроки: ");
        
        for (int i = 0; i < players.size(); i++) {
            String playerName = PlayerUtils.getSafePlayerName(players.get(i));
            response.append(playerName);
            if (i < players.size() - 1) {
                response.append(", ");
            }
        }

        String result = response.toString();
        listCache.put(cacheKey, new CachedListResult(result, playerCount));
        return result;
    }
    
    public static void cleanupCache() {
        long currentTime = System.currentTimeMillis();
        listCache.entrySet().removeIf(entry -> 
            currentTime - entry.getValue().timestamp > CACHE_DURATION * 10);
    }
} 