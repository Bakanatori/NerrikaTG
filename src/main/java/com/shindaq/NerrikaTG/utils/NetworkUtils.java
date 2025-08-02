package com.shindaq.NerrikaTG.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Утилиты для оптимизации сетевых запросов
 */
public class NetworkUtils {
    
    // Отдельный пул потоков для сетевых операций
    private static final ExecutorService networkExecutor = Executors.newFixedThreadPool(4);
    
    /**
     * Выполняет GET запрос с максимально оптимизированными настройками
     */
    public static CompletableFuture<String> makeGetRequest(String urlString) {
        return CompletableFuture.supplyAsync(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("User-Agent", "NerrikaTG/1.0");
                connection.setConnectTimeout(5000); // Увеличиваем до 5 секунд
                connection.setReadTimeout(10000);   // Увеличиваем до 10 секунд
                connection.setUseCaches(false);
                connection.setDoInput(true);
                connection.setDoOutput(false);
                connection.setInstanceFollowRedirects(true);

                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    try (InputStream is = connection.getInputStream()) {
                        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    }
                } else {
                    throw new IOException("HTTP " + responseCode);
                }
            } catch (IOException e) {
                throw new RuntimeException("Network request failed", e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }, networkExecutor).orTimeout(15, TimeUnit.SECONDS); // Увеличиваем общий timeout до 15 секунд
    }
    
    /**
     * Выполняет POST запрос с максимально оптимизированными настройками
     */
    public static CompletableFuture<String> makePostRequest(String urlString, String jsonBody) {
        return CompletableFuture.supplyAsync(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("User-Agent", "NerrikaTG/1.0");
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setConnectTimeout(5000);  // Увеличиваем до 5 секунд
                connection.setReadTimeout(10000);    // Увеличиваем до 10 секунд
                connection.setUseCaches(false);
                connection.setInstanceFollowRedirects(true);

                // Отправляем данные
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                    os.flush();
                }

                int responseCode = connection.getResponseCode();
                InputStream responseStream = (responseCode >= 200 && responseCode < 300)
                        ? connection.getInputStream()
                        : connection.getErrorStream();

                String response = new String(responseStream.readAllBytes(), StandardCharsets.UTF_8);
                
                if (responseCode != 200) {
                    throw new IOException("HTTP " + responseCode + ": " + response);
                }
                
                return response;
            } catch (IOException e) {
                throw new RuntimeException("Network request failed", e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }, networkExecutor).orTimeout(15, TimeUnit.SECONDS); // Увеличиваем общий timeout до 15 секунд
    }
    
    /**
     * Проверяет доступность URL с быстрым timeout
     */
    public static boolean isUrlAccessible(String urlString) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(2000); // 2 секунды
            connection.setReadTimeout(3000);    // 3 секунды
            
            int responseCode = connection.getResponseCode();
            return responseCode >= 200 && responseCode < 400;
        } catch (IOException e) {
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    
    /**
     * Закрывает пул потоков (вызывать при выключении мода)
     */
    public static void shutdown() {
        networkExecutor.shutdown();
        try {
            if (!networkExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                networkExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            networkExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
} 