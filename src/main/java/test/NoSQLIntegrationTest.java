package test;

import dao.DBConnect;
import nosql.mongo.MongoDBConnection;
import nosql.mongo.HoSoHangHoaRepository;
import nosql.mongo.NhatKyThaoTacRepository;
import nosql.redis.RedisConnection;
import redis.clients.jedis.Jedis;

import java.sql.Connection;
import java.util.*;

/**
 * Manual test class để kiểm tra tất cả kết nối NoSQL
 * Chạy class này để verify:
 * - SQL Server connection
 * - MongoDB connection + CRUD
 * - Redis connection + cache operations
 */
public class NoSQLIntegrationTest {

    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("  KIEM TRA TICH HOP NoSQL");
        System.out.println("===========================================\n");

        boolean allPassed = true;

        // Test 1: SQL Server
        allPassed &= testSQLServer();

        // Test 2: MongoDB
        allPassed &= testMongoDB();

        // Test 3: Redis
        allPassed &= testRedis();

        System.out.println("\n===========================================");
        if (allPassed) {
            System.out.println("  KET QUA: TAT CA TESTS THANH CONG ✓");
        } else {
            System.out.println("  KET QUA: MOT SO TESTS THAT BAI ✗");
        }
        System.out.println("===========================================");
    }

    private static boolean testSQLServer() {
        System.out.println("[TEST 1] SQL Server Connection");
        System.out.println("------------------------------------");

        try (Connection conn = DBConnect.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("✓ Ket noi SQL Server thanh cong");
                System.out.println("  Database: QuanLyKhoHang");
                System.out.println("  Status: CONNECTED\n");
                return true;
            } else {
                System.out.println("✗ Ket noi SQL Server that bai\n");
                return false;
            }
        } catch (Exception e) {
            System.out.println("✗ Loi ket noi SQL Server: " + e.getMessage());
            System.out.println("  Chi tiet: Kiem tra DBConnect.java va SQL Server service\n");
            return false;
        }
    }

    private static boolean testMongoDB() {
        System.out.println("[TEST 2] MongoDB Connection & CRUD");
        System.out.println("------------------------------------");

        try {
            // Test connection
            if (!MongoDBConnection.isConnected()) {
                System.out.println("✗ Khong the ket noi MongoDB");
                System.out.println("  Kiem tra: docker ps | grep mongodb\n");
                return false;
            }
            System.out.println("✓ Ket noi MongoDB thanh cong");

            // Test HoSoHangHoaRepository
            HoSoHangHoaRepository hoSoRepo = new HoSoHangHoaRepository();

            String testMaHang = "TEST_" + System.currentTimeMillis();
            Map<String, Object> thuocTinh = new HashMap<>();
            thuocTinh.put("testField", "testValue");
            List<String> tags = Arrays.asList("test", "demo");

            // Test CREATE
            boolean created = hoSoRepo.themHoSo(
                testMaHang,
                "Test mo ta chi tiet",
                thuocTinh,
                tags,
                "Test ghi chu"
            );

            if (created) {
                System.out.println("✓ Them ho so thanh cong (CREATE)");
            } else {
                System.out.println("✗ Them ho so that bai");
                return false;
            }

            // Test READ
            org.bson.Document doc = hoSoRepo.layHoSoTheoMaHang(testMaHang);
            if (doc != null) {
                System.out.println("✓ Lay ho so thanh cong (READ)");
            } else {
                System.out.println("✗ Lay ho so that bai");
                return false;
            }

            // Test UPDATE
            boolean updated = hoSoRepo.capNhatHoSo(
                testMaHang,
                "Test mo ta da cap nhat",
                thuocTinh,
                tags,
                "Test ghi chu da cap nhat"
            );

            if (updated) {
                System.out.println("✓ Cap nhat ho so thanh cong (UPDATE)");
            } else {
                System.out.println("✗ Cap nhat ho so that bai");
                return false;
            }

            // Test DELETE
            boolean deleted = hoSoRepo.xoaHoSo(testMaHang);
            if (deleted) {
                System.out.println("✓ Xoa ho so thanh cong (DELETE)");
            } else {
                System.out.println("✗ Xoa ho so that bai");
                return false;
            }

            // Test NhatKyThaoTacRepository
            NhatKyThaoTacRepository nhatKyRepo = new NhatKyThaoTacRepository();
            boolean logged = nhatKyRepo.ghiNhatKy(
                "TEST_ACTION",
                "TEST_OBJ_001",
                "test_user",
                "THANH_CONG",
                "Day la test log tu NoSQLIntegrationTest"
            );

            if (logged) {
                System.out.println("✓ Ghi nhat ky thanh cong");
            } else {
                System.out.println("✗ Ghi nhat ky that bai");
                return false;
            }

            System.out.println("  Database: quanlykhohang_nosql");
            System.out.println("  Collections: ho_so_hang_hoa, nhat_ky_thao_tac_kho");
            System.out.println("  Status: ALL CRUD OPERATIONS OK\n");

            return true;

        } catch (Exception e) {
            System.out.println("✗ Loi MongoDB: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static boolean testRedis() {
        System.out.println("[TEST 3] Redis Connection & Cache");
        System.out.println("------------------------------------");

        try {
            // Test connection
            if (!RedisConnection.isConnected()) {
                System.out.println("✗ Khong the ket noi Redis");
                System.out.println("  Kiem tra: docker ps | grep redis\n");
                return false;
            }
            System.out.println("✓ Ket noi Redis thanh cong");

            // Test SET operation
            try (Jedis jedis = RedisConnection.getConnection()) {
                if (jedis == null) {
                    System.out.println("✗ Khong lay duoc Jedis connection");
                    return false;
                }

                String testKey = "test:integration:" + System.currentTimeMillis();
                String testValue = "Test value from NoSQLIntegrationTest";

                // Test SET
                jedis.setex(testKey, 60, testValue);
                System.out.println("✓ Set cache thanh cong (SET)");

                // Test GET
                String retrieved = jedis.get(testKey);
                if (testValue.equals(retrieved)) {
                    System.out.println("✓ Get cache thanh cong (GET)");
                } else {
                    System.out.println("✗ Get cache that bai");
                    return false;
                }

                // Test TTL
                Long ttl = jedis.ttl(testKey);
                if (ttl != null && ttl > 0) {
                    System.out.println("✓ TTL hoat dong (TTL: " + ttl + "s)");
                } else {
                    System.out.println("✗ TTL khong hoat dong");
                    return false;
                }

                // Test DELETE
                jedis.del(testKey);
                String afterDelete = jedis.get(testKey);
                if (afterDelete == null) {
                    System.out.println("✓ Xoa cache thanh cong (DELETE)");
                } else {
                    System.out.println("✗ Xoa cache that bai");
                    return false;
                }

                System.out.println("  Host: localhost:6379");
                System.out.println("  Connection Pool: Active");
                System.out.println("  Status: ALL CACHE OPERATIONS OK\n");

                return true;
            }

        } catch (Exception e) {
            System.out.println("✗ Loi Redis: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
