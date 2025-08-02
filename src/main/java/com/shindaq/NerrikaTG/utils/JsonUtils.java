package com.shindaq.NerrikaTG.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonUtils {
    
    // Паттерны для более эффективного парсинга
    private static final Pattern STRING_PATTERN = Pattern.compile("\"([^\"]*)\"");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\b(\\d+)\\b");
    
    /**
     * Экранирует строку для использования в JSON
     */
    public static String escapeJsonString(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t")
                   .replace("\b", "\\b")
                   .replace("\f", "\\f");
    }
    
    /**
     * Извлекает значение из JSON строки по ключу (улучшенная версия)
     */
    public static String extractJsonValue(String json, String key) {
        if (json == null || key == null) return null;
        
        // Ищем строковое значение
        String searchPattern = "\"" + key + "\"\\s*:\\s*\"([^\"]*)\"";
        Pattern pattern = Pattern.compile(searchPattern);
        Matcher matcher = pattern.matcher(json);
        
        if (matcher.find()) {
            String value = matcher.group(1);
            // Декодируем Unicode escape sequences
            return decodeUnicode(value);
        }
        
        // Ищем числовое значение
        searchPattern = "\"" + key + "\"\\s*:\\s*(\\d+)";
        pattern = Pattern.compile(searchPattern);
        matcher = pattern.matcher(json);
        
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        // Ищем boolean значение
        searchPattern = "\"" + key + "\"\\s*:\\s*(true|false)";
        pattern = Pattern.compile(searchPattern);
        matcher = pattern.matcher(json);
        
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        return null;
    }
    
    /**
     * Извлекает числовое значение из JSON строки по ключу
     */
    public static Long extractJsonLong(String json, String key) {
        String value = extractJsonValue(json, key);
        if (value == null) return null;
        
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Декодирует Unicode escape sequences
     */
    private static String decodeUnicode(String input) {
        if (input == null) return null;
        
        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < input.length()) {
            char c = input.charAt(i);
            if (c == '\\' && i + 1 < input.length()) {
                char next = input.charAt(i + 1);
                if (next == 'u' && i + 5 < input.length()) {
                    // Unicode escape sequence
                    try {
                        String hex = input.substring(i + 2, i + 6);
                        int unicode = Integer.parseInt(hex, 16);
                        result.append((char) unicode);
                        i += 6;
                        continue;
                    } catch (NumberFormatException e) {
                        // Если не удалось распарсить, оставляем как есть
                    }
                } else if (next == 'n') {
                    result.append('\n');
                    i += 2;
                    continue;
                } else if (next == 'r') {
                    result.append('\r');
                    i += 2;
                    continue;
                } else if (next == 't') {
                    result.append('\t');
                    i += 2;
                    continue;
                } else if (next == 'b') {
                    result.append('\b');
                    i += 2;
                    continue;
                } else if (next == 'f') {
                    result.append('\f');
                    i += 2;
                    continue;
                } else if (next == '"') {
                    result.append('"');
                    i += 2;
                    continue;
                } else if (next == '\\') {
                    result.append('\\');
                    i += 2;
                    continue;
                }
            }
            result.append(c);
            i++;
        }
        return result.toString();
    }
    
    /**
     * Проверяет, содержит ли JSON определенный ключ
     */
    public static boolean containsKey(String json, String key) {
        if (json == null || key == null) return false;
        return json.contains("\"" + key + "\"");
    }
    
    /**
     * Извлекает вложенное значение из JSON
     */
    public static String extractNestedValue(String json, String... keys) {
        if (json == null || keys == null || keys.length == 0) return null;
        
        String currentJson = json;
        for (String key : keys) {
            currentJson = extractJsonValue(currentJson, key);
            if (currentJson == null) return null;
        }
        
        return currentJson;
    }
} 