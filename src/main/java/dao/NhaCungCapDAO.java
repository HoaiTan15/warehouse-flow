package dao;

import model.NhaCungCap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class NhaCungCapDAO {
    
    // 1. LẤY DANH SÁCH
    public List<NhaCungCap> getAll() {
        List<NhaCungCap> list = new ArrayList<>();
        String sql = "SELECT * FROM NhaCungCap ORDER BY MaNCC DESC";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new NhaCungCap(rs.getString("MaNCC"), rs.getString("TenNCC"), 
                                        rs.getString("DiaChi"), rs.getString("SDT"), rs.getString("Email")));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // 2. KIỂM TRA TRÙNG TÊN (Dùng cho cả Thêm và Sửa)
    public boolean kiemTraTrungTen(String tenNCC, String maNCC_loaiTru) {
        String sql = "SELECT COUNT(*) FROM NhaCungCap WHERE TenNCC = ?";
        if (maNCC_loaiTru != null && !maNCC_loaiTru.isEmpty()) sql += " AND MaNCC != ?";
        
        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenNCC);
            if (maNCC_loaiTru != null && !maNCC_loaiTru.isEmpty()) ps.setString(2, maNCC_loaiTru);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // 3. THÊM MỚI (Bỏ trống MaNCC để SQL tự động sinh bằng Function fn_AutoMaNCC)
    public void themNCC(NhaCungCap ncc) {
        String sql = "INSERT INTO NhaCungCap (TenNCC, DiaChi, SDT, Email) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ncc.getTenNCC());
            ps.setString(2, ncc.getDiaChi());
            ps.setString(3, ncc.getSdt());
            ps.setString(4, ncc.getEmail());
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // 4. CẬP NHẬT
    public void suaNCC(NhaCungCap ncc) {
        String sql = "UPDATE NhaCungCap SET TenNCC=?, DiaChi=?, SDT=?, Email=? WHERE MaNCC=?";
        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ncc.getTenNCC());
            ps.setString(2, ncc.getDiaChi());
            ps.setString(3, ncc.getSdt());
            ps.setString(4, ncc.getEmail());
            ps.setString(5, ncc.getMaNCC());
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // 5. XÓA (Bắt lỗi khóa ngoại nếu NCC đã có Phiếu Nhập)
    public String xoaNCC(String maNCC) {
        String sql = "DELETE FROM NhaCungCap WHERE MaNCC=?";
        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNCC);
            ps.executeUpdate();
            return "SUCCESS";
        } catch (Exception e) { 
            return "LỖI: Không thể xóa vì Nhà Cung Cấp này đã có dữ liệu Phiếu Nhập Kho!"; 
        }
    }
}