package dao;

import model.CongNo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CongNoDAO {
    
    // 1. Lấy toàn bộ danh sách công nợ để kế toán theo dõi
    public List<CongNo> getDanhSachCongNo() {
        List<CongNo> list = new ArrayList<>();
        // Query lấy thông tin từ bảng CongNo bạn vừa tạo
        String sql = "SELECT * FROM CongNo ORDER BY MaCongNo DESC";
        
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                list.add(new CongNo(
                    rs.getString("MaCongNo"),
                    rs.getString("MaPhieu"),
                    rs.getString("LoaiPhieu"),
                    rs.getDouble("SoTienPhaiTra"),
                    rs.getDouble("SoTienDaTra"),
                    rs.getString("TrangThai")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // 2. Hàm Kế toán ấn "Xác nhận trả tiền"
    public void thanhToan(String maCongNo, double soTienTra) {
        // Cập nhật số tiền đã trả và chuyển trạng thái sang "Hoàn tất"
        String sql = "UPDATE CongNo SET SoTienDaTra = SoTienDaTra + ?, "
                   + "TrangThai = CASE WHEN (SoTienDaTra + ?) >= SoTienPhaiTra THEN N'Hoàn tất' ELSE N'Thanh toán một phần' END "
                   + "WHERE MaCongNo = ?";
                   
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, soTienTra);
            ps.setDouble(2, soTienTra);
            ps.setString(3, maCongNo);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}