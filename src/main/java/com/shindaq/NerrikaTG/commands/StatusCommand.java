package com.shindaq.NerrikaTG.commands;

import net.minecraft.server.MinecraftServer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class StatusCommand {
    
    private static final Map<String, CachedStatusResult> statusCache = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION = 5000; // 5 секунд кэш для статуса сервера
    
    private static class CachedStatusResult {
        final String result;
        final long timestamp;
        final boolean serverRunning;
        
        CachedStatusResult(String result, boolean serverRunning) {
            this.result = result;
            this.timestamp = System.currentTimeMillis();
            this.serverRunning = serverRunning;
        }
        
        boolean isValid(boolean currentServerRunning) {
            // Кэш действителен только если статус сервера не изменился
            return System.currentTimeMillis() - timestamp < CACHE_DURATION && 
                   this.serverRunning == currentServerRunning;
        }
    }
    
    public static String execute(MinecraftServer server) {
        if (server == null) {
            return "Статус сервера: 🚫 Выключен";
        }

        boolean isRunning = server.isRunning();
        String cacheKey = "status_" + isRunning;
        
        // Проверяем кэш
        CachedStatusResult cached = statusCache.get(cacheKey);
        if (cached != null && cached.isValid(isRunning)) {
            return cached.result;
        }

        String status = isRunning ? "✅ Работает" : "🚫 Выключен";
        String result = "Статус сервера: " + status;
        
        // Кэшируем результат
        statusCache.put(cacheKey, new CachedStatusResult(result, isRunning));
        return result;
    }
    
    public static void cleanupCache() {
        long currentTime = System.currentTimeMillis();
        statusCache.entrySet().removeIf(entry -> 
            currentTime - entry.getValue().timestamp > CACHE_DURATION * 10);
    }
} 