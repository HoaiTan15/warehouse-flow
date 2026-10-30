package nosql.elasticsearch;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/** Small Java 8 REST transport; uses the JVM trust store for HTTPS. */
public class ElasticsearchClient {
    private final String url;
    private final int timeout;

    public ElasticsearchClient() {
        this(setting("ELASTICSEARCH_URL", "http://localhost:9200"),
                Integer.parseInt(setting("ELASTICSEARCH_TIMEOUT_MS", "3000")));
    }

    public ElasticsearchClient(String url, int timeout) {
        this.url = url.replaceAll("/+$", "");
        this.timeout = timeout;
    }

    public static String setting(String key, String fallback) {
        String value = System.getProperty(key);
        if (value == null) value = System.getenv(key);
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    public JsonObject request(String method, String path, String body) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url + path).openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(timeout);
        connection.setReadTimeout(timeout);
        connection.setRequestProperty("Accept", "application/json");
        String apiKey = setting("ELASTICSEARCH_API_KEY", "");
        String user = setting("ELASTICSEARCH_USERNAME", "");
        if (!apiKey.isEmpty()) connection.setRequestProperty("Authorization", "ApiKey " + apiKey);
        else if (!user.isEmpty()) connection.setRequestProperty("Authorization", "Basic " +
                Base64.getEncoder().encodeToString((user + ":" + setting("ELASTICSEARCH_PASSWORD", ""))
                        .getBytes(StandardCharsets.UTF_8)));
        try {
            if (body != null) {
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", path.contains("_bulk")
                        ? "application/x-ndjson" : "application/json");
                try (OutputStream out = connection.getOutputStream()) {
                    out.write(body.getBytes(StandardCharsets.UTF_8));
                }
            }
            int status = connection.getResponseCode();
            InputStream stream = status < 300 ? connection.getInputStream() : connection.getErrorStream();
            StringBuilder content = new StringBuilder();
            if (stream != null) try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                char[] buffer = new char[4096];
                int count;
                while ((count = reader.read(buffer)) != -1) content.append(buffer, 0, count);
            }
            if (status < 200 || status >= 300) throw new IOException("Elasticsearch HTTP " + status);
            return content.length() == 0 ? new JsonObject() : new JsonParser().parse(content.toString()).getAsJsonObject();
        } finally {
            connection.disconnect();
        }
    }
}
