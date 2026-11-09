package nosql.neo4j;

import model.SupplyChainTraceDTO;
import model.SupplierImpactAnalysisDTO;
import org.junit.Test;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Value;
import org.neo4j.driver.internal.value.IntegerValue;
import org.neo4j.driver.internal.value.FloatValue;
import org.neo4j.driver.internal.value.NullValue;
import org.neo4j.driver.internal.value.StringValue;

import java.lang.reflect.Proxy;
import java.util.*;

import static org.junit.Assert.*;

public class WarehouseGraphServiceTest {

    // Helper: Fake Record using Map<String, Value>
    private static Record createFakeRecord(Map<String, Value> fields) {
        return (Record) Proxy.newProxyInstance(
                Record.class.getClassLoader(),
                new Class<?>[]{Record.class},
                (proxy, method, args) -> {
                    String methodName = method.getName();
                    if ("get".equals(methodName) && args.length == 1 && args[0] instanceof String) {
                        Value val = fields.get((String) args[0]);
                        return val != null ? val : NullValue.NULL;
                    }
                    if ("containsKey".equals(methodName) && args.length == 1 && args[0] instanceof String) {
                        return fields.containsKey((String) args[0]);
                    }
                    return null;
                }
        );
    }

    // Helper: Fake Result using List<Record>
    private static Result createFakeResult(List<Record> records) {
        Iterator<Record> it = records.iterator();
        return (Result) Proxy.newProxyInstance(
                Result.class.getClassLoader(),
                new Class<?>[]{Result.class},
                (proxy, method, args) -> {
                    String methodName = method.getName();
                    if ("hasNext".equals(methodName)) {
                        return it.hasNext();
                    }
                    if ("next".equals(methodName)) {
                        return it.next();
                    }
                    return null;
                }
        );
    }

    // Helper: Fake Session
    private static Session createFakeSession(List<Record> recordsToReturn, boolean throwException) {
        return (Session) Proxy.newProxyInstance(
                Session.class.getClassLoader(),
                new Class<?>[]{Session.class},
                (proxy, method, args) -> {
                    if ("close".equals(method.getName())) {
                        return null;
                    }
                    if ("run".equals(method.getName())) {
                        if (throwException) {
                            throw new RuntimeException("Simulated Neo4j connection failure");
                        }
                        return createFakeResult(recordsToReturn);
                    }
                    return null;
                }
        );
    }

    @Test
    public void testTraceSupplyChain_HappyPath() {
        Map<String, Value> row1 = new HashMap<>();
        row1.put("maHang", new StringValue("SP01"));
        row1.put("tenHang", new StringValue("Dầu gội Clear Men"));
        row1.put("dvt", new StringValue("Chai"));
        row1.put("quyCach", new StringValue("650g/chai"));
        row1.put("maNCC", new StringValue("NCC01"));
        row1.put("tenNCC", new StringValue("Unilever Việt Nam"));
        row1.put("sdtNCC", new StringValue("02838123456"));
        row1.put("emailNCC", new StringValue("contact@unilever.com"));
        row1.put("diaChiNCC", new StringValue("Quận 7, TP.HCM"));
        row1.put("maViTri", new StringValue("K1-T1"));
        row1.put("tenViTri", new StringValue("Kệ 1 - Tầng 1 (Khu A)"));
        row1.put("maPhieuXuat", new StringValue("PX001"));
        row1.put("ngayXuat", new StringValue("2026-05-21"));
        row1.put("mucDichXuat", new StringValue("Giao đại lý"));
        row1.put("boPhanNhan", new StringValue("Phòng Kinh Doanh"));
        row1.put("soLuongXuat", new IntegerValue(20));
        row1.put("donGiaXuat", new FloatValue(150000.0));

        Map<String, Value> row2 = new HashMap<>(row1);
        row2.put("maPhieuXuat", new StringValue("PX002"));
        row2.put("ngayXuat", new StringValue("2026-06-10"));
        row2.put("boPhanNhan", new StringValue("Đại Lý Phân Phối"));
        row2.put("soLuongXuat", new IntegerValue(15));
        row2.put("donGiaXuat", new FloatValue(150000.0));

        List<Record> records = Arrays.asList(createFakeRecord(row1), createFakeRecord(row2));
        Session fakeSession = createFakeSession(records, false);

        WarehouseGraphService service = new WarehouseGraphService(() -> fakeSession);
        SupplyChainTraceDTO dto = service.traceSupplyChain("SP01");

        assertNotNull(dto);
        assertEquals("SP01", dto.getMaHang());
        assertEquals("Dầu gội Clear Men", dto.getTenHang());
        assertEquals("Unilever Việt Nam", dto.getTenNCC());
        assertEquals("Kệ 1 - Tầng 1 (Khu A)", dto.getTenViTri());
        assertEquals(2, dto.getDanhSachXuat().size());
        assertEquals("Phòng Kinh Doanh", dto.getDanhSachXuat().get(0).getBoPhan());
        assertEquals(20, dto.getDanhSachXuat().get(0).getSoLuong());
        assertEquals("Đại Lý Phân Phối", dto.getDanhSachXuat().get(1).getBoPhan());
    }

    @Test
    public void testTraceSupplyChain_NullOrEmptyCode_ReturnsNull() {
        WarehouseGraphService service = new WarehouseGraphService(() -> createFakeSession(Collections.emptyList(), false));
        assertNull("Null code should return null immediately", service.traceSupplyChain(null));
        assertNull("Empty code should return null immediately", service.traceSupplyChain("   "));
    }

    @Test
    public void testTraceSupplyChain_SessionNull_ReturnsNullGracefully() {
        WarehouseGraphService service = new WarehouseGraphService(() -> null);
        SupplyChainTraceDTO dto = service.traceSupplyChain("SP01");
        assertNull("Null session should return null without crashing", dto);
    }

    @Test
    public void testTraceSupplyChain_ItemNotFound_ReturnsNull() {
        Session fakeSession = createFakeSession(Collections.emptyList(), false);
        WarehouseGraphService service = new WarehouseGraphService(() -> fakeSession);
        assertNull(service.traceSupplyChain("NON_EXISTENT"));
    }

    @Test
    public void testTraceSupplyChain_NoSupplierYet_HandlesGracefully() {
        Map<String, Value> row = new HashMap<>();
        row.put("maHang", new StringValue("SP05"));
        row.put("tenHang", new StringValue("Sản phẩm tự sản xuất"));
        row.put("maNCC", NullValue.NULL);
        row.put("tenNCC", NullValue.NULL);
        row.put("maViTri", new StringValue("K3-T1"));
        row.put("tenViTri", new StringValue("Kệ 3 Tầng 1"));
        row.put("maPhieuXuat", NullValue.NULL);

        Session fakeSession = createFakeSession(Collections.singletonList(createFakeRecord(row)), false);
        WarehouseGraphService service = new WarehouseGraphService(() -> fakeSession);

        SupplyChainTraceDTO dto = service.traceSupplyChain("SP05");
        assertNotNull(dto);
        assertEquals("SP05", dto.getMaHang());
        assertNull(dto.getTenNCC());
        assertEquals("Kệ 3 Tầng 1", dto.getTenViTri());
        assertTrue(dto.getDanhSachXuat().isEmpty());
    }

    @Test
    public void testTraceSupplyChain_NoOutboundShipmentsYet_EmptyList() {
        Map<String, Value> row = new HashMap<>();
        row.put("maHang", new StringValue("SP03"));
        row.put("tenHang", new StringValue("Sữa Ensure"));
        row.put("tenNCC", new StringValue("Abbott"));
        row.put("tenViTri", new StringValue("K2-T1"));
        row.put("maPhieuXuat", NullValue.NULL); // Chưa từng xuất

        Session fakeSession = createFakeSession(Collections.singletonList(createFakeRecord(row)), false);
        WarehouseGraphService service = new WarehouseGraphService(() -> fakeSession);

        SupplyChainTraceDTO dto = service.traceSupplyChain("SP03");
        assertNotNull(dto);
        assertEquals(0, dto.getDanhSachXuat().size());
    }

    @Test
    public void testTraceSupplyChain_SessionThrowsException_ReturnsNullWithoutCrash() {
        Session errorSession = createFakeSession(Collections.emptyList(), true);
        WarehouseGraphService service = new WarehouseGraphService(() -> errorSession);
        assertNull("Should catch exception and return null safely", service.traceSupplyChain("SP01"));
    }

    @Test
    public void testToJsonGraph_GeneratesValidVisNetworkJson() {
        SupplyChainTraceDTO dto = new SupplyChainTraceDTO("SP01", "Dầu gội Clear Men", "Chai", "650g");
        dto.setTenNCC("Unilever Việt Nam");
        dto.setMaNCC("NCC01");
        dto.setTenViTri("Kệ 1");
        dto.setMaViTri("K1");
        dto.addXuatKho(new SupplyChainTraceDTO.XuatKhoDestination("PX01", "2026-05-21", "Phòng Kinh Doanh", 20, 100.0, "Xuất kho"));

        String json = dto.toJsonGraph();
        assertNotNull(json);
        assertTrue(json.contains("nodes"));
        assertTrue(json.contains("edges"));
        assertTrue(json.contains("HH_SP01"));
        assertTrue(json.contains("NCC_NCC01"));
        assertTrue(json.contains("CUNG CẤP"));
        assertTrue(json.contains("LƯU TRỮ"));
        assertTrue(json.contains("Phòng Kinh Doanh"));
    }

    @Test
    public void testToJsonGraph_EmptyDTO_ReturnsEmptyNodesAndEdges() {
        SupplyChainTraceDTO dto = new SupplyChainTraceDTO();
        String json = dto.toJsonGraph();
        assertTrue(json.contains("\"nodes\":[]"));
        assertTrue(json.contains("\"edges\":[]"));
    }

    // =========================================================================
    // CHỨC NĂNG 2: GỢI Ý MẶT HÀNG THƯỜNG XUẤT CÙNG NHAU (RECOMMENDATION)
    // =========================================================================

    @Test
    public void testRecommendRelatedProducts_HappyPath() {
        Map<String, Value> rec1 = new HashMap<>();
        rec1.put("maHang", new StringValue("SP04"));
        rec1.put("tenHang", new StringValue("Bột giặt OMO"));
        rec1.put("dvt", new StringValue("Bịch"));
        rec1.put("soLanXuatCung", new IntegerValue(2));

        Map<String, Value> rec2 = new HashMap<>();
        rec2.put("maHang", new StringValue("SP02"));
        rec2.put("tenHang", new StringValue("Nước mắm Nam Ngư"));
        rec2.put("dvt", new StringValue("Chai"));
        rec2.put("soLanXuatCung", new IntegerValue(1));

        Session fakeSession = createFakeSession(Arrays.asList(createFakeRecord(rec1), createFakeRecord(rec2)), false);
        WarehouseGraphService service = new WarehouseGraphService(() -> fakeSession);

        List<model.ProductRecommendationDTO> list = service.recommendRelatedProducts("SP01", 5);

        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals("SP04", list.get(0).getMaHang());
        assertEquals(2L, list.get(0).getSoLanXuatCung());
        assertEquals("SP02", list.get(1).getMaHang());
        assertEquals(1L, list.get(1).getSoLanXuatCung());
    }

    @Test
    public void testRecommendRelatedProducts_NullOrEmptyCode_ReturnsEmptyList() {
        WarehouseGraphService service = new WarehouseGraphService(() -> createFakeSession(Collections.emptyList(), false));
        assertTrue(service.recommendRelatedProducts(null, 5).isEmpty());
        assertTrue(service.recommendRelatedProducts("   ", 5).isEmpty());
    }

    @Test
    public void testRecommendRelatedProducts_ZeroOrNegativeLimit_ReturnsEmptyList() {
        WarehouseGraphService service = new WarehouseGraphService(() -> createFakeSession(Collections.emptyList(), false));
        assertTrue(service.recommendRelatedProducts("SP01", 0).isEmpty());
        assertTrue(service.recommendRelatedProducts("SP01", -3).isEmpty());
    }

    @Test
    public void testRecommendRelatedProducts_SessionNull_ReturnsEmptyListGracefully() {
        WarehouseGraphService deadService = new WarehouseGraphService(() -> null);
        List<model.ProductRecommendationDTO> list = deadService.recommendRelatedProducts("SP01", 5);
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    @Test
    public void testRecommendRelatedProducts_NoCoOccurringProducts_ReturnsEmptyList() {
        Session fakeSession = createFakeSession(Collections.emptyList(), false);
        WarehouseGraphService service = new WarehouseGraphService(() -> fakeSession);
        List<model.ProductRecommendationDTO> list = service.recommendRelatedProducts("SP99", 5);
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    @Test
    public void testRecommendRelatedProducts_SessionThrowsException_ReturnsEmptyListWithoutCrash() {
        Session errorSession = createFakeSession(Collections.emptyList(), true);
        WarehouseGraphService service = new WarehouseGraphService(() -> errorSession);
        List<model.ProductRecommendationDTO> list = service.recommendRelatedProducts("SP01", 5);
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    // =========================================================================
    // CHỨC NĂNG 3: PHÂN TÍCH RỦI RO & PHẠM VI ẢNH HƯỞNG NHÀ CUNG CẤP (IMPACT ANALYSIS)
    // =========================================================================

    @Test
    public void testAnalyzeSupplierImpact_HappyPath_HighRisk() {
        // Record 1: SP01 -> Phong Kinh Doanh (sl: 30, phieu: PX01)
        Map<String, Value> r1 = new HashMap<>();
        r1.put("maNCC", new StringValue("NCC01"));
        r1.put("tenNCC", new StringValue("Unilever Việt Nam"));
        r1.put("sdtNCC", new StringValue("02838123456"));
        r1.put("emailNCC", new StringValue("contact@unilever.com"));
        r1.put("maHang", new StringValue("SP01"));
        r1.put("tenHang", new StringValue("Clear Men"));
        r1.put("boPhan", new StringValue("Phòng Kinh Doanh"));
        r1.put("soLuongXuat", new IntegerValue(30));
        r1.put("maPhieu", new StringValue("PX01"));

        // Record 2: SP02 -> Xuong San Xuat (sl: 25, phieu: PX02)
        Map<String, Value> r2 = new HashMap<>(r1);
        r2.put("maHang", new StringValue("SP02"));
        r2.put("tenHang", new StringValue("Lifebuoy"));
        r2.put("boPhan", new StringValue("Xưởng Sản Xuất"));
        r2.put("soLuongXuat", new IntegerValue(25));
        r2.put("maPhieu", new StringValue("PX02"));

        // Record 3: SP03 -> Chưa xuất kho
        Map<String, Value> r3 = new HashMap<>(r1);
        r3.put("maHang", new StringValue("SP03"));
        r3.put("tenHang", new StringValue("Omo"));
        r3.put("boPhan", NullValue.NULL);
        r3.put("soLuongXuat", NullValue.NULL);
        r3.put("maPhieu", NullValue.NULL);

        Session fakeSession = createFakeSession(Arrays.asList(
                createFakeRecord(r1),
                createFakeRecord(r2),
                createFakeRecord(r3)
        ), false);

        WarehouseGraphService service = new WarehouseGraphService(() -> fakeSession);
        SupplierImpactAnalysisDTO dto = service.analyzeSupplierImpact("NCC01");

        assertNotNull(dto);
        assertEquals("NCC01", dto.getMaNCC());
        assertEquals("Unilever Việt Nam", dto.getTenNCC());
        assertEquals(3, dto.getDanhSachHangCungCap().size());
        assertEquals(2, dto.getDanhSachBoPhanAnhHuong().size());
        assertEquals("CAO", dto.getMucDoRuiRo());

        SupplierImpactAnalysisDTO.DepartmentImpact bp1 = dto.getDanhSachBoPhanAnhHuong().get(0);
        assertEquals("Phòng Kinh Doanh", bp1.getTenBoPhan());
        assertEquals(30, bp1.getTongSoLuongDaNhan());
        assertEquals(1, bp1.getSoDonHangLienQuan());

        SupplierImpactAnalysisDTO.DepartmentImpact bp2 = dto.getDanhSachBoPhanAnhHuong().get(1);
        assertEquals("Xưởng Sản Xuất", bp2.getTenBoPhan());
        assertEquals(25, bp2.getTongSoLuongDaNhan());
        assertEquals(1, bp2.getSoDonHangLienQuan());
    }

    @Test
    public void testAnalyzeSupplierImpact_NullOrEmptyCode_ReturnsNull() {
        WarehouseGraphService service = new WarehouseGraphService(() -> createFakeSession(Collections.emptyList(), false));
        assertNull(service.analyzeSupplierImpact(null));
        assertNull(service.analyzeSupplierImpact("   "));
    }

    @Test
    public void testAnalyzeSupplierImpact_NotFound_ReturnsNull() {
        Session fakeSession = createFakeSession(Collections.emptyList(), false);
        WarehouseGraphService service = new WarehouseGraphService(() -> fakeSession);
        assertNull(service.analyzeSupplierImpact("NCC_KHONG_TON_TAI"));
    }

    @Test
    public void testAnalyzeSupplierImpact_NewSupplierNoGoods_ReturnsLowRisk() {
        Map<String, Value> row = new HashMap<>();
        row.put("maNCC", new StringValue("NCC_MOI"));
        row.put("tenNCC", new StringValue("Nhà Cung Cấp Mới"));
        row.put("sdtNCC", new StringValue("0909000111"));
        row.put("emailNCC", new StringValue("nccmoi@gmail.com"));
        row.put("maHang", NullValue.NULL);
        row.put("tenHang", NullValue.NULL);
        row.put("boPhan", NullValue.NULL);
        row.put("soLuongXuat", NullValue.NULL);
        row.put("maPhieu", NullValue.NULL);

        Session fakeSession = createFakeSession(Collections.singletonList(createFakeRecord(row)), false);
        WarehouseGraphService service = new WarehouseGraphService(() -> fakeSession);

        SupplierImpactAnalysisDTO dto = service.analyzeSupplierImpact("NCC_MOI");
        assertNotNull(dto);
        assertEquals("NCC_MOI", dto.getMaNCC());
        assertEquals("Nhà Cung Cấp Mới", dto.getTenNCC());
        assertTrue(dto.getDanhSachHangCungCap().isEmpty());
        assertTrue(dto.getDanhSachBoPhanAnhHuong().isEmpty());
        assertEquals("THẤP", dto.getMucDoRuiRo());
    }

    @Test
    public void testAnalyzeSupplierImpact_GoodsNeverExported_ReturnsLowRisk() {
        Map<String, Value> row = new HashMap<>();
        row.put("maNCC", new StringValue("NCC02"));
        row.put("tenNCC", new StringValue("Masan Consumer"));
        row.put("sdtNCC", new StringValue("02838999888"));
        row.put("emailNCC", new StringValue("contact@masan.com"));
        row.put("maHang", new StringValue("SP10"));
        row.put("tenHang", new StringValue("Tương ớt Chinsu"));
        row.put("boPhan", NullValue.NULL);
        row.put("soLuongXuat", NullValue.NULL);
        row.put("maPhieu", NullValue.NULL);

        Session fakeSession = createFakeSession(Collections.singletonList(createFakeRecord(row)), false);
        WarehouseGraphService service = new WarehouseGraphService(() -> fakeSession);

        SupplierImpactAnalysisDTO dto = service.analyzeSupplierImpact("NCC02");
        assertNotNull(dto);
        assertEquals(1, dto.getDanhSachHangCungCap().size());
        assertTrue(dto.getDanhSachBoPhanAnhHuong().isEmpty());
        assertEquals("THẤP", dto.getMucDoRuiRo());
    }

    @Test
    public void testAnalyzeSupplierImpact_MediumRisk_SingleDepartment() {
        Map<String, Value> row = new HashMap<>();
        row.put("maNCC", new StringValue("NCC03"));
        row.put("tenNCC", new StringValue("Acecook"));
        row.put("sdtNCC", new StringValue("02838111222"));
        row.put("emailNCC", new StringValue("contact@acecook.com"));
        row.put("maHang", new StringValue("SP05"));
        row.put("tenHang", new StringValue("Mì Hảo Hảo"));
        row.put("boPhan", new StringValue("Ban Hậu Cần"));
        row.put("soLuongXuat", new IntegerValue(10));
        row.put("maPhieu", new StringValue("PX03"));

        Session fakeSession = createFakeSession(Collections.singletonList(createFakeRecord(row)), false);
        WarehouseGraphService service = new WarehouseGraphService(() -> fakeSession);

        SupplierImpactAnalysisDTO dto = service.analyzeSupplierImpact("NCC03");
        assertNotNull(dto);
        assertEquals(1, dto.getDanhSachBoPhanAnhHuong().size());
        assertEquals("TRUNG BÌNH", dto.getMucDoRuiRo());
    }

    @Test
    public void testAnalyzeSupplierImpact_SessionNull_ReturnsNullGracefully() {
        WarehouseGraphService deadService = new WarehouseGraphService(() -> null);
        assertNull(deadService.analyzeSupplierImpact("NCC01"));
    }

    @Test
    public void testAnalyzeSupplierImpact_SessionThrowsException_ReturnsNullWithoutCrash() {
        Session errorSession = createFakeSession(Collections.emptyList(), true);
        WarehouseGraphService service = new WarehouseGraphService(() -> errorSession);
        assertNull(service.analyzeSupplierImpact("NCC01"));
    }

    @Test
    public void testSupplierImpact_ToJsonGraph() {
        SupplierImpactAnalysisDTO dto = new SupplierImpactAnalysisDTO("NCC01", "Unilever", "02838123", "uni@gmail.com");
        dto.addHangCungCap("SP01 (Clear Men)");
        SupplierImpactAnalysisDTO.DepartmentImpact dept = new SupplierImpactAnalysisDTO.DepartmentImpact("Kinh Doanh");
        dept.addHangPhuThuoc("SP01 (Clear Men)");
        dept.addSoLuong(20);
        dept.incrementSoDonHang();
        dto.addBoPhanImpact(dept);

        String json = dto.toJsonGraph();
        assertNotNull(json);
        assertTrue(json.contains("nodes"));
        assertTrue(json.contains("edges"));
        assertTrue(json.contains("NCC_NCC01"));
        assertTrue(json.contains("Kinh Doanh"));
        assertTrue(json.contains("Lan truyền rủi ro"));
    }

    @Test
    public void testSupplierImpact_ToJsonGraph_Empty() {
        SupplierImpactAnalysisDTO emptyDto = new SupplierImpactAnalysisDTO();
        String json = emptyDto.toJsonGraph();
        assertNotNull(json);
        assertTrue(json.contains("\"nodes\":[]"));
        assertTrue(json.contains("\"edges\":[]"));
    }

    @Test
    public void testGetAllSuppliersInGraph_HappyPath() {
        Map<String, Value> s1 = new HashMap<>();
        s1.put("maNCC", new StringValue("NCC01"));
        s1.put("tenNCC", new StringValue("Unilever"));

        Map<String, Value> s2 = new HashMap<>();
        s2.put("maNCC", new StringValue("NCC02"));
        s2.put("tenNCC", new StringValue("Masan"));

        Session fakeSession = createFakeSession(Arrays.asList(createFakeRecord(s1), createFakeRecord(s2)), false);
        WarehouseGraphService service = new WarehouseGraphService(() -> fakeSession);

        List<Map<String, String>> list = service.getAllSuppliersInGraph();
        assertEquals(2, list.size());
        assertEquals("NCC01", list.get(0).get("maNCC"));
        assertEquals("Unilever", list.get(0).get("tenNCC"));
    }

    @Test
    public void testGetAllSuppliersInGraph_SessionNullOrException() {
        WarehouseGraphService deadService = new WarehouseGraphService(() -> null);
        assertTrue(deadService.getAllSuppliersInGraph().isEmpty());

        Session errorSession = createFakeSession(Collections.emptyList(), true);
        WarehouseGraphService errorService = new WarehouseGraphService(() -> errorSession);
        assertTrue(errorService.getAllSuppliersInGraph().isEmpty());
    }
}

