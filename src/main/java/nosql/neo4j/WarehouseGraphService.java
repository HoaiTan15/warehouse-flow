package nosql.neo4j;

import model.SupplyChainTraceDTO;
import model.SupplierImpactAnalysisDTO;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Value;

import java.util.*;

public class WarehouseGraphService {

    @FunctionalInterface
    public interface SessionProvider {
        Session getSession();
    }

    private final SessionProvider provider;

    public WarehouseGraphService() {
        this(Neo4jConnection::getSession);
    }

    public WarehouseGraphService(SessionProvider provider) {
        this.provider = provider != null ? provider : Neo4jConnection::getSession;
    }

    /**
     * Chức năng 1: Truy vết chuỗi cung ứng & hành trình phân phối của một mặt hàng
     * Kết quả bao gồm:
     * - Nguồn gốc: Nhà Cung Cấp
     * - Lưu kho: Vị Trí Kệ Kho
     * - Phân phối: Các Bộ Phận đã nhận hàng kèm số lượng & ngày xuất
     */
    public SupplyChainTraceDTO traceSupplyChain(String maHang) {
        if (maHang == null || maHang.trim().isEmpty()) {
            return null;
        }

        String targetCode = maHang.trim();

        String cypher =
                "MATCH (h:HangHoa {maHang: $maHang}) " +
                "OPTIONAL MATCH (ncc:NhaCungCap)-[:CUNG_CAP]->(h) " +
                "OPTIONAL MATCH (h)-[:LUU_TAI]->(vt:ViTriKho) " +
                "OPTIONAL MATCH (px:PhieuXuat)-[x:XUAT_HANG]->(h) " +
                "OPTIONAL MATCH (px)-[:CAP_CHO]->(bp:BoPhan) " +
                "RETURN h.maHang AS maHang, h.tenHang AS tenHang, h.dvt AS dvt, h.quyCach AS quyCach, " +
                "       ncc.maNCC AS maNCC, ncc.tenNCC AS tenNCC, ncc.sdt AS sdtNCC, ncc.email AS emailNCC, ncc.diaChi AS diaChiNCC, " +
                "       vt.maViTri AS maViTri, vt.tenViTri AS tenViTri, " +
                "       px.maPhieu AS maPhieuXuat, px.ngayLap AS ngayXuat, px.mucDich AS mucDichXuat, " +
                "       bp.tenBoPhan AS boPhanNhan, " +
                "       x.soLuong AS soLuongXuat, x.donGia AS donGiaXuat";

        try (Session session = provider.getSession()) {
            if (session == null) {
                System.err.println("Neo4j session is null (server may be offline).");
                return null;
            }

            Map<String, Object> params = Collections.singletonMap("maHang", targetCode);
            Result result = session.run(cypher, params);

            if (result == null || !result.hasNext()) {
                return null;
            }

            SupplyChainTraceDTO dto = null;
            Set<String> processedOutbound = new HashSet<>();

            while (result.hasNext()) {
                Record record = result.next();
                if (dto == null) {
                    dto = new SupplyChainTraceDTO();
                    dto.setMaHang(getStringSafe(record.get("maHang")));
                    dto.setTenHang(getStringSafe(record.get("tenHang")));
                    dto.setDvt(getStringSafe(record.get("dvt")));
                    dto.setQuyCach(getStringSafe(record.get("quyCach")));

                    dto.setMaNCC(getStringSafe(record.get("maNCC")));
                    dto.setTenNCC(getStringSafe(record.get("tenNCC")));
                    dto.setSdtNCC(getStringSafe(record.get("sdtNCC")));
                    dto.setEmailNCC(getStringSafe(record.get("emailNCC")));
                    dto.setDiaChiNCC(getStringSafe(record.get("diaChiNCC")));

                    dto.setMaViTri(getStringSafe(record.get("maViTri")));
                    dto.setTenViTri(getStringSafe(record.get("tenViTri")));
                }

                String maPhieu = getStringSafe(record.get("maPhieuXuat"));
                String boPhan = getStringSafe(record.get("boPhanNhan"));
                if (maPhieu != null && !processedOutbound.contains(maPhieu + "_" + boPhan)) {
                    SupplyChainTraceDTO.XuatKhoDestination dest = new SupplyChainTraceDTO.XuatKhoDestination();
                    dest.setMaPhieu(maPhieu);
                    dest.setNgayLap(getStringSafe(record.get("ngayXuat")));
                    dest.setBoPhan(boPhan != null ? boPhan : "Chưa xác định");
                    dest.setSoLuong(getIntSafe(record.get("soLuongXuat")));
                    dest.setDonGia(getDoubleSafe(record.get("donGiaXuat")));
                    dest.setGhiChu(getStringSafe(record.get("mucDichXuat")));

                    dto.addXuatKho(dest);
                    processedOutbound.add(maPhieu + "_" + boPhan);
                }
            }

            return dto;
        } catch (Exception e) {
            System.err.println("WarehouseGraphService.traceSupplyChain error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Chức năng 2: Gợi ý các mặt hàng thường xuyên được xuất cùng nhau trong lịch sử các phiếu xuất (Recommendation System)
     */
    public List<model.ProductRecommendationDTO> recommendRelatedProducts(String maHang, int limit) {
        if (maHang == null || maHang.trim().isEmpty() || limit <= 0) {
            return Collections.emptyList();
        }

        String targetCode = maHang.trim();
        String cypher =
                "MATCH (h1:HangHoa {maHang: $maHang})<-[:XUAT_HANG]-(px:PhieuXuat)-[:XUAT_HANG]->(h2:HangHoa) " +
                "WHERE h1.maHang <> h2.maHang " +
                "RETURN h2.maHang AS maHang, h2.tenHang AS tenHang, h2.dvt AS dvt, " +
                "       count(DISTINCT px) AS soLanXuatCung " +
                "ORDER BY soLanXuatCung DESC " +
                "LIMIT $limit";

        try (Session session = provider.getSession()) {
            if (session == null) {
                System.err.println("Neo4j session is null (server may be offline).");
                return Collections.emptyList();
            }

            Map<String, Object> params = new HashMap<>();
            params.put("maHang", targetCode);
            params.put("limit", limit);

            Result result = session.run(cypher, params);
            if (result == null) {
                return Collections.emptyList();
            }

            List<model.ProductRecommendationDTO> recommendations = new ArrayList<>();
            while (result.hasNext()) {
                Record record = result.next();
                String code = getStringSafe(record.get("maHang"));
                String name = getStringSafe(record.get("tenHang"));
                String dvt = getStringSafe(record.get("dvt"));
                long count = getLongSafe(record.get("soLanXuatCung"));

                if (code != null) {
                    recommendations.add(new model.ProductRecommendationDTO(code, name, dvt, count));
                }
            }
            return recommendations;
        } catch (Exception e) {
            System.err.println("WarehouseGraphService.recommendRelatedProducts error: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Chức năng 3: Phân tích mức độ rủi ro và phạm vi ảnh hưởng khi một Nhà Cung Cấp gặp sự cố (Supplier Impact Analysis)
     */
    public SupplierImpactAnalysisDTO analyzeSupplierImpact(String maNCC) {
        if (maNCC == null || maNCC.trim().isEmpty()) {
            return null;
        }

        String targetSupplier = maNCC.trim();
        String cypher =
                "MATCH (ncc:NhaCungCap {maNCC: $maNCC}) " +
                "OPTIONAL MATCH (ncc)-[:CUNG_CAP]->(h:HangHoa) " +
                "OPTIONAL MATCH (px:PhieuXuat)-[x:XUAT_HANG]->(h) " +
                "OPTIONAL MATCH (px)-[:CAP_CHO]->(bp:BoPhan) " +
                "RETURN ncc.maNCC AS maNCC, ncc.tenNCC AS tenNCC, ncc.sdt AS sdtNCC, ncc.email AS emailNCC, " +
                "       h.maHang AS maHang, h.tenHang AS tenHang, " +
                "       bp.tenBoPhan AS boPhan, " +
                "       x.soLuong AS soLuongXuat, " +
                "       px.maPhieu AS maPhieu";

        try (Session session = provider.getSession()) {
            if (session == null) {
                System.err.println("Neo4j session is null (server may be offline).");
                return null;
            }

            Map<String, Object> params = new HashMap<>();
            params.put("maNCC", targetSupplier);

            Result result = session.run(cypher, params);
            if (result == null || !result.hasNext()) {
                return null;
            }

            SupplierImpactAnalysisDTO dto = null;
            Map<String, SupplierImpactAnalysisDTO.DepartmentImpact> boPhanMap = new LinkedHashMap<>();
            Map<String, Set<String>> phieuByBoPhan = new HashMap<>();

            while (result.hasNext()) {
                Record record = result.next();
                if (dto == null) {
                    String code = getStringSafe(record.get("maNCC"));
                    String name = getStringSafe(record.get("tenNCC"));
                    String sdt = getStringSafe(record.get("sdtNCC"));
                    String email = getStringSafe(record.get("emailNCC"));
                    dto = new SupplierImpactAnalysisDTO(code, name, sdt, email);
                }

                String maHang = getStringSafe(record.get("maHang"));
                String tenHang = getStringSafe(record.get("tenHang"));
                if (maHang != null) {
                    String hangHienThi = tenHang != null ? maHang + " (" + tenHang + ")" : maHang;
                    dto.addHangCungCap(hangHienThi);

                    String boPhan = getStringSafe(record.get("boPhan"));
                    if (boPhan != null && !boPhan.trim().isEmpty()) {
                        String bpKey = boPhan.trim();
                        SupplierImpactAnalysisDTO.DepartmentImpact impact =
                                boPhanMap.computeIfAbsent(bpKey, SupplierImpactAnalysisDTO.DepartmentImpact::new);

                        impact.addHangPhuThuoc(hangHienThi);
                        int soLuong = getIntSafe(record.get("soLuongXuat"));
                        impact.addSoLuong(soLuong);

                        String maPhieu = getStringSafe(record.get("maPhieu"));
                        if (maPhieu != null && !maPhieu.trim().isEmpty()) {
                            if (phieuByBoPhan.computeIfAbsent(bpKey, k -> new HashSet<>()).add(maPhieu.trim())) {
                                impact.incrementSoDonHang();
                            }
                        }
                    }
                }
            }

            if (dto != null) {
                dto.setDanhSachBoPhanAnhHuong(new ArrayList<>(boPhanMap.values()));
            }

            return dto;
        } catch (Exception e) {
            System.err.println("WarehouseGraphService.analyzeSupplierImpact error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Lấy danh sách tất cả Nhà Cung Cấp đang tồn tại trong Graph Database
     */
    public List<Map<String, String>> getAllSuppliersInGraph() {
        String cypher = "MATCH (ncc:NhaCungCap) RETURN ncc.maNCC AS maNCC, ncc.tenNCC AS tenNCC ORDER BY ncc.maNCC";
        try (Session session = provider.getSession()) {
            if (session == null) return Collections.emptyList();
            Result result = session.run(cypher);
            if (result == null) return Collections.emptyList();
            List<Map<String, String>> list = new ArrayList<>();
            while (result.hasNext()) {
                Record r = result.next();
                Map<String, String> m = new HashMap<>();
                m.put("maNCC", getStringSafe(r.get("maNCC")));
                m.put("tenNCC", getStringSafe(r.get("tenNCC")));
                list.add(m);
            }
            return list;
        } catch (Exception e) {
            System.err.println("WarehouseGraphService.getAllSuppliersInGraph error: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private static String getStringSafe(Value val) {
        if (val == null || val.isNull()) return null;
        return val.asString();
    }

    private static int getIntSafe(Value val) {
        if (val == null || val.isNull()) return 0;
        try {
            return val.asInt();
        } catch (Exception e) {
            return 0;
        }
    }

    private static long getLongSafe(Value val) {
        if (val == null || val.isNull()) return 0L;
        try {
            return val.asLong();
        } catch (Exception e) {
            return 0L;
        }
    }

    private static double getDoubleSafe(Value val) {
        if (val == null || val.isNull()) return 0.0;
        try {
            return val.asDouble();
        } catch (Exception e) {
            return 0.0;
        }
    }
}

