package tech.razikus.headlesshaven.bot.automation;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DiscordWebhook {
    private final String webhookUrl;

    public DiscordWebhook(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public void sendMessage(String message) throws Exception {
        // Prepare JSON payload
        String jsonPayload = String.format("{\"content\": \"%s\"}", message);

        // Create connection
        URL url = new URL(webhookUrl);
        HttpURLConnection connection = getHttpURLConnection(url, jsonPayload);

        // Check response
        int responseCode = connection.getResponseCode();
        if (responseCode != 204) {
            throw new RuntimeException("Failed to send message. Response code: " + responseCode);
        }

        connection.disconnect();
    }

    @NotNull
    private static HttpURLConnection getHttpURLConnection(URL url, String jsonPayload) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Set up the request
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", "Java-DiscordWebhook");
        connection.setDoOutput(true);

        // Send the request
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        return connection;
    }

}