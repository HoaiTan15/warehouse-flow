package dao;

import model.TaiKhoanDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuanLyTaiKhoanDAO {

    // 1. LẤY DANH SÁCH TÀI KHOẢN KÈM TÊN NHÂN VIÊN
    public List<TaiKhoanDTO> getAllTaiKhoan() {
        List<TaiKhoanDTO> list = new ArrayList<>();
        String sql = "SELECT t.TenDangNhap, t.MatKhau, t.VaiTro, t.TrangThai, t.MaNV, n.HoTen "
                   + "FROM TaiKhoan t LEFT JOIN NhanVien n ON t.MaNV = n.MaNV";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new TaiKhoanDTO(
                    rs.getString("TenDangNhap"), rs.getString("MatKhau"),
                    rs.getString("VaiTro"), rs.getString("TrangThai"),
                    rs.getString("MaNV"), rs.getString("HoTen")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // 2. LẤY DANH SÁCH NHÂN VIÊN "CHƯA CÓ" TÀI KHOẢN (Để đổ vào Dropdown lúc Thêm Mới)
    public Map<String, String> getNhanVienChuaCoTaiKhoan() {
        Map<String, String> map = new HashMap<>();
        String sql = "SELECT MaNV, HoTen FROM NhanVien WHERE MaNV NOT IN (SELECT MaNV FROM TaiKhoan WHERE MaNV IS NOT NULL)";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("MaNV"), rs.getString("HoTen"));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return map;
    }

    // 3. KIỂM TRA TRÙNG TÊN ĐĂNG NHẬP
    public boolean kiemTraTrung(String tenDangNhap) {
        String sql = "SELECT COUNT(*) FROM TaiKhoan WHERE TenDangNhap = ?";
        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenDangNhap);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // 4. THÊM TÀI KHOẢN
    public void themTaiKhoan(String tenDN, String matKhau, String vaiTro, String maNV) {
        String sql = "INSERT INTO TaiKhoan (TenDangNhap, MatKhau, VaiTro, TrangThai, MaNV) VALUES (?, ?, ?, N'Hoạt động', ?)";
        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenDN); ps.setString(2, matKhau);
            ps.setString(3, vaiTro); ps.setString(4, maNV);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // 5. CẬP NHẬT TÀI KHOẢN (Mật khẩu, Vai trò, Trạng thái)
    public void suaTaiKhoan(String tenDN, String matKhau, String vaiTro, String trangThai) {
        String sql = "UPDATE TaiKhoan SET MatKhau=?, VaiTro=?, TrangThai=? WHERE TenDangNhap=?";
        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matKhau); ps.setString(2, vaiTro);
            ps.setString(3, trangThai); ps.setString(4, tenDN);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // 6. XÓA TÀI KHOẢN
    public void xoaTaiKhoan(String tenDN) {
        String sql = "DELETE FROM TaiKhoan WHERE TenDangNhap=?";
        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenDN);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }
}