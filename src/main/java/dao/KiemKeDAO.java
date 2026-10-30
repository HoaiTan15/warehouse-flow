package dao;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.sql.*;
import model.PhieuKiemKe;
public class KiemKeDAO {
    public String luuPhieuKiemKe(String maKiemKe, Date ngayKiem, String tenDangNhap, 
                                 String[] maHangArr, String[] thucTeArr, String[] chenhLechArr, String[] nguyenNhanArr) {
        Connection conn = null;
        try {
            conn = DBConnect.getConnection();
            conn.setAutoCommit(false);

            String sqlPhieu = "INSERT INTO PhieuKiemKeHangHoa (MaKiemKe, NgayKiem, MaNV, TrangThai) "
                            + "VALUES (?, ?, (SELECT MaNV FROM TaiKhoan WHERE TenDangNhap = ?), N'Chờ duyệt')";
            try (PreparedStatement ps = conn.prepareStatement(sqlPhieu)) {
                ps.setString(1, maKiemKe);
                ps.setDate(2, ngayKiem);
                ps.setString(3, tenDangNhap);
                ps.executeUpdate();
            }

            String sqlChiTiet = "INSERT INTO ChiTietPhieuKiemKe (MaKiemKe, MaHang, SoLuongThucTe, ChenhLech, NguyenNhan) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement psChiTiet = conn.prepareStatement(sqlChiTiet)) {
                for (int i = 0; i < maHangArr.length; i++) {
                    psChiTiet.setString(1, maKiemKe);
                    psChiTiet.setString(2, maHangArr[i]);
                    psChiTiet.setInt(3, Integer.parseInt(thucTeArr[i]));
                    psChiTiet.setInt(4, Integer.parseInt(chenhLechArr[i]));
                    psChiTiet.setString(5, (nguyenNhanArr[i] == null || nguyenNhanArr[i].isEmpty()) ? "Không chênh lệch" : nguyenNhanArr[i]);
                    psChiTiet.executeUpdate();
                }
            }
            conn.commit();
            return "SUCCESS";
        } catch (Exception e) {
            if (conn != null) try { conn.rollback(); } catch (Exception ex) {}
            return "Lỗi: " + e.getMessage();
        } finally {
            if (conn != null) try { conn.close(); } catch (Exception e) {}
        }
    }
    public List<PhieuKiemKe> getPhieuKiemKeChoDuyet() {
    List<PhieuKiemKe> list = new ArrayList<>();
    String sql = "SELECT * FROM PhieuKiemKeHangHoa WHERE TrangThai = N'Chờ duyệt'";
    try (Connection conn = DBConnect.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
            list.add(new PhieuKiemKe(
                rs.getString("MaKiemKe"),
                rs.getDate("NgayKiem"),
                rs.getString("MaNV"),
                rs.getString("TrangThai")
            ));
        }
    } catch (Exception e) { 
        e.printStackTrace(); 
    }
    return list;
}
}