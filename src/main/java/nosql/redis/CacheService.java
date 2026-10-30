package nosql.redis;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import redis.clients.jedis.Jedis;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CacheService {
    private static final Gson gson = new Gson();

    private static final int TTL_HANGHOA = 300;
    private static final int TTL_SEARCH = 300;
    private static final int TTL_DROPDOWN = 600;

    private static final String PREFIX_HANGHOA_ALL = "cache:hanghoa:all";
    private static final String PREFIX_HANGHOA_SEARCH = "cache:hanghoa:search:";
    private static final String PREFIX_DROPDOWN_LOAI = "cache:dropdown:loaihanghoa";
    private static final String PREFIX_DROPDOWN_VITRI = "cache:dropdown:vitrikho";

    public static <T> void set(String key, T value, int ttlSeconds) {
        try (Jedis jedis = RedisConnection.getConnection()) {
            if (jedis != null && value != null) {
                String json = gson.toJson(value);
                jedis.setex(key, ttlSeconds, json);
            }
        } catch (Exception e) {
            System.err.println("Cache set failed for key " + key + ": " + e.getMessage());
        }
    }

    public static <T> T get(String key, Class<T> clazz) {
        try (Jedis jedis = RedisConnection.getConnection()) {
            if (jedis != null) {
                String json = jedis.get(key);
                if (json != null && !json.isEmpty()) {
                    return gson.fromJson(json, clazz);
                }
            }
        } catch (Exception e) {
            System.err.println("Cache get failed for key " + key + ": " + e.getMessage());
        }
        return null;
    }

    public static <T> T get(String key, Type type) {
        try (Jedis jedis = RedisConnection.getConnection()) {
            if (jedis != null) {
                String json = jedis.get(key);
                if (json != null && !json.isEmpty()) {
                    return gson.fromJson(json, type);
                }
            }
        } catch (Exception e) {
            System.err.println("Cache get failed for key " + key + ": " + e.getMessage());
        }
        return null;
    }

    public static void delete(String key) {
        try (Jedis jedis = RedisConnection.getConnection()) {
            if (jedis != null) {
                jedis.del(key);
            }
        } catch (Exception e) {
            System.err.println("Cache delete failed for key " + key + ": " + e.getMessage());
        }
    }

    public static void deleteByPattern(String pattern) {
        try (Jedis jedis = RedisConnection.getConnection()) {
            if (jedis != null) {
                Set<String> keys = jedis.keys(pattern);
                if (keys != null && !keys.isEmpty()) {
                    for (String key : keys) {
                        jedis.del(key);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Cache deleteByPattern failed for pattern " + pattern + ": " + e.getMessage());
        }
    }

    public static <T> void cacheHangHoaAll(List<T> data) {
        set(PREFIX_HANGHOA_ALL, data, TTL_HANGHOA);
    }

    public static <T> List<T> getHangHoaAll(Type type) {
        return get(PREFIX_HANGHOA_ALL, type);
    }

    public static <T> void cacheHangHoaSearch(String keyword, List<T> data) {
        String normalizedKey = keyword.toLowerCase().trim();
        set(PREFIX_HANGHOA_SEARCH + normalizedKey, data, TTL_SEARCH);
    }

    public static <T> List<T> getHangHoaSearch(String keyword, Type type) {
        String normalizedKey = keyword.toLowerCase().trim();
        return get(PREFIX_HANGHOA_SEARCH + normalizedKey, type);
    }

    public static <T> void cacheDropdownLoai(Map<T, T> data) {
        set(PREFIX_DROPDOWN_LOAI, data, TTL_DROPDOWN);
    }

    public static <T> Map<T, T> getDropdownLoai(Type type) {
        return get(PREFIX_DROPDOWN_LOAI, type);
    }

    public static <T> void cacheDropdownViTri(Map<T, T> data) {
        set(PREFIX_DROPDOWN_VITRI, data, TTL_DROPDOWN);
    }

    public static <T> Map<T, T> getDropdownViTri(Type type) {
        return get(PREFIX_DROPDOWN_VITRI, type);
    }

    public static void invalidateHangHoaCache() {
        nosql.elasticsearch.HangHoaSearchService.invalidate();
        delete(PREFIX_HANGHOA_ALL);
        deleteByPattern(PREFIX_HANGHOA_SEARCH + "*");
        System.out.println("HangHoa cache invalidated");
    }

    public static void invalidateDropdownCache() {
        delete(PREFIX_DROPDOWN_LOAI);
        delete(PREFIX_DROPDOWN_VITRI);
        System.out.println("Dropdown cache invalidated");
    }

    public static void invalidateAllCache() {
        invalidateHangHoaCache();
        invalidateDropdownCache();
        System.out.println("All cache invalidated");
    }
}
