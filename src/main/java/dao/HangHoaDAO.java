package dao;

import com.google.gson.reflect.TypeToken;
import model.HangHoa;
import nosql.redis.CacheService;
import nosql.elasticsearch.HangHoaSearchService;
import nosql.mongo.NhatKyThaoTacRepository;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HangHoaDAO {
    private NhatKyThaoTacRepository nhatKyRepo;

    public HangHoaDAO() {
        try {
            this.nhatKyRepo = new NhatKyThaoTacRepository();
        } catch (Exception e) {
            System.err.println("Failed to initialize NhatKyThaoTacRepository: " + e.getMessage());
        }
    }

    // 1. LẤY DANH SÁCH HÀNG HÓA (JOIN ĐỂ LẤY TÊN LOẠI VÀ TÊN VỊ TRÍ) - CÓ CACHE
    public List<HangHoa> getAllHangHoa() {
        Type listType = new TypeToken<List<HangHoa>>(){}.getType();
        List<HangHoa> cached = CacheService.getHangHoaAll(listType);

        if (cached != null) {
            System.out.println("Cache HIT: getAllHangHoa");
            return cached;
        }

        System.out.println("Cache MISS: getAllHangHoa - querying database");
        List<HangHoa> list = new ArrayList<>();
        String sql = "SELECT h.*, l.TenLoai, v.TenViTri FROM HangHoa h "
                   + "LEFT JOIN LoaiHangHoa l ON h.MaLoai = l.MaLoai "
                   + "LEFT JOIN ViTriKho v ON h.MaViTri = v.MaViTri ORDER BY h.MaHang DESC";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new HangHoa(
                    rs.getString("MaHang"), rs.getString("TenHang"), rs.getString("DVT"),
                    rs.getInt("SoLuongTonKho"), rs.getString("QuyCach"), rs.getInt("HanMucTonToiThieu"),
                    rs.getString("MaLoai"), rs.getString("MaViTri"), rs.getString("TenLoai"), rs.getString("TenViTri")
                ));
            }

            CacheService.cacheHangHoaAll(list);
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // 2. LẤY DANH SÁCH LOẠI HÀNG HÓA ĐỂ ĐỔ VÀO DROPDOWN - CÓ CACHE
    public Map<String, String> getMapLoaiHang() {
        Type mapType = new TypeToken<Map<String, String>>(){}.getType();
        Map<String, String> cached = CacheService.getDropdownLoai(mapType);

        if (cached != null) {
            System.out.println("Cache HIT: getMapLoaiHang");
            return cached;
        }

        System.out.println("Cache MISS: getMapLoaiHang - querying database");
        Map<String, String> map = new HashMap<>();
        String sql = "SELECT MaLoai, TenLoai FROM LoaiHangHoa";
        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) { map.put(rs.getString("MaLoai"), rs.getString("TenLoai")); }

            CacheService.cacheDropdownLoai(map);
        } catch (Exception e) { e.printStackTrace(); }
        return map;
    }

    // 3. LẤY DANH SÁCH VỊ TRÍ KHO ĐỂ ĐỔ VÀO DROPDOWN - CÓ CACHE
    public Map<String, String> getMapViTriKho() {
        Type mapType = new TypeToken<Map<String, String>>(){}.getType();
        Map<String, String> cached = CacheService.getDropdownViTri(mapType);

        if (cached != null) {
            System.out.println("Cache HIT: getMapViTriKho");
            return cached;
        }

        System.out.println("Cache MISS: getMapViTriKho - querying database");
        Map<String, String> map = new HashMap<>();
        String sql = "SELECT MaViTri, TenViTri FROM ViTriKho";
        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) { map.put(rs.getString("MaViTri"), rs.getString("TenViTri")); }

            CacheService.cacheDropdownViTri(map);
        } catch (Exception e) { e.printStackTrace(); }
        return map;
    }

    // 4. KIỂM TRA TRÙNG TÊN SẢN PHẨM
    public boolean kiemTraTrungTen(String tenHang, String maHang_loaiTru) {
        String sql = "SELECT COUNT(*) FROM HangHoa WHERE TenHang = ?";
        if (maHang_loaiTru != null && !maHang_loaiTru.isEmpty()) sql += " AND MaHang != ?";
        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenHang);
            if (maHang_loaiTru != null && !maHang_loaiTru.isEmpty()) ps.setString(2, maHang_loaiTru);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // 5. THÊM MỚI HÀNG HÓA
    public void themHangHoa(HangHoa hh) {
        String sql = "INSERT INTO HangHoa (TenHang, DVT, SoLuongTonKho, QuyCach, HanMucTonToiThieu, MaLoai, MaViTri) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hh.getTenHang());
            ps.setString(2, hh.getDvt());
            ps.setInt(3, hh.getSoLuongTonKho());
            ps.setString(4, hh.getQuyCach());
            ps.setInt(5, hh.getHanMucTonToiThieu());
            ps.setString(6, hh.getMaLoai());
            ps.setString(7, hh.getMaViTri());
            ps.executeUpdate();

            CacheService.invalidateHangHoaCache();

            if (nhatKyRepo != null) {
                nhatKyRepo.ghiNhatKy("THEM_HANG_HOA", hh.getTenHang(), "system",
                    "THANH_CONG", "Thêm hàng hóa: " + hh.getTenHang());
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (nhatKyRepo != null) {
                nhatKyRepo.ghiNhatKy("THEM_HANG_HOA", hh.getTenHang(), "system",
                    "THAT_BAI", "Lỗi: " + e.getMessage());
            }
        }
    }

    // 6. SỬA THÔNG TIN HÀNG HÓA
    public void suaHangHoa(HangHoa hh) {
        String sql = "UPDATE HangHoa SET TenHang=?, DVT=?, SoLuongTonKho=?, QuyCach=?, HanMucTonToiThieu=?, MaLoai=?, MaViTri=? WHERE MaHang=?";
        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hh.getTenHang());
            ps.setString(2, hh.getDvt());
            ps.setInt(3, hh.getSoLuongTonKho());
            ps.setString(4, hh.getQuyCach());
            ps.setInt(5, hh.getHanMucTonToiThieu());
            ps.setString(6, hh.getMaLoai());
            ps.setString(7, hh.getMaViTri());
            ps.setString(8, hh.getMaHang());
            ps.executeUpdate();

            CacheService.invalidateHangHoaCache();

            if (nhatKyRepo != null) {
                nhatKyRepo.ghiNhatKy("SUA_HANG_HOA", hh.getMaHang(), "system",
                    "THANH_CONG", "Sửa hàng hóa: " + hh.getMaHang() + " - " + hh.getTenHang());
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (nhatKyRepo != null) {
                nhatKyRepo.ghiNhatKy("SUA_HANG_HOA", hh.getMaHang(), "system",
                    "THAT_BAI", "Lỗi: " + e.getMessage());
            }
        }
    }

    // 7. XÓA HÀNG HÓA
    public String xoaHangHoa(String maHang) {
        String sql = "DELETE FROM HangHoa WHERE MaHang=?";
        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maHang);
            ps.executeUpdate();

            CacheService.invalidateHangHoaCache();

            if (nhatKyRepo != null) {
                nhatKyRepo.ghiNhatKy("XOA_HANG_HOA", maHang, "system",
                    "THANH_CONG", "Xóa hàng hóa: " + maHang);
            }
            return "SUCCESS";
        } catch (Exception e) {
            if (nhatKyRepo != null) {
                nhatKyRepo.ghiNhatKy("XOA_HANG_HOA", maHang, "system",
                    "THAT_BAI", "Lỗi: " + e.getMessage());
            }
            return "LỖI: Không thể xóa mặt hàng này vì đã có lịch sử Nhập/Xuất kho liên quan!";
        }
    }
// ==========================================================================
    // HÀM TÌM KIẾM HÀNG HÓA - CÓ CACHE
    // ==========================================================================
    public List<HangHoa> searchHangHoa(String tuKhoa) {
        tuKhoa = tuKhoa == null ? "" : tuKhoa.trim();
        if (tuKhoa.isEmpty()) return getAllHangHoa();
        List<HangHoa> indexed = HangHoaSearchService.getInstance().search(tuKhoa, this::loadForSearchIndex);
        if (indexed != null) return indexed;
        Type listType = new TypeToken<List<HangHoa>>(){}.getType();
        List<HangHoa> cached = CacheService.getHangHoaSearch(tuKhoa, listType);

        if (cached != null) {
            System.out.println("Cache HIT: searchHangHoa - " + tuKhoa);
            return cached;
        }

        System.out.println("Cache MISS: searchHangHoa - " + tuKhoa + " - querying database");
        List<HangHoa> list = new ArrayList<>();
        String sql = "SELECT h.*, l.TenLoai, v.TenViTri FROM HangHoa h "
                   + "LEFT JOIN LoaiHangHoa l ON h.MaLoai = l.MaLoai "
                   + "LEFT JOIN ViTriKho v ON h.MaViTri = v.MaViTri "
                   + "WHERE h.TenHang LIKE ? OR h.MaHang LIKE ? OR l.TenLoai LIKE ? OR v.TenViTri LIKE ? OR h.QuyCach LIKE ? "
                   + "ORDER BY h.MaHang DESC";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + tuKhoa + "%");
            ps.setString(2, "%" + tuKhoa + "%");
            ps.setString(3, "%" + tuKhoa + "%");
            ps.setString(4, "%" + tuKhoa + "%");
            ps.setString(5, "%" + tuKhoa + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new HangHoa(
                        rs.getString("MaHang"),
                        rs.getString("TenHang"),
                        rs.getString("DVT"),
                        rs.getInt("SoLuongTonKho"),
                        rs.getString("QuyCach"),
                        rs.getInt("HanMucTonToiThieu"),
                        rs.getString("MaLoai"),
                        rs.getString("MaViTri"),
                        rs.getString("TenLoai"),
                        rs.getString("TenViTri")
                    ));
                }
            }

            CacheService.cacheHangHoaSearch(tuKhoa, list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Bypass Redis and propagate SQL failures: never publish an empty/partial snapshot on failure.
    private List<HangHoa> loadForSearchIndex() throws Exception {
        List<HangHoa> result = new ArrayList<>();
        String sql = "SELECT h.*, l.TenLoai, v.TenViTri FROM HangHoa h "
                + "LEFT JOIN LoaiHangHoa l ON h.MaLoai=l.MaLoai "
                + "LEFT JOIN ViTriKho v ON h.MaViTri=v.MaViTri";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(new HangHoa(rs.getString("MaHang"), rs.getString("TenHang"),
                    rs.getString("DVT"), rs.getInt("SoLuongTonKho"), rs.getString("QuyCach"),
                    rs.getInt("HanMucTonToiThieu"), rs.getString("MaLoai"), rs.getString("MaViTri"),
                    rs.getString("TenLoai"), rs.getString("TenViTri")));
        }
        return result;
    }
}
