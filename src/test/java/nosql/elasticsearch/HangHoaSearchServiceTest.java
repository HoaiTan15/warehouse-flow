package nosql.elasticsearch;

import com.google.gson.*;
import java.io.IOException;
import java.util.*;
import model.HangHoa;
import org.junit.*;
import static org.junit.Assert.*;

public class HangHoaSearchServiceTest {
    static class FakeClient extends ElasticsearchClient {
        boolean failBulk;
        boolean published;
        int searches;
        int pages;
        List<String> paths = new ArrayList<>();
        @Override public JsonObject request(String method, String path, String body) throws IOException {
            paths.add(method + " " + path);
            if (path.endsWith("_bulk")) {
                assertTrue(body.endsWith("\n"));
                assertTrue(body.contains("HH01"));
                return json("{\"errors\":" + failBulk + "}");
            }
            if (path.equals("/_aliases")) published = true;
            if (path.endsWith("_search")) {
                searches++;
                JsonArray hits = new JsonArray();
                int count = pages > 0 && searches == 1 ? 500 : 1;
                for (int i = 0; i < count; i++) hits.add(json("{\"_source\":{\"maHang\":\"HH01\"},\"sort\":[1,\"HH01\"]}"));
                JsonObject response = json("{\"timed_out\":false,\"_shards\":{\"failed\":0},\"hits\":{}}");
                response.getAsJsonObject("hits").add("hits", hits);
                if (searches == 2) assertTrue(json(body).has("search_after"));
                return response;
            }
            return new JsonObject();
        }
    }
    static JsonObject json(String value) { return new JsonParser().parse(value).getAsJsonObject(); }
    static List<HangHoa> data() {
        return Collections.singletonList(new HangHoa("HH01", "Đường trắng", "Kg", 30, "1kg", 10, "L1", "V1", "Thực phẩm", "Kệ A"));
    }

    @Test public void publishesThenReusesSnapshotAndPaginates() {
        FakeClient client = new FakeClient();
        client.pages = 1;
        HangHoaSearchService service = new HangHoaSearchService(client);
        assertEquals(501, service.search("duong", HangHoaSearchServiceTest::data).size());
        assertTrue(client.published);
        assertEquals(1, service.search("HH01", () -> { throw new AssertionError("Unexpected SQL reload"); }).size());
    }

    @Test public void partialBulkDoesNotPublishAndFallsBackWithCooldown() {
        FakeClient client = new FakeClient();
        client.failBulk = true;
        HangHoaSearchService service = new HangHoaSearchService(client);
        assertNull(service.search("duong", HangHoaSearchServiceTest::data));
        assertFalse(client.published);
        assertTrue(client.paths.stream().anyMatch(p -> p.startsWith("DELETE ")));
        int calls = client.paths.size();
        assertNull(service.search("duong", () -> { throw new AssertionError("Cooldown must skip SQL reload"); }));
        assertEquals(calls, client.paths.size());
    }

    @Test public void sqlFailureNeverPublishesEmptyIndex() {
        FakeClient client = new FakeClient();
        assertNull(new HangHoaSearchService(client).search("duong", () -> { throw new IOException("SQL offline"); }));
        assertTrue(client.paths.isEmpty());
    }

    @Test public void realElasticsearchVietnameseAndDeletion() throws Exception {
        Assume.assumeTrue(Boolean.getBoolean("elasticsearch.integration"));
        String alias = "quanlykhohang_test_" + UUID.randomUUID().toString().replace("-", "");
        System.setProperty("ELASTICSEARCH_INDEX", alias);
        ElasticsearchClient client = new ElasticsearchClient();
        try {
            HangHoaSearchService service = new HangHoaSearchService(client);
            assertEquals("HH01", service.search("duong trang", HangHoaSearchServiceTest::data).get(0).getMaHang());
            assertEquals(1, service.search("đường", HangHoaSearchServiceTest::data).size());
            assertEquals(1, service.search("duong tragn", HangHoaSearchServiceTest::data).size());
            assertEquals(1, service.search("HH01", HangHoaSearchServiceTest::data).size());
            assertEquals(1, service.search("ke a", HangHoaSearchServiceTest::data).size());
            assertTrue(service.search("zzzzzzzzzz", HangHoaSearchServiceTest::data).isEmpty());
            service.markDirty();
            List<HangHoa> updated = data();
            updated.get(0).setSoLuongTonKho(99);
            assertEquals(99, service.search("duong", () -> updated).get(0).getSoLuongTonKho());
            service.markDirty();
            assertTrue(service.search("duong", Collections::emptyList).isEmpty());
            // Restart also rebuilds from authoritative SQL, bypassing persisted stale snapshots.
            assertTrue(new HangHoaSearchService(client).search("duong", Collections::emptyList).isEmpty());
        } finally {
            System.clearProperty("ELASTICSEARCH_INDEX");
            JsonObject indices = client.request("GET", "/" + alias + "_v_*/_alias", null);
            for (String index : indices.keySet()) client.request("DELETE", "/" + index, null);
        }
    }
}
