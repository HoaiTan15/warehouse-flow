package nosql.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisConnection {
    private static JedisPool jedisPool;

    private static final String REDIS_HOST = System.getenv("REDIS_HOST") != null
        ? System.getenv("REDIS_HOST")
        : "localhost";

    private static final int REDIS_PORT = System.getenv("REDIS_PORT") != null
        ? Integer.parseInt(System.getenv("REDIS_PORT"))
        : 6379;

    private static final String REDIS_PASSWORD = System.getenv("REDIS_PASSWORD");

    private static final int REDIS_TIMEOUT = 2000;
    private static final int MAX_TOTAL = 20;
    private static final int MAX_IDLE = 10;
    private static final int MIN_IDLE = 5;

    public static JedisPool getPool() {
        if (jedisPool == null) {
            synchronized (RedisConnection.class) {
                if (jedisPool == null) {
                    try {
                        JedisPoolConfig poolConfig = new JedisPoolConfig();
                        poolConfig.setMaxTotal(MAX_TOTAL);
                        poolConfig.setMaxIdle(MAX_IDLE);
                        poolConfig.setMinIdle(MIN_IDLE);
                        poolConfig.setTestOnBorrow(true);
                        poolConfig.setTestOnReturn(true);
                        poolConfig.setTestWhileIdle(true);

                        if (REDIS_PASSWORD != null && !REDIS_PASSWORD.isEmpty()) {
                            jedisPool = new JedisPool(poolConfig, REDIS_HOST, REDIS_PORT, REDIS_TIMEOUT, REDIS_PASSWORD);
                        } else {
                            jedisPool = new JedisPool(poolConfig, REDIS_HOST, REDIS_PORT, REDIS_TIMEOUT);
                        }

                        System.out.println("Redis pool created successfully: " + REDIS_HOST + ":" + REDIS_PORT);
                    } catch (Exception e) {
                        System.err.println("Failed to create Redis pool: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
        return jedisPool;
    }

    public static Jedis getConnection() {
        try {
            JedisPool pool = getPool();
            if (pool != null) {
                return pool.getResource();
            }
        } catch (Exception e) {
            System.err.println("Failed to get Redis connection: " + e.getMessage());
        }
        return null;
    }

    public static void close() {
        if (jedisPool != null && !jedisPool.isClosed()) {
            jedisPool.close();
            jedisPool = null;
            System.out.println("Redis pool closed");
        }
    }

    public static boolean isConnected() {
        try (Jedis jedis = getConnection()) {
            if (jedis != null) {
                String pong = jedis.ping();
                return "PONG".equals(pong);
            }
        } catch (Exception e) {
            System.err.println("Redis ping failed: " + e.getMessage());
        }
        return false;
    }
}
