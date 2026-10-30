package dao;

import model.PhieuKiemKe;
import nosql.mongo.NhatKyThaoTacRepository;
import nosql.redis.CacheService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.KiemKeDTO;

public class KiemKeDuyetDAO {
    private NhatKyThaoTacRepository nhatKyRepo;

    public KiemKeDuyetDAO() {
        try {
            this.nhatKyRepo = new NhatKyThaoTacRepository();
        } catch (Exception e) {
            System.err.println("Failed to initialize NhatKyThaoTacRepository: " + e.getMessage());
        }
    }

    // =====================================================================
    // Lấy danh sách phiếu kiểm kê đang chờ xét duyệt (TrangThai = 'Chờ duyệt')
    // =====================================================================
    public List<PhieuKiemKe> getDanhSachChoXetDuyet() {
        List<PhieuKiemKe> list = new ArrayList<>();
        String sql = "SELECT MaKiemKe, NgayKiem, MaNV, TrangThai "
                   + "FROM PhieuKiemKeHangHoa "
                   + "WHERE TrangThai = N'Chờ duyệt' "
                   + "ORDER BY NgayKiem DESC";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                PhieuKiemKe p = new PhieuKiemKe(
                    rs.getString("MaKiemKe"),
                    rs.getDate("NgayKiem"),
                    rs.getString("MaNV"),
                    rs.getString("TrangThai")
                );
                list.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // =====================================================================
    // Duyệt phiếu: Gọi stored procedure xử lý chênh lệch + cập nhật trạng thái
    // =====================================================================
    public void duyetPhieuKiemKe(String maKiemKe) {
        try (Connection conn = DBConnect.getConnection()) {
            // Bước 1: Gọi stored procedure cập nhật tồn kho
            try (CallableStatement cs = conn.prepareCall("{CALL SP_XuLyChenhLechKiemKe(?)}")) {
                cs.setString(1, maKiemKe);
                cs.executeUpdate();
            }
            // Bước 2: Cập nhật trạng thái thành "Đã duyệt", xoá lý do cũ
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE PhieuKiemKeHangHoa SET TrangThai = N'Đã duyệt', LyDo = NULL WHERE MaKiemKe = ?")) {
                ps.setString(1, maKiemKe);
                ps.executeUpdate();
            }

            CacheService.invalidateHangHoaCache();

            if (nhatKyRepo != null) {
                nhatKyRepo.ghiNhatKy("DUYET_KIEM_KE", maKiemKe, "system",
                    "THANH_CONG", "Duyệt phiếu kiểm kê: " + maKiemKe);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (nhatKyRepo != null) {
                nhatKyRepo.ghiNhatKy("DUYET_KIEM_KE", maKiemKe, "system",
                    "THAT_BAI", "Lỗi: " + e.getMessage());
            }
        }
    }

    // =====================================================================
    // Từ chối / Yêu cầu kiểm tra lại: Lưu trạng thái và lý do
    // =====================================================================
    public void capNhatTrangThaiPhieu(String ma, String trangThai, String lyDo) {
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "UPDATE PhieuKiemKeHangHoa SET TrangThai = ?, LyDo = ? WHERE MaKiemKe = ?")) {
            ps.setString(1, trangThai);
            ps.setString(2, lyDo);
            ps.setString(3, ma);
            ps.executeUpdate();

            if (nhatKyRepo != null) {
                nhatKyRepo.ghiNhatKy("CAP_NHAT_TRANG_THAI_KIEM_KE", ma, "system",
                    "THANH_CONG", "Cập nhật trạng thái: " + trangThai + " - Lý do: " + lyDo);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (nhatKyRepo != null) {
                nhatKyRepo.ghiNhatKy("CAP_NHAT_TRANG_THAI_KIEM_KE", ma, "system",
                    "THAT_BAI", "Lỗi: " + e.getMessage());
            }
        }
    }
    
    public List<KiemKeDTO> getChiTietPhieu(String maKiemKe) {
    List<KiemKeDTO> list = new ArrayList<>();
    String sql = "SELECT h.TenHang, ct.SoLuongThucTe, ct.NguyenNhan " +
                 "FROM ChiTietPhieuKiemKe ct " +
                 "JOIN HangHoa h ON ct.MaHang = h.MaHang " +
                 "WHERE ct.MaKiemKe = ?";
    try (Connection conn = DBConnect.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, maKiemKe);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            // Lưu ý: Dùng constructor hoặc setter phù hợp với class KiemKeDTO của bạn
            KiemKeDTO dto = new KiemKeDTO();
            dto.setTenHang(rs.getString("TenHang"));
            dto.setThucTe(rs.getInt("SoLuongThucTe"));
            dto.setNguyenNhan(rs.getString("NguyenNhan"));
            list.add(dto);
        }
    } catch (Exception e) { e.printStackTrace(); }
    return list;
}
 
public boolean capNhatTrangThai(String maKiemKe, String trangThai) {
    String sql = "UPDATE PhieuKiemKeHangHoa SET TrangThai = ? WHERE MaKiemKe = ?";
    try (Connection conn = DBConnect.getConnection(); 
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, trangThai);
        ps.setString(2, maKiemKe);
        return ps.executeUpdate() > 0;
    } catch (Exception e) {
        e.printStackTrace();
    }
    return false;
}
}