package dao;

import model.PhieuXuatKho;
import model.PhieuXuatDTO;
import nosql.mongo.NhatKyThaoTacRepository;
import nosql.redis.CacheService;

import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
            if (maHangArr == null || soLuongArr == null || donGiaArr == null
                    || maHangArr.length == 0 || maHangArr.length != soLuongArr.length
                    || maHangArr.length != donGiaArr.length) {
                return "Danh sách mặt hàng xuất không hợp lệ.";
            }
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            conn = DBConnect.getConnection();
            
            // TẮT AUTO COMMIT để gộp chung 2 thao tác (Lưu phiếu và Lưu chi tiết) vào 1 Giao dịch
            conn.setAutoCommit(false);
            px.setMaPhieu(taoMaPhieu(conn));

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

            // Trừ tồn và lưu chi tiết trên cùng connection/database với phiếu xuất.
            double tongTien = 0.0;
            String sqlTruTon = "UPDATE HangHoa SET SoLuongTonKho = SoLuongTonKho - ? "
                    + "WHERE MaHang = ? AND SoLuongTonKho >= ?";
            String sqlChiTiet = "INSERT INTO ChiTietPhieuXuatKho (MaPhieu, MaHang, SoLuong, DonGia) VALUES (?, ?, ?, ?)";
            try (PreparedStatement truTon = conn.prepareStatement(sqlTruTon);
                 PreparedStatement chiTiet = conn.prepareStatement(sqlChiTiet)) {
                for (int i = 0; i < maHangArr.length; i++) {
                    int soLuong = Integer.parseInt(soLuongArr[i]);
                    double donGia = Double.parseDouble(donGiaArr[i]);
                    if (maHangArr[i] == null || maHangArr[i].trim().isEmpty() || soLuong <= 0
                            || !Double.isFinite(donGia) || donGia < 0) {
                        throw new IllegalArgumentException("Mã hàng, số lượng hoặc đơn giá không hợp lệ.");
                    }
                    tongTien += soLuong * donGia;

                    truTon.setInt(1, soLuong);
                    truTon.setString(2, maHangArr[i]);
                    truTon.setInt(3, soLuong);
                    if (truTon.executeUpdate() != 1)
                        throw new IllegalArgumentException("Mặt hàng " + maHangArr[i] + " không tồn tại hoặc không đủ tồn kho.");

                    chiTiet.setString(1, px.getMaPhieu());
                    chiTiet.setString(2, maHangArr[i]);
                    chiTiet.setInt(3, soLuong);
                    chiTiet.setDouble(4, donGia);
                    if (chiTiet.executeUpdate() != 1)
                        throw new SQLException("Không lưu được chi tiết phiếu xuất.");
                }
            }

            // Mọi thứ hoàn hảo -> Xác nhận lưu
            conn.commit();

            CacheService.invalidateHangHoaCache();

            // Đẩy giao dịch xuất kho vào Redis Stream và cập nhật thống kê ngày
            try {
                new nosql.redis.WarehouseRedisService().pushTransaction(new model.TransactionFeedItem(
                    px.getMaPhieu(), "XUAT", tenDangNhapThuKho, tongTien,
                    maHangArr != null ? maHangArr.length : 0, null,
                    "Xuất kho cho: " + (px.getBoPhanNhan() != null ? px.getBoPhanNhan() : "") +
                    (px.getMucDich() != null && !px.getMucDich().isEmpty() ? " - " + px.getMucDich() : "")
                ));
            } catch (Exception redisEx) {
                System.err.println("Redis pushTransaction failed: " + redisEx.getMessage());
            }

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
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException rollbackError) { e.addSuppressed(rollbackError); }
            }
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

    public String maPhieuTiepTheo() throws Exception {
        try (Connection conn = DBConnect.getConnection()) {
            return taoMaPhieu(conn);
        }
    }

    private String taoMaPhieu(Connection conn) throws SQLException {
        int max = 0;
        String sql = "SELECT MaPhieu FROM PhieuXuatKho WITH (UPDLOCK, HOLDLOCK)";
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String ma = rs.getString(1);
                if (ma != null && ma.matches("PX[0-9]{3}"))
                    max = Math.max(max, Integer.parseInt(ma.substring(2)));
            }
        }
        if (max >= 999) throw new SQLException("Đã hết mã phiếu xuất PX001-PX999");
        return String.format(Locale.ROOT, "PX%03d", max + 1);
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
