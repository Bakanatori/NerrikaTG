package com.shindaq.NerrikaTG.commands;

import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Реестр команд по образцу tgbridge
 * Обеспечивает четкое разделение обработчиков команд
 */
public class CommandRegistry {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, Function<MinecraftServer, String>> commands = new ConcurrentHashMap<>();
    
    static {
        // Регистрируем команды
        registerCommand("list", ListCommand::execute);
        registerCommand("status", StatusCommand::execute);
    }
    
    /**
     * Регистрирует новую команду
     */
    public static void registerCommand(String commandName, Function<MinecraftServer, String> handler) {
        commands.put("/" + commandName.toLowerCase(), handler);
        LOGGER.info("Зарегистрирована команда: /{}", commandName);
    }
    
    /**
     * Выполняет команду
     */
    public static String executeCommand(String command, MinecraftServer server) {
        String commandName = command.toLowerCase().split("\\s+")[0];
        Function<MinecraftServer, String> handler = commands.get(commandName);
        
        if (handler != null) {
            try {
                return handler.apply(server);
            } catch (Exception e) {
                LOGGER.error("Ошибка при выполнении команды {}: {}", commandName, e.getMessage());
                return "❌ Ошибка при выполнении команды";
            }
        }
        
        return null; // Команда не найдена
    }
    
    /**
     * Проверяет, является ли строка командой
     */
    public static boolean isCommand(String text) {
        if (text == null || !text.startsWith("/")) {
            return false;
        }
        
        String commandName = text.toLowerCase().split("\\s+")[0];
        return commands.containsKey(commandName);
    }
    
    /**
     * Возвращает список доступных команд
     */
    public static String getAvailableCommands() {
        StringBuilder sb = new StringBuilder("📋 Доступные команды:\n");
        commands.keySet().forEach(cmd -> sb.append("• ").append(cmd).append("\n"));
        return sb.toString();
    }
}