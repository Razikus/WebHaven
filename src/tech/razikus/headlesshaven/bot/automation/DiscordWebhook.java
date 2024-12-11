package tech.razikus.headlesshaven.bot.automation;

import org.jetbrains.annotations.NotNull;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiscordWebhook {
    private final String webhookUrl;

    public DiscordWebhook(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public void sendMessage(String message) throws Exception {
        sendPayload(String.format("{\"content\": \"%s\"}", message));
    }

    public void sendEmbed(Embed embed) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        List<Map<String, Object>> embeds = new ArrayList<>();
        embeds.add(embed.toMap());
        payload.put("embeds", embeds);

        String jsonPayload = convertMapToJson(payload);
        sendPayload(jsonPayload);
    }

    private void sendPayload(String jsonPayload) throws Exception {
        URL url = new URL(webhookUrl);
        HttpURLConnection connection = getHttpURLConnection(url, jsonPayload);

        int responseCode = connection.getResponseCode();
        if (responseCode != 204) {
            throw new RuntimeException("Failed to send message. Response code: " + responseCode);
        }

        connection.disconnect();
    }

    @NotNull
    private static HttpURLConnection getHttpURLConnection(URL url, String jsonPayload) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", "Java-DiscordWebhook");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        return connection;
    }

    private String convertMapToJson(Map<String, Object> map) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                json.append(",");
            }
            first = false;

            json.append(String.format("\"%s\":", entry.getKey()));
            Object value = entry.getValue();

            if (value instanceof String) {
                json.append(String.format("\"%s\"", escapeJson((String) value)));
            } else if (value instanceof Number) {
                json.append(value);
            } else if (value instanceof List) {
                json.append(convertListToJson((List<?>) value));
            } else if (value instanceof Map) {
                json.append(convertMapToJson((Map<String, Object>) value));
            }
        }

        json.append("}");
        return json.toString();
    }

    private String convertListToJson(List<?> list) {
        StringBuilder json = new StringBuilder("[");
        boolean first = true;

        for (Object item : list) {
            if (!first) {
                json.append(",");
            }
            first = false;

            if (item instanceof String) {
                json.append(String.format("\"%s\"", escapeJson((String) item)));
            } else if (item instanceof Number) {
                json.append(item);
            } else if (item instanceof Map) {
                json.append(convertMapToJson((Map<String, Object>) item));
            }
        }

        json.append("]");
        return json.toString();
    }

    private String escapeJson(String input) {
        return input.replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public static class Embed {
        private final Map<String, Object> embedData;
        private final List<Map<String, String>> fields;

        public Embed() {
            this.embedData = new HashMap<>();
            this.fields = new ArrayList<>();
            embedData.put("fields", fields);
        }

        public Embed setTitle(String title) {
            embedData.put("title", title);
            return this;
        }

        public Embed setDescription(String description) {
            embedData.put("description", description);
            return this;
        }

        public Embed setColor(int color) {
            embedData.put("color", color);
            return this;
        }

        public Embed addField(String name, String value, boolean inline) {
            Map<String, String> field = new HashMap<>();
            field.put("name", name);
            field.put("value", value);
            field.put("inline", String.valueOf(inline));
            fields.add(field);
            return this;
        }

        public Embed setThumbnail(String url) {
            Map<String, String> thumbnail = new HashMap<>();
            thumbnail.put("url", url);
            embedData.put("thumbnail", thumbnail);
            return this;
        }

        public Embed setImage(String url) {
            Map<String, String> image = new HashMap<>();
            image.put("url", url);
            embedData.put("image", image);
            return this;
        }

        public Embed setFooter(String text, String iconUrl) {
            Map<String, String> footer = new HashMap<>();
            footer.put("text", text);
            if (iconUrl != null) {
                footer.put("icon_url", iconUrl);
            }
            embedData.put("footer", footer);
            return this;
        }

        public Map<String, Object> toMap() {
            return embedData;
        }
    }
}