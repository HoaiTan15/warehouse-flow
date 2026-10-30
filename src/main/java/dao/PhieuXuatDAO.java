package dao;

import model.PhieuXuatKho;
import model.PhieuXuatDTO;
import nosql.mongo.NhatKyThaoTacRepository;
import nosql.redis.CacheService;

import java.sql.CallableStatement;
import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PhieuXuatDAO {

    private NhatKyThaoTacRepository nhatKyRepo;

    public PhieuXuatDAO() {
        try {
            this.nhatKyRepo = new NhatKyThaoTacRepository();
        } catch (Exception e) {
            System.err.println("Failed to initialize NhatKyThaoTacRepository: " + e.getMessage());
        }
    }

    // ==============================================================================
    // HÀM 1: LƯU PHIẾU XUẤT MỚI (Dùng cho trang lapphieuxuat.jsp)
    // Dùng Transaction để đảm bảo nếu kho hết hàng thì Rollback, không lưu phiếu
    // ==============================================================================
    public String luuPhieuXuatMoi(PhieuXuatKho px, String tenDangNhapThuKho, String[] maHangArr, String[] soLuongArr, String[] donGiaArr) {
        Connection conn = null;
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            conn = DBConnect.getConnection();
            
            // TẮT AUTO COMMIT để gộp chung 2 thao tác (Lưu phiếu và Lưu chi tiết) vào 1 Giao dịch
            conn.setAutoCommit(false);

            // 1. LƯU THÔNG TIN CHUNG VÀO BẢNG PhieuXuatKho (Trạng thái chờ duyệt -> MaNguoiDuyet = NULL)
            String sqlPhieu = "INSERT INTO PhieuXuatKho (MaPhieu, NgayLap, MaNV, MaDeNghi, BoPhanNhan, MucDich, MaNguoiDuyet) "
                            + "VALUES (?, ?, (SELECT MaNV FROM TaiKhoan WHERE TenDangNhap = ?), ?, ?, ?, NULL)";
            
            try (PreparedStatement ps = conn.prepareStatement(sqlPhieu)) {
                ps.setString(1, px.getMaPhieu());
                ps.setDate(2, px.getNgayLap());
                ps.setString(3, tenDangNhapThuKho); 
                
                if (px.getMaDeNghi() == null || px.getMaDeNghi().trim().isEmpty()) {
                    ps.setNull(4, java.sql.Types.VARCHAR);
                } else {
                    ps.setString(4, px.getMaDeNghi());
                }
                
                ps.setString(5, px.getBoPhanNhan());
                ps.setString(6, px.getMucDich());
                ps.executeUpdate();
            }

            // 2. LƯU CHI TIẾT & TRỪ TỒN KHO BẰNG PROCEDURE
            String sqlChiTiet = "{CALL SP_LapChiTietPhieuXuat(?, ?, ?, ?)}";
            try (CallableStatement cs = conn.prepareCall(sqlChiTiet)) {
                for (int i = 0; i < maHangArr.length; i++) {
                    cs.setString(1, px.getMaPhieu());
                    cs.setString(2, maHangArr[i]);
                    cs.setInt(3, Integer.parseInt(soLuongArr[i]));
                    cs.setDouble(4, Double.parseDouble(donGiaArr[i]));
                    
                    // Thực thi Procedure. Nếu thiếu hàng, nó sẽ văng lỗi RAISERROR tại đây
                    cs.executeUpdate(); 
                }
            }

            // Mọi thứ hoàn hảo -> Xác nhận lưu
            conn.commit();

            CacheService.invalidateHangHoaCache();

            if (nhatKyRepo != null) {
                nhatKyRepo.ghiNhatKy("LAP_PHIEU_XUAT", px.getMaPhieu(), tenDangNhapThuKho,
                    "THANH_CONG", "Lập phiếu xuất: " + px.getMaPhieu() + " - Bộ phận: " + px.getBoPhanNhan());
            }

            return "SUCCESS";

        } catch (SQLException e) {
            // Có lỗi (như thiếu hàng) -> Quay xe hủy toàn bộ thao tác
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            if (nhatKyRepo != null) {
                nhatKyRepo.ghiNhatKy("LAP_PHIEU_XUAT", px.getMaPhieu(), tenDangNhapThuKho,
                    "THAT_BAI", "Lỗi: " + e.getMessage());
            }
            return e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            if (nhatKyRepo != null) {
                nhatKyRepo.ghiNhatKy("LAP_PHIEU_XUAT", px.getMaPhieu(), tenDangNhapThuKho,
                    "THAT_BAI", "Lỗi hệ thống: " + e.getMessage());
            }
            return "Lỗi hệ thống: " + e.getMessage();
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    // ==============================================================================
    // HÀM 2: LẤY DANH SÁCH PHIẾU XUẤT (Dùng cho trang quanlyxuatkho.jsp)
    // Kết nối 3 bảng để lấy đủ thông tin: Mã phiếu, Ngày lập, Tên hàng, Số lượng, ...
    // ==============================================================================
   public List<model.PhieuXuatDTO> getDanhSachPhieuXuat(String tuKhoa) {
        List<model.PhieuXuatDTO> list = new ArrayList<>();
        
        String sql = "SELECT px.MaPhieu, px.NgayLap, px.BoPhanNhan, hh.TenHang, ct.SoLuong, ct.DonGia, px.MaNguoiDuyet, px.MucDich "
                   + "FROM PhieuXuatKho px "
                   + "JOIN ChiTietPhieuXuatKho ct ON px.MaPhieu = ct.MaPhieu "
                   + "JOIN HangHoa hh ON ct.MaHang = hh.MaHang ";
        
        if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
            sql += "WHERE hh.TenHang LIKE ? OR px.MaPhieu LIKE ? ";
        }
        
        sql += "ORDER BY px.NgayLap DESC, px.MaPhieu DESC";

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            try (Connection conn = DBConnect.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                
                if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
                    ps.setString(1, "%" + tuKhoa + "%");
                    ps.setString(2, "%" + tuKhoa + "%");
                }
                
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        model.PhieuXuatDTO dto = new model.PhieuXuatDTO(
                            rs.getString("MaPhieu"),
                            rs.getDate("NgayLap"),
                            rs.getString("BoPhanNhan"),
                            rs.getString("TenHang"),
                            rs.getInt("SoLuong"),
                            rs.getDouble("DonGia"),
                            rs.getString("MaNguoiDuyet"),
                            rs.getString("MucDich")     
                        );
                        list.add(dto);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    
    // LẤY DANH SÁCH PHIẾU XUẤT CHO TRƯỞNG KHO
    public List<model.PhieuXuatDTO> getDanhSachPhieuXuat() {
        List<model.PhieuXuatDTO> list = new ArrayList<>();
        String sql = "SELECT px.MaPhieu, px.NgayLap, px.BoPhanNhan, hh.TenHang, ct.SoLuong, ct.DonGia, px.MaNguoiDuyet, px.MucDich "
                   + "FROM PhieuXuatKho px "
                   + "JOIN ChiTietPhieuXuatKho ct ON px.MaPhieu = ct.MaPhieu "
                   + "JOIN HangHoa hh ON ct.MaHang = hh.MaHang "
                   + "ORDER BY px.NgayLap DESC, px.MaPhieu DESC";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new model.PhieuXuatDTO(
                    rs.getString("MaPhieu"), rs.getDate("NgayLap"), rs.getString("BoPhanNhan"),
                    rs.getString("TenHang"), rs.getInt("SoLuong"), rs.getDouble("DonGia"),
                    rs.getString("MaNguoiDuyet"), rs.getString("MucDich")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // DUYỆT PHIẾU XUẤT
    public void duyetPhieu(String maPhieu, String tenDangNhapNguoiDuyet) {
        String sql = "UPDATE PhieuXuatKho SET MaNguoiDuyet = (SELECT MaNV FROM TaiKhoan WHERE TenDangNhap = ?) WHERE MaPhieu = ?";
        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenDangNhapNguoiDuyet);
            ps.setString(2, maPhieu);
            ps.executeUpdate();

            CacheService.invalidateHangHoaCache();

            if (nhatKyRepo != null) {
                nhatKyRepo.ghiNhatKy("DUYET_PHIEU_XUAT", maPhieu, tenDangNhapNguoiDuyet,
                    "THANH_CONG", "Duyệt phiếu xuất: " + maPhieu);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (nhatKyRepo != null) {
                nhatKyRepo.ghiNhatKy("DUYET_PHIEU_XUAT", maPhieu, tenDangNhapNguoiDuyet,
                    "THAT_BAI", "Lỗi: " + e.getMessage());
            }
        }
    }

    // TỪ CHỐI PHIẾU XUẤT
    public void tuChoiPhieu(String maPhieu, String tenDangNhapNguoiDuyet, String lyDo) {
        String sql = "UPDATE PhieuXuatKho SET MaNguoiDuyet = (SELECT MaNV FROM TaiKhoan WHERE TenDangNhap = ?), MucDich = ? WHERE MaPhieu = ?";
        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenDangNhapNguoiDuyet);
            ps.setString(2, lyDo);
            ps.setString(3, maPhieu);
            ps.executeUpdate();

            if (nhatKyRepo != null) {
                nhatKyRepo.ghiNhatKy("TU_CHOI_PHIEU_XUAT", maPhieu, tenDangNhapNguoiDuyet,
                    "THANH_CONG", "Từ chối phiếu xuất: " + maPhieu + " - Lý do: " + lyDo);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (nhatKyRepo != null) {
                nhatKyRepo.ghiNhatKy("TU_CHOI_PHIEU_XUAT", maPhieu, tenDangNhapNguoiDuyet,
                    "THAT_BAI", "Lỗi: " + e.getMessage());
            }
        }
    }
}