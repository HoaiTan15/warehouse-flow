package nosql.redis;

import com.google.gson.Gson;
import model.DailyWarehouseStats;
import model.TransactionFeedItem;
import redis.clients.jedis.Jedis;

import java.text.SimpleDateFormat;
import java.util.*;

public class WarehouseRedisService {
    @FunctionalInterface
    public interface JedisProvider {
        Jedis getResource();
    }

    public static final String KEY_RECENT_FEED = "warehouse:feed:recent";
    public static final String KEY_STATS_PREFIX = "warehouse:stats:";
    public static final int MAX_FEED_ITEMS = 50;
    public static final int STATS_TTL_SECONDS = 86400 * 3; // 3 ngày

    private static final Gson gson = new Gson();
    private final JedisProvider provider;

    public WarehouseRedisService() {
        this(RedisConnection::getConnection);
    }

    public WarehouseRedisService(JedisProvider provider) {
        this.provider = provider != null ? provider : RedisConnection::getConnection;
    }

    /**
     * Đẩy một giao dịch xuất/nhập mới vào đầu Redis List và cắt tỉa (LTRIM) tối đa 50 phần tử.
     * Đồng thời cập nhật bộ đếm thống kê trong ngày.
     */
    public boolean pushTransaction(TransactionFeedItem item) {
        if (item == null || item.getMaPhieu() == null || item.getMaPhieu().trim().isEmpty()) {
            return false;
        }

        try (Jedis jedis = provider.getResource()) {
            if (jedis == null) {
                return false;
            }

            if (item.getThoiGian() == null || item.getThoiGian().trim().isEmpty()) {
                item.setThoiGian(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            }

            String json = gson.toJson(item);
            jedis.lpush(KEY_RECENT_FEED, json);
            jedis.ltrim(KEY_RECENT_FEED, 0, MAX_FEED_ITEMS - 1);

            // Cập nhật bộ đếm thống kê ngày
            recordDailyStatsInternal(jedis, item.getLoaiPhieu(), item.getTongTien());

            return true;
        } catch (Exception e) {
            System.err.println("WarehouseRedisService.pushTransaction error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lấy danh sách giao dịch gần nhất từ Redis List.
     * Khả năng chịu lỗi: Bỏ qua các bản ghi JSON bị hỏng nếu có, không bao giờ trả về null.
     */
    public List<TransactionFeedItem> getRecentTransactions(int limit) {
        if (limit <= 0) {
            return Collections.emptyList();
        }

        try (Jedis jedis = provider.getResource()) {
            if (jedis == null) {
                return Collections.emptyList();
            }

            List<String> rawList = jedis.lrange(KEY_RECENT_FEED, 0, limit - 1);
            if (rawList == null || rawList.isEmpty()) {
                return Collections.emptyList();
            }

            List<TransactionFeedItem> result = new ArrayList<>();
            for (String json : rawList) {
                try {
                    TransactionFeedItem item = gson.fromJson(json, TransactionFeedItem.class);
                    if (item != null && item.getMaPhieu() != null) {
                        result.add(item);
                    }
                } catch (Exception parseEx) {
                    System.err.println("Skipping corrupted transaction feed entry: " + parseEx.getMessage());
                }
            }
            return result;
        } catch (Exception e) {
            System.err.println("WarehouseRedisService.getRecentTransactions error: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Ghi nhận thống kê ngày vào Redis Hash.
     */
    public boolean recordDailyStats(String loaiPhieu, double tongTien) {
        if (loaiPhieu == null || loaiPhieu.trim().isEmpty()) {
            return false;
        }

        try (Jedis jedis = provider.getResource()) {
            if (jedis == null) {
                return false;
            }
            return recordDailyStatsInternal(jedis, loaiPhieu, tongTien);
        } catch (Exception e) {
            System.err.println("WarehouseRedisService.recordDailyStats error: " + e.getMessage());
            return false;
        }
    }

    private boolean recordDailyStatsInternal(Jedis jedis, String loaiPhieu, double tongTien) {
        if (jedis == null || loaiPhieu == null) return false;

        String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String key = KEY_STATS_PREFIX + dateStr;
        String normalizedType = loaiPhieu.toUpperCase().trim();
        double validAmount = Math.max(0.0, tongTien);

        if (normalizedType.contains("NHAP")) {
            jedis.hincrBy(key, "soPhieuNhap", 1L);
            jedis.hincrByFloat(key, "tongTienNhap", validAmount);
        } else if (normalizedType.contains("XUAT")) {
            jedis.hincrBy(key, "soPhieuXuat", 1L);
            jedis.hincrByFloat(key, "tongTienXuat", validAmount);
        }

        jedis.expire(key, STATS_TTL_SECONDS);
        return true;
    }

    /**
     * Lấy thống kê nhanh trong ngày từ Redis Hash.
     */
    public DailyWarehouseStats getDailyStats(String dateStr) {
        String targetDate = (dateStr != null && !dateStr.trim().isEmpty())
                ? dateStr.trim()
                : new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        try (Jedis jedis = provider.getResource()) {
            if (jedis == null) {
                return new DailyWarehouseStats(targetDate, 0, 0, 0.0, 0.0);
            }

            String key = KEY_STATS_PREFIX + targetDate;
            Map<String, String> data = jedis.hgetAll(key);
            if (data == null || data.isEmpty()) {
                return new DailyWarehouseStats(targetDate, 0, 0, 0.0, 0.0);
            }

            long soNhap = parseLongSafe(data.get("soPhieuNhap"));
            long soXuat = parseLongSafe(data.get("soPhieuXuat"));
            double tienNhap = parseDoubleSafe(data.get("tongTienNhap"));
            double tienXuat = parseDoubleSafe(data.get("tongTienXuat"));

            return new DailyWarehouseStats(targetDate, soNhap, soXuat, tienNhap, tienXuat);
        } catch (Exception e) {
            System.err.println("WarehouseRedisService.getDailyStats error: " + e.getMessage());
            return new DailyWarehouseStats(targetDate, 0, 0, 0.0, 0.0);
        }
    }

    private static long parseLongSafe(String s) {
        if (s == null) return 0L;
        try {
            return Long.parseLong(s.trim());
        } catch (Exception e) {
            return 0L;
        }
    }

    private static double parseDoubleSafe(String s) {
        if (s == null) return 0.0;
        try {
            return Double.parseDouble(s.trim());
        } catch (Exception e) {
            return 0.0;
        }
    }
}

