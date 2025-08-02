package com.shindaq.NerrikaTG.commands;

import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class CommandManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, AtomicLong> lastCommandExecution = new ConcurrentHashMap<>();
    private static final Map<String, CachedResult> resultCache = new ConcurrentHashMap<>();
    private static final long COMMAND_COOLDOWN = 200; // Уменьшаем до 200мс
    private static final long CACHE_DURATION = 2000; // 2 секунды кэш
    
    private static class CachedResult {
        final String result;
        final long timestamp;
        
        CachedResult(String result) {
            this.result = result;
            this.timestamp = System.currentTimeMillis();
        }
        
        boolean isValid() {
            return System.currentTimeMillis() - timestamp < CACHE_DURATION;
        }
    }
    
    public static CompletableFuture<String> executeCommand(String command, MinecraftServer server) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Парсим команду
                String[] parts = command.trim().split("\\s+", 2);
                String commandName = parts[0].toLowerCase();
                String arguments = parts.length > 1 ? parts[1] : "";
                
                // Проверяем кэш для команд без аргументов
                if (arguments.isEmpty()) {
                    CachedResult cached = resultCache.get(commandName);
                    if (cached != null && cached.isValid()) {
                        return cached.result;
                    }
                }
                
                // Проверяем cooldown для предотвращения спама
                long currentTime = System.currentTimeMillis();
                AtomicLong lastExecution = lastCommandExecution.computeIfAbsent(commandName, k -> new AtomicLong(0));
                
                long lastExecTime = lastExecution.get();
                if (currentTime - lastExecTime < COMMAND_COOLDOWN) {
                    return "⏳ Подождите немного перед повторным выполнением команды";
                }
                
                // Обновляем время последнего выполнения
                lastExecution.set(currentTime);
                
                // Выполняем команду
                String result = executeCommandInternal(commandName, arguments, server);
                
                if (result != null) {
                    // Кэшируем результат для команд без аргументов
                    if (arguments.isEmpty()) {
                        resultCache.put(commandName, new CachedResult(result));
                    }
                }
                
                return result;
                
            } catch (Exception e) {
                LOGGER.error("Ошибка при выполнении команды: '{}'", command, e);
                return "❌ Ошибка при выполнении команды";
            }
        });
    }
    
    public static String executeCommandSync(String command, MinecraftServer server) {
        try {
            // Парсим команду
            String[] parts = command.trim().split("\\s+", 2);
            String commandName = parts[0].toLowerCase();
            String arguments = parts.length > 1 ? parts[1] : "";
            
            // Проверяем кэш для команд без аргументов
            if (arguments.isEmpty()) {
                CachedResult cached = resultCache.get(commandName);
                if (cached != null && cached.isValid()) {
                    return cached.result;
                }
            }
            
            // Проверяем cooldown для предотвращения спама
            long currentTime = System.currentTimeMillis();
            AtomicLong lastExecution = lastCommandExecution.computeIfAbsent(commandName, k -> new AtomicLong(0));
            
            long lastExecTime = lastExecution.get();
            if (currentTime - lastExecTime < COMMAND_COOLDOWN) {
                return "⏳ Подождите немного перед повторным выполнением команды";
            }
            
            // Обновляем время последнего выполнения
            lastExecution.set(currentTime);
            
            // Выполняем команду
            String result = executeCommandInternal(commandName, arguments, server);
            
            if (result != null) {
                // Кэшируем результат для команд без аргументов
                if (arguments.isEmpty()) {
                    resultCache.put(commandName, new CachedResult(result));
                }
            }
            
            return result;
            
        } catch (Exception e) {
            LOGGER.error("Ошибка при выполнении команды: '{}'", command, e);
            return "❌ Ошибка при выполнении команды";
        }
    }
    
    private static String executeCommandInternal(String commandName, String arguments, MinecraftServer server) {
        switch (commandName) {
            case "/list":
                return ListCommand.execute(server);
            case "/status":
                return StatusCommand.execute(server);
            default:
                return null; // Неизвестная команда
        }
    }
    
    public static void cleanup() {
        long currentTime = System.currentTimeMillis();
        
        // Очищаем старые записи cooldown
        lastCommandExecution.entrySet().removeIf(entry -> 
            currentTime - entry.getValue().get() > COMMAND_COOLDOWN * 10);
        
        // Очищаем устаревший кэш
        resultCache.entrySet().removeIf(entry -> !entry.getValue().isValid());
        
        // Очищаем кэш команд
        ListCommand.cleanupCache();
        StatusCommand.cleanupCache();
    }
} 