package nosql.redis;

import model.DailyWarehouseStats;
import model.TransactionFeedItem;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.util.*;

import static org.junit.Assert.*;

public class WarehouseRedisServiceTest {

    /**
     * Fake Jedis in-memory implementation for deterministic TDD unit tests
     */
    static class FakeJedis extends Jedis {
        Map<String, List<String>> lists = new HashMap<>();
        Map<String, Map<String, String>> hashes = new HashMap<>();
        Map<String, Integer> expires = new HashMap<>();

        boolean throwOnCommand = false;
        long lastLtrimStart = -1;
        long lastLtrimStop = -1;

        public FakeJedis() {
            super("localhost"); // Dummy host, socket never opened
        }

        @Override
        public Long lpush(String key, String... strings) {
            if (throwOnCommand) throw new RuntimeException("Simulated Redis failure");
            List<String> list = lists.computeIfAbsent(key, k -> new ArrayList<>());
            for (String s : strings) {
                list.add(0, s); // LPUSH pushes to head
            }
            return (long) list.size();
        }

        @Override
        public String ltrim(String key, long start, long stop) {
            if (throwOnCommand) throw new RuntimeException("Simulated Redis failure");
            this.lastLtrimStart = start;
            this.lastLtrimStop = stop;
            List<String> list = lists.get(key);
            if (list != null && !list.isEmpty()) {
                int fromIndex = (int) Math.max(0, start);
                int toIndex = (int) Math.min(list.size(), stop + 1);
                if (fromIndex < toIndex) {
                    lists.put(key, new ArrayList<>(list.subList(fromIndex, toIndex)));
                } else {
                    list.clear();
                }
            }
            return "OK";
        }

        @Override
        public List<String> lrange(String key, long start, long stop) {
            if (throwOnCommand) throw new RuntimeException("Simulated Redis failure");
            List<String> list = lists.get(key);
            if (list == null || list.isEmpty()) return Collections.emptyList();
            int fromIndex = (int) Math.max(0, start);
            int toIndex = (int) Math.min(list.size(), stop + 1);
            if (fromIndex >= toIndex) return Collections.emptyList();
            return new ArrayList<>(list.subList(fromIndex, toIndex));
        }

        @Override
        public Long hincrBy(String key, String field, long value) {
            if (throwOnCommand) throw new RuntimeException("Simulated Redis failure");
            Map<String, String> map = hashes.computeIfAbsent(key, k -> new HashMap<>());
            long current = 0L;
            try { current = Long.parseLong(map.getOrDefault(field, "0")); } catch (Exception ignored) {}
            long next = current + value;
            map.put(field, String.valueOf(next));
            return next;
        }

        @Override
        public Double hincrByFloat(String key, String field, double value) {
            if (throwOnCommand) throw new RuntimeException("Simulated Redis failure");
            Map<String, String> map = hashes.computeIfAbsent(key, k -> new HashMap<>());
            double current = 0.0;
            try { current = Double.parseDouble(map.getOrDefault(field, "0.0")); } catch (Exception ignored) {}
            double next = current + value;
            map.put(field, String.valueOf(next));
            return next;
        }

        @Override
        public Long expire(String key, int seconds) {
            if (throwOnCommand) throw new RuntimeException("Simulated Redis failure");
            expires.put(key, seconds);
            return 1L;
        }

        @Override
        public Map<String, String> hgetAll(String key) {
            if (throwOnCommand) throw new RuntimeException("Simulated Redis failure");
            Map<String, String> map = hashes.get(key);
            return map != null ? new HashMap<>(map) : Collections.emptyMap();
        }

        @Override
        public void close() {
            // No-op for fake test
        }
    }

    private FakeJedis fakeJedis;
    private WarehouseRedisService service;

    @Before
    public void setUp() {
        fakeJedis = new FakeJedis();
        service = new WarehouseRedisService(() -> fakeJedis);
    }

    // =========================================================================
    // 1. Tests for pushTransaction & Edge cases
    // =========================================================================

    @Test
    public void testPushTransaction_HappyPath() {
        TransactionFeedItem item = new TransactionFeedItem(
                "PN001", "NHAP", "thukho1", 500000.0, 5, "2026-11-09 10:00:00", "Nhập hàng bánh kẹo"
        );

        boolean success = service.pushTransaction(item);
        assertTrue(success);

        // Kiểm tra đã có 1 phần tử trong feed
        List<String> list = fakeJedis.lists.get(WarehouseRedisService.KEY_RECENT_FEED);
        assertNotNull(list);
        assertEquals(1, list.size());
        assertTrue(list.get(0).contains("PN001"));
        assertTrue(list.get(0).contains("500000.0"));

        // Kiểm tra LTRIM được gọi đúng cấu hình MAX_FEED_ITEMS - 1 = 49
        assertEquals(0, fakeJedis.lastLtrimStart);
        assertEquals(WarehouseRedisService.MAX_FEED_ITEMS - 1, fakeJedis.lastLtrimStop);
    }

    @Test
    public void testPushTransaction_NullItem_ReturnsFalse() {
        boolean success = service.pushTransaction(null);
        assertFalse("Null item should return false without throwing", success);
    }

    @Test
    public void testPushTransaction_EmptyOrNullMaPhieu_ReturnsFalse() {
        TransactionFeedItem item = new TransactionFeedItem();
        item.setMaPhieu(null);
        assertFalse(service.pushTransaction(item));

        item.setMaPhieu("   ");
        assertFalse(service.pushTransaction(item));
    }

    @Test
    public void testPushTransaction_RedisProviderReturnsNull_ReturnsFalse() {
        WarehouseRedisService deadService = new WarehouseRedisService(() -> null);
        TransactionFeedItem item = new TransactionFeedItem("PN001", "NHAP", "admin", 100.0, 1, null, null);
        assertFalse(deadService.pushTransaction(item));
    }

    @Test
    public void testPushTransaction_RedisThrowsException_ReturnsFalse() {
        fakeJedis.throwOnCommand = true;
        TransactionFeedItem item = new TransactionFeedItem("PN001", "NHAP", "admin", 100.0, 1, null, null);
        assertFalse("Should catch Redis exception and return false", service.pushTransaction(item));
    }

    @Test
    public void testPushTransaction_AutoGeneratesTimestampIfNull() {
        TransactionFeedItem item = new TransactionFeedItem();
        item.setMaPhieu("PN002");
        item.setLoaiPhieu("NHAP");
        item.setThoiGian(null);

        assertTrue(service.pushTransaction(item));
        assertNotNull(item.getThoiGian());
        assertFalse(item.getThoiGian().trim().isEmpty());
    }

    @Test
    public void testPushTransaction_LTrimPruningKeepsOnlyMaxItems() {
        // Đẩy 55 phần tử vào list
        for (int i = 1; i <= 55; i++) {
            TransactionFeedItem item = new TransactionFeedItem("P" + i, "NHAP", "admin", i * 100.0, 1, null, null);
            service.pushTransaction(item);
        }

        List<String> list = fakeJedis.lists.get(WarehouseRedisService.KEY_RECENT_FEED);
        assertNotNull(list);
        assertEquals("List must be trimmed to exactly MAX_FEED_ITEMS (50)", WarehouseRedisService.MAX_FEED_ITEMS, list.size());

        // Phần tử mới nhất (P55) phải ở đầu danh sách (index 0)
        assertTrue(list.get(0).contains("P55"));
    }

    // =========================================================================
    // 2. Tests for getRecentTransactions & Edge cases
    // =========================================================================

    @Test
    public void testGetRecentTransactions_HappyPath() {
        service.pushTransaction(new TransactionFeedItem("PN01", "NHAP", "thukho", 1000.0, 2, "2026-11-09 10:00:00", "Ghi chú 1"));
        service.pushTransaction(new TransactionFeedItem("PX01", "XUAT", "truongkho", 2000.0, 3, "2026-11-09 10:05:00", "Ghi chú 2"));

        List<TransactionFeedItem> items = service.getRecentTransactions(10);
        assertEquals(2, items.size());
        assertEquals("PX01", items.get(0).getMaPhieu()); // LIFO order
        assertEquals("PN01", items.get(1).getMaPhieu());
    }

    @Test
    public void testGetRecentTransactions_ZeroOrNegativeLimit_ReturnsEmptyList() {
        service.pushTransaction(new TransactionFeedItem("PN01", "NHAP", "thukho", 1000.0, 2, null, null));

        assertTrue(service.getRecentTransactions(0).isEmpty());
        assertTrue(service.getRecentTransactions(-5).isEmpty());
    }

    @Test
    public void testGetRecentTransactions_RedisDown_ReturnsEmptyList() {
        WarehouseRedisService deadService = new WarehouseRedisService(() -> null);
        List<TransactionFeedItem> list = deadService.getRecentTransactions(10);
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    @Test
    public void testGetRecentTransactions_CorruptedJsonEntry_SkipsCorruptedSafely() {
        // Đẩy 1 JSON hợp lệ, 1 JSON hỏng, 1 JSON hợp lệ vào Redis
        fakeJedis.lpush(WarehouseRedisService.KEY_RECENT_FEED,
                "{\"maPhieu\":\"PN01\",\"loaiPhieu\":\"NHAP\"}",
                "{invalid_json_data!!!",
                "{\"maPhieu\":\"PX02\",\"loaiPhieu\":\"XUAT\"}"
        );

        List<TransactionFeedItem> items = service.getRecentTransactions(10);
        // Phải bỏ qua bản ghi hỏng mà không làm sập feed
        assertEquals(2, items.size());
        assertEquals("PX02", items.get(0).getMaPhieu());
        assertEquals("PN01", items.get(1).getMaPhieu());
    }

    // =========================================================================
    // 3. Tests for Daily Stats (Counters & Hashes) & Edge cases
    // =========================================================================

    @Test
    public void testRecordDailyStats_NhapAndXuat() {
        service.recordDailyStats("NHAP", 150000.0);
        service.recordDailyStats("NHAP", 50000.0);
        service.recordDailyStats("XUAT", 80000.0);

        DailyWarehouseStats stats = service.getDailyStats(null); // Hôm nay
        assertNotNull(stats);
        assertEquals(2, stats.getSoPhieuNhap());
        assertEquals(1, stats.getSoPhieuXuat());
        assertEquals(200000.0, stats.getTongTienNhap(), 0.01);
        assertEquals(80000.0, stats.getTongTienXuat(), 0.01);
        assertEquals(3, stats.getTongSoPhieu());
        assertEquals(280000.0, stats.getTongGiaTriGiaoDich(), 0.01);
    }

    @Test
    public void testRecordDailyStats_NullOrEmptyLoai_ReturnsFalse() {
        assertFalse(service.recordDailyStats(null, 1000.0));
        assertFalse(service.recordDailyStats("   ", 1000.0));
    }

    @Test
    public void testRecordDailyStats_NegativeAmount_ClampedToZero() {
        service.recordDailyStats("NHAP", -5000.0);
        DailyWarehouseStats stats = service.getDailyStats(null);
        assertEquals(1, stats.getSoPhieuNhap());
        assertEquals(0.0, stats.getTongTienNhap(), 0.01);
    }

    @Test
    public void testGetDailyStats_KeyNotFound_ReturnsZeroedObject() {
        DailyWarehouseStats stats = service.getDailyStats("2020-01-01");
        assertNotNull(stats);
        assertEquals(0, stats.getSoPhieuNhap());
        assertEquals(0, stats.getSoPhieuXuat());
        assertEquals(0.0, stats.getTongTienNhap(), 0.001);
        assertEquals(0.0, stats.getTongTienXuat(), 0.001);
    }

    @Test
    public void testGetDailyStats_CorruptedValuesInHash_ReturnsSafeZeros() {
        String testKey = WarehouseRedisService.KEY_STATS_PREFIX + "2026-11-09";
        Map<String, String> badMap = new HashMap<>();
        badMap.put("soPhieuNhap", "NOT_A_NUMBER");
        badMap.put("tongTienNhap", "ABC_XYZ");
        fakeJedis.hashes.put(testKey, badMap);

        DailyWarehouseStats stats = service.getDailyStats("2026-11-09");
        assertNotNull(stats);
        assertEquals(0L, stats.getSoPhieuNhap());
        assertEquals(0.0, stats.getTongTienNhap(), 0.001);
    }

    @Test
    public void testGetDailyStats_RedisDown_ReturnsZeroedObjectWithoutCrash() {
        WarehouseRedisService deadService = new WarehouseRedisService(() -> null);
        DailyWarehouseStats stats = deadService.getDailyStats("2026-11-09");
        assertNotNull(stats);
        assertEquals(0L, stats.getSoPhieuNhap());
        assertEquals(0L, stats.getSoPhieuXuat());
    }
}

