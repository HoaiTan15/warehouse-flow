package seed;

import model.TransactionFeedItem;
import nosql.neo4j.Neo4jConnection;
import nosql.redis.RedisConnection;
import nosql.redis.WarehouseRedisService;
import org.neo4j.driver.Session;
import redis.clients.jedis.Jedis;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DatabaseSampleSeeder {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   BAT DAU SEED DU LIEU MAU: NEO4J & REDIS       ");
        System.out.println("=================================================");

        seedNeo4j();
        seedRedis();

        System.out.println("=================================================");
        System.out.println("   SEED DU LIEU HOAN TAT THANH CONG 100%!        ");
        System.out.println("=================================================");
    }

    public static void seedNeo4j() {
        System.out.println("\n[1/2] Dang seed du lieu vao Neo4j Graph Database...");
        try (Session session = Neo4jConnection.getSession()) {
            if (session == null) {
                System.err.println("❌ Khong the ket noi Neo4j. Vui long kiem tra Neo4j server!");
                return;
            }

            // 1. Tạo ràng buộc Constraints
            session.run("CREATE CONSTRAINT c_hanghoa IF NOT EXISTS FOR (h:HangHoa) REQUIRE h.maHang IS UNIQUE;");
            session.run("CREATE CONSTRAINT c_ncc IF NOT EXISTS FOR (ncc:NhaCungCap) REQUIRE ncc.maNCC IS UNIQUE;");
            session.run("CREATE CONSTRAINT c_vitri IF NOT EXISTS FOR (vt:ViTriKho) REQUIRE vt.maViTri IS UNIQUE;");
            session.run("CREATE CONSTRAINT c_bophan IF NOT EXISTS FOR (bp:BoPhan) REQUIRE bp.tenBoPhan IS UNIQUE;");
            session.run("CREATE CONSTRAINT c_phieuxuat IF NOT EXISTS FOR (px:PhieuXuat) REQUIRE px.maPhieu IS UNIQUE;");

            // 2. Tạo Nhà Cung Cấp
            session.run(
                    "MERGE (ncc1:NhaCungCap {maNCC: 'NCC01'}) " +
                    "SET ncc1.tenNCC = 'Unilever Việt Nam', ncc1.diaChi = 'Quận 7, TP.HCM', ncc1.sdt = '02838123456', ncc1.email = 'contact@unilever.com';");
            session.run(
                    "MERGE (ncc2:NhaCungCap {maNCC: 'NCC02'}) " +
                    "SET ncc2.tenNCC = 'Masan Consumer', ncc2.diaChi = 'Quận 1, TP.HCM', ncc2.sdt = '02838123457', ncc2.email = 'sales@masan.com';");
            session.run(
                    "MERGE (ncc3:NhaCungCap {maNCC: 'NCC03'}) " +
                    "SET ncc3.tenNCC = 'Abbott Việt Nam', ncc3.diaChi = 'Quận 1, TP.HCM', ncc3.sdt = '02838123458', ncc3.email = 'info@abbott.com';");

            // 3. Tạo Vị Trí Kho
            session.run("MERGE (vt1:ViTriKho {maViTri: 'K1-T1'}) SET vt1.tenViTri = 'Kệ 1 - Tầng 1 (Khu A)';");
            session.run("MERGE (vt2:ViTriKho {maViTri: 'K1-T2'}) SET vt2.tenViTri = 'Kệ 1 - Tầng 2 (Khu A)';");
            session.run("MERGE (vt3:ViTriKho {maViTri: 'K2-T1'}) SET vt3.tenViTri = 'Kệ 2 - Tầng 1 (Khu B)';");

            // 4. Tạo Hàng Hóa và liên kết Lưu trữ
            session.run(
                    "MERGE (h1:HangHoa {maHang: 'SP01'}) " +
                    "SET h1.tenHang = 'Dầu gội Clear Men', h1.dvt = 'Chai', h1.quyCach = '650g/chai';");
            session.run(
                    "MERGE (h2:HangHoa {maHang: 'SP02'}) " +
                    "SET h2.tenHang = 'Nước mắm Nam Ngư', h2.dvt = 'Chai', h2.quyCach = '500ml/chai';");
            session.run(
                    "MERGE (h3:HangHoa {maHang: 'SP03'}) " +
                    "SET h3.tenHang = 'Sữa bột Ensure Gold', h3.dvt = 'Hộp', h3.quyCach = '850g/hộp';");
            session.run(
                    "MERGE (h4:HangHoa {maHang: 'SP04'}) " +
                    "SET h4.tenHang = 'Bột giặt OMO', h4.dvt = 'Bịch', h4.quyCach = '4.5kg/bịch';");

            session.run(
                    "MATCH (h1:HangHoa {maHang: 'SP01'}), (vt1:ViTriKho {maViTri: 'K1-T1'}) " +
                    "MERGE (h1)-[:LUU_TAI]->(vt1);");
            session.run(
                    "MATCH (h2:HangHoa {maHang: 'SP02'}), (vt2:ViTriKho {maViTri: 'K1-T2'}) " +
                    "MERGE (h2)-[:LUU_TAI]->(vt2);");
            session.run(
                    "MATCH (h3:HangHoa {maHang: 'SP03'}), (vt3:ViTriKho {maViTri: 'K2-T1'}) " +
                    "MERGE (h3)-[:LUU_TAI]->(vt3);");
            session.run(
                    "MATCH (h4:HangHoa {maHang: 'SP04'}), (vt3:ViTriKho {maViTri: 'K2-T1'}) " +
                    "MERGE (h4)-[:LUU_TAI]->(vt3);");

            // 5. Cung cấp hàng hóa (NhaCungCap -> HangHoa)
            session.run(
                    "MATCH (ncc:NhaCungCap {maNCC: 'NCC01'}), (h1:HangHoa {maHang: 'SP01'}), (h4:HangHoa {maHang: 'SP04'}) " +
                    "MERGE (ncc)-[:CUNG_CAP]->(h1) " +
                    "MERGE (ncc)-[:CUNG_CAP]->(h4);");
            session.run(
                    "MATCH (ncc:NhaCungCap {maNCC: 'NCC02'}), (h2:HangHoa {maHang: 'SP02'}) " +
                    "MERGE (ncc)-[:CUNG_CAP]->(h2);");
            session.run(
                    "MATCH (ncc:NhaCungCap {maNCC: 'NCC03'}), (h3:HangHoa {maHang: 'SP03'}) " +
                    "MERGE (ncc)-[:CUNG_CAP]->(h3);");

            // 6. Bộ phận nhận hàng
            session.run("MERGE (bp1:BoPhan {tenBoPhan: 'Phòng Kinh Doanh'});");
            session.run("MERGE (bp2:BoPhan {tenBoPhan: 'Phân Xưởng Sản Xuất'});");
            session.run("MERGE (bp3:BoPhan {tenBoPhan: 'Đại Lý Phân Phối'});");

            // 7. Tạo chuỗi Phiếu Xuất liên kết (đáp ứng cả Feature 1, 2 và 3)
            // PX001: SP01 + SP04 cho Phòng Kinh Doanh
            session.run(
                    "MERGE (px:PhieuXuat {maPhieu: 'PX001'}) " +
                    "SET px.ngayLap = '2026-05-21', px.mucDich = 'Giao đại lý Bình Thạnh' " +
                    "WITH px " +
                    "MATCH (bp:BoPhan {tenBoPhan: 'Phòng Kinh Doanh'}), (h1:HangHoa {maHang: 'SP01'}), (h4:HangHoa {maHang: 'SP04'}) " +
                    "MERGE (px)-[:CAP_CHO]->(bp) " +
                    "MERGE (px)-[:XUAT_HANG {soLuong: 20, donGia: 150000.0}]->(h1) " +
                    "MERGE (px)-[:XUAT_HANG {soLuong: 50, donGia: 180000.0}]->(h4);");

            // PX002: SP01 + SP04 cho Đại Lý Phân Phối
            session.run(
                    "MERGE (px:PhieuXuat {maPhieu: 'PX002'}) " +
                    "SET px.ngayLap = '2026-06-10', px.mucDich = 'Cung cấp hàng định kỳ' " +
                    "WITH px " +
                    "MATCH (bp:BoPhan {tenBoPhan: 'Đại Lý Phân Phối'}), (h1:HangHoa {maHang: 'SP01'}), (h4:HangHoa {maHang: 'SP04'}) " +
                    "MERGE (px)-[:CAP_CHO]->(bp) " +
                    "MERGE (px)-[:XUAT_HANG {soLuong: 15, donGia: 150000.0}]->(h1) " +
                    "MERGE (px)-[:XUAT_HANG {soLuong: 30, donGia: 180000.0}]->(h4);");

            // PX003: SP02 cho Phân Xưởng Sản Xuất
            session.run(
                    "MERGE (px:PhieuXuat {maPhieu: 'PX003'}) " +
                    "SET px.ngayLap = '2026-06-15', px.mucDich = 'Bếp ăn công nghiệp' " +
                    "WITH px " +
                    "MATCH (bp:BoPhan {tenBoPhan: 'Phân Xưởng Sản Xuất'}), (h2:HangHoa {maHang: 'SP02'}) " +
                    "MERGE (px)-[:CAP_CHO]->(bp) " +
                    "MERGE (px)-[:XUAT_HANG {soLuong: 10, donGia: 25000.0}]->(h2);");

            // PX004: SP01 + SP04 cho Đại Lý Phân Phối
            session.run(
                    "MERGE (px:PhieuXuat {maPhieu: 'PX004'}) " +
                    "SET px.ngayLap = '2026-07-02', px.mucDich = 'Bổ sung tồn kho đại lý' " +
                    "WITH px " +
                    "MATCH (bp:BoPhan {tenBoPhan: 'Đại Lý Phân Phối'}), (h1:HangHoa {maHang: 'SP01'}), (h4:HangHoa {maHang: 'SP04'}) " +
                    "MERGE (px)-[:CAP_CHO]->(bp) " +
                    "MERGE (px)-[:XUAT_HANG {soLuong: 25, donGia: 150000.0}]->(h1) " +
                    "MERGE (px)-[:XUAT_HANG {soLuong: 40, donGia: 180000.0}]->(h4);");

            // PX005: SP01 + SP02 + SP04 cho Phòng Kinh Doanh
            session.run(
                    "MERGE (px:PhieuXuat {maPhieu: 'PX005'}) " +
                    "SET px.ngayLap = '2026-07-20', px.mucDich = 'Đơn hàng combo khuyến mãi' " +
                    "WITH px " +
                    "MATCH (bp:BoPhan {tenBoPhan: 'Phòng Kinh Doanh'}), (h1:HangHoa {maHang: 'SP01'}), (h2:HangHoa {maHang: 'SP02'}), (h4:HangHoa {maHang: 'SP04'}) " +
                    "MERGE (px)-[:CAP_CHO]->(bp) " +
                    "MERGE (px)-[:XUAT_HANG {soLuong: 30, donGia: 150000.0}]->(h1) " +
                    "MERGE (px)-[:XUAT_HANG {soLuong: 15, donGia: 25000.0}]->(h2) " +
                    "MERGE (px)-[:XUAT_HANG {soLuong: 20, donGia: 180000.0}]->(h4);");

            // PX006: SP03 + SP01 cho Phòng Kinh Doanh (để SP03 có quan hệ xuất cùng SP01)
            session.run(
                    "MERGE (px:PhieuXuat {maPhieu: 'PX006'}) " +
                    "SET px.ngayLap = '2026-08-05', px.mucDich = 'Giao hàng đợt 2' " +
                    "WITH px " +
                    "MATCH (bp:BoPhan {tenBoPhan: 'Phòng Kinh Doanh'}), (h1:HangHoa {maHang: 'SP01'}), (h3:HangHoa {maHang: 'SP03'}) " +
                    "MERGE (px)-[:CAP_CHO]->(bp) " +
                    "MERGE (px)-[:XUAT_HANG {soLuong: 10, donGia: 150000.0}]->(h1) " +
                    "MERGE (px)-[:XUAT_HANG {soLuong: 20, donGia: 320000.0}]->(h3);");

            // PX007: SP03 + SP02 cho Phân Xưởng Sản Xuất (để SP03 có quan hệ xuất cùng SP02)
            session.run(
                    "MERGE (px:PhieuXuat {maPhieu: 'PX007'}) " +
                    "SET px.ngayLap = '2026-08-12', px.mucDich = 'Phục vụ dinh dưỡng bếp ăn' " +
                    "WITH px " +
                    "MATCH (bp:BoPhan {tenBoPhan: 'Phân Xưởng Sản Xuất'}), (h2:HangHoa {maHang: 'SP02'}), (h3:HangHoa {maHang: 'SP03'}) " +
                    "MERGE (px)-[:CAP_CHO]->(bp) " +
                    "MERGE (px)-[:XUAT_HANG {soLuong: 12, donGia: 25000.0}]->(h2) " +
                    "MERGE (px)-[:XUAT_HANG {soLuong: 15, donGia: 320000.0}]->(h3);");

            System.out.println("✅ Da seed thanh cong toan bo Graph vao Neo4j!");
        } catch (Exception e) {
            System.err.println("❌ Loi khi seed du lieu Neo4j: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void seedRedis() {
        System.out.println("\n[2/2] Dang seed du lieu vao Redis Cache & Feed...");
        try (Jedis jedis = RedisConnection.getConnection()) {
            if (jedis == null) {
                System.err.println("❌ Khong the ket noi Redis. Vui long kiem tra Redis server!");
                return;
            }

            // Xóa dữ liệu feed và thống kê cũ để làm mới
            jedis.del(WarehouseRedisService.KEY_RECENT_FEED);
            String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            jedis.del(WarehouseRedisService.KEY_STATS_PREFIX + today);

            WarehouseRedisService redisService = new WarehouseRedisService(() -> jedis);

            // Thêm các giao dịch mẫu (giao dịch nhập và xuất)
            redisService.pushTransaction(new TransactionFeedItem(
                    "PN001", "NHAP", "Trần Văn An (Thủ kho)", 15000000.0, 1,
                    today + " 08:30:15", "Nhập 100 Chai Dầu gội Clear Men từ Unilever Việt Nam"));

            redisService.pushTransaction(new TransactionFeedItem(
                    "PN002", "NHAP", "Trần Văn An (Thủ kho)", 9500000.0, 1,
                    today + " 09:15:20", "Nhập 50 Hộp Sữa bột Ensure Gold từ Abbott"));

            redisService.pushTransaction(new TransactionFeedItem(
                    "PX001", "XUAT", "Nguyễn Văn Minh (Trưởng kho)", 12000000.0, 2,
                    today + " 10:00:45", "Xuất 20 Clear Men + 50 OMO cho Phòng Kinh Doanh"));

            redisService.pushTransaction(new TransactionFeedItem(
                    "PN003", "NHAP", "Trần Văn An (Thủ kho)", 2000000.0, 1,
                    today + " 13:20:10", "Nhập 80 Chai Nước mắm Nam Ngư từ Masan"));

            redisService.pushTransaction(new TransactionFeedItem(
                    "PX002", "XUAT", "Nguyễn Văn Minh (Trưởng kho)", 7650000.0, 2,
                    today + " 14:15:30", "Xuất 15 Clear Men + 30 OMO cho Đại Lý Phân Phối"));

            redisService.pushTransaction(new TransactionFeedItem(
                    "PX003", "XUAT", "Nguyễn Văn Minh (Trưởng kho)", 250000.0, 1,
                    today + " 15:45:00", "Xuất 10 Nước mắm Nam Ngư cho Phân Xưởng Sản Xuất"));

            System.out.println("✅ Da seed thanh cong 6 giao dịch va thong ke ngay vao Redis!");
        } catch (Exception e) {
            System.err.println("❌ Loi khi seed du lieu Redis: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

