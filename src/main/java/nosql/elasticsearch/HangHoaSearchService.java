package nosql.elasticsearch;

import com.google.gson.*;
import model.HangHoa;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/** SQL remains authoritative. Publish complete snapshots only after every bulk item succeeds. */
public class HangHoaSearchService {
    public interface Source { List<HangHoa> load() throws Exception; }
    private static final HangHoaSearchService INSTANCE = new HangHoaSearchService(new ElasticsearchClient());
    private final ElasticsearchClient client;
    private final Gson gson = new Gson();
    private final AtomicLong generation = new AtomicLong();
    private long indexedGeneration = -1;
    private long indexedAt;
    private long retryAt;
    private String currentIndex;
    private final String alias = ElasticsearchClient.setting("ELASTICSEARCH_INDEX", "quanlykhohang_hanghoa");

    public HangHoaSearchService(ElasticsearchClient client) {
        this.client = client;
        if (!alias.matches("[a-z][a-z0-9_-]{0,100}")) throw new IllegalArgumentException("Invalid ELASTICSEARCH_INDEX");
    }

    public static HangHoaSearchService getInstance() { return INSTANCE; }
    public static void invalidate() { INSTANCE.markDirty(); }
    public void markDirty() { generation.incrementAndGet(); }

    /** Null means unavailable; an empty list means a successful search with no hits. */
    public synchronized List<HangHoa> search(String keyword, Source source) {
        if (!Boolean.parseBoolean(ElasticsearchClient.setting("ELASTICSEARCH_ENABLED", "true"))
                || System.currentTimeMillis() < retryAt) return null;
        try {
            long version = generation.get();
            if (indexedGeneration != version || System.currentTimeMillis() - indexedAt >= 60000) {
                rebuild(source.load());
                indexedGeneration = version;
                indexedAt = System.currentTimeMillis();
            }
            if (version != generation.get()) return null;
            List<HangHoa> results = new ArrayList<>();
            JsonObject query = query(keyword);
            while (true) {
                JsonObject response = client.request("POST", "/" + currentIndex + "/_search", gson.toJson(query));
                if (response.get("timed_out").getAsBoolean()
                        || response.getAsJsonObject("_shards").get("failed").getAsInt() > 0)
                    throw new IOException("Incomplete Elasticsearch search");
                JsonArray hits = response.getAsJsonObject("hits").getAsJsonArray("hits");
                for (JsonElement hit : hits) results.add(gson.fromJson(hit.getAsJsonObject().get("_source"), HangHoa.class));
                if (hits.size() < 500) break;
                query.add("search_after", hits.get(hits.size() - 1).getAsJsonObject().get("sort"));
            }
            return version == generation.get() ? results : null;
        } catch (Exception e) {
            indexedGeneration = -1;
            retryAt = System.currentTimeMillis() + 15000;
            System.err.println("Elasticsearch unavailable; using SQL search: " + e.getMessage());
            return null;
        }
    }

    private void rebuild(List<HangHoa> data) throws Exception {
        JsonObject previous = client.request("GET", "/" + alias + "_v_*/_alias?allow_no_indices=true&ignore_unavailable=true", null);
        String next = alias + "_v_" + UUID.randomUUID().toString().replace("-", "");
        boolean published = false;
        try {
            String mapping;
            try (InputStream in = getClass().getResourceAsStream("/elasticsearch/hanghoa-index.json")) {
                if (in == null) throw new IOException("Missing Elasticsearch mapping");
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] bytes = new byte[4096];
                int count;
                while ((count = in.read(bytes)) != -1) out.write(bytes, 0, count);
                mapping = new String(out.toByteArray(), StandardCharsets.UTF_8);
            }
            client.request("PUT", "/" + next, mapping);
            for (int start = 0; start < data.size(); start += 250) {
                StringBuilder bulk = new StringBuilder();
                for (HangHoa item : data.subList(start, Math.min(start + 250, data.size()))) {
                    if (item.getMaHang() == null) throw new IOException("Missing product ID");
                    JsonObject id = new JsonObject();
                    id.addProperty("_id", item.getMaHang());
                    JsonObject action = new JsonObject();
                    action.add("index", id);
                    bulk.append(gson.toJson(action)).append('\n').append(gson.toJson(item)).append('\n');
                }
                JsonObject result = client.request("POST", "/" + next + "/_bulk", bulk.toString());
                if (result.get("errors").getAsBoolean()) throw new IOException("Elasticsearch bulk item failed");
            }
            client.request("POST", "/" + next + "/_refresh", null);
            // Remove only this alias; old physical indices remain intact until publication succeeds.
            JsonObject actions = new JsonParser().parse("{\"actions\":[{\"remove\":{\"index\":\""
                    + alias + "_v_*\",\"alias\":\"" + alias + "\",\"must_exist\":false}},"
                    + "{\"add\":{\"index\":\"" + next + "\",\"alias\":\"" + alias + "\"}}]}").getAsJsonObject();
            client.request("POST", "/_aliases", gson.toJson(actions));
            published = true;
            currentIndex = next;
            for (String old : previous.keySet()) {
                if (old.startsWith(alias + "_v_") && !old.equals(next)) cleanup(old);
            }
            System.out.println("Elasticsearch indexed " + data.size() + " products into " + alias);
        } finally {
            if (!published) cleanup(next);
        }
    }

    private void cleanup(String index) {
        try { client.request("DELETE", "/" + index, null); }
        catch (Exception e) { System.err.println("Could not clean Elasticsearch index " + index); }
    }

    static JsonObject query(String keyword) {
        JsonObject multi = new JsonObject();
        multi.addProperty("query", keyword);
        multi.add("fields", new Gson().toJsonTree(Arrays.asList("maHang.text^5", "tenHang^4", "tenLoai^2", "tenViTri", "quyCach")));
        multi.addProperty("fuzziness", "AUTO");
        multi.addProperty("operator", "and");
        JsonObject match = new JsonObject();
        match.add("multi_match", multi);
        JsonObject query = new JsonObject();
        query.add("query", match);
        query.addProperty("size", 500);
        query.add("sort", new JsonParser().parse("[{\"_score\":\"desc\"},{\"maHang\":\"asc\"}]"));
        return query;
    }
}
