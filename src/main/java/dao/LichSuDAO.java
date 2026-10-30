package dao;

import model.LichSuDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class LichSuDAO {

    public List<LichSuDTO> getLichSuBienDong(Connection conn, String maHangLoc) {
        List<LichSuDTO> list = new ArrayList<>();
        
        // Dùng CTE gom Nhập, Xuất, Kiểm Kê lại. Sau đó dùng SUM() OVER để tính dồn Tồn Sau
        String sql = 
            "WITH HistoryCTE AS (" +
            "    SELECT pn.NgayLap AS ThoiGian, ct.MaHang, hh.TenHang, N'Nhập' AS Loai, ct.SoLuong AS ThayDoi, pn.MaPhieu AS ChungTu, pn.GhiChu " +
            "    FROM PhieuNhapKho pn JOIN ChiTietPhieuNhapKho ct ON pn.MaPhieu = ct.MaPhieu JOIN HangHoa hh ON ct.MaHang = hh.MaHang " +
            "    WHERE pn.MaNguoiDuyet IS NOT NULL " + // Chỉ lấy phiếu đã duyệt
            "    UNION ALL " +
            "    SELECT px.NgayLap AS ThoiGian, ct.MaHang, hh.TenHang, N'Xuất' AS Loai, -ct.SoLuong AS ThayDoi, px.MaPhieu AS ChungTu, px.MucDich AS GhiChu " +
            "    FROM PhieuXuatKho px JOIN ChiTietPhieuXuatKho ct ON px.MaPhieu = ct.MaPhieu JOIN HangHoa hh ON ct.MaHang = hh.MaHang " +
            "    WHERE px.MaNguoiDuyet IS NOT NULL " +
            "    UNION ALL " +
            "    SELECT pk.NgayKiem AS ThoiGian, ct.MaHang, hh.TenHang, N'Kiểm kê' AS Loai, ct.ChenhLech AS ThayDoi, pk.MaKiemKe AS ChungTu, ct.NguyenNhan AS GhiChu " +
            "    FROM PhieuKiemKeHangHoa pk JOIN ChiTietPhieuKiemKe ct ON pk.MaKiemKe = ct.MaKiemKe JOIN HangHoa hh ON ct.MaHang = hh.MaHang " +
            "    WHERE ct.ChenhLech <> 0" +
            ") " +
            "SELECT * FROM ( " +
            "    SELECT ThoiGian, MaHang, TenHang, Loai, ThayDoi, ChungTu, GhiChu, " +
            "           SUM(ThayDoi) OVER (PARTITION BY MaHang ORDER BY ThoiGian ASC, ChungTu ASC) AS TonSau " +
            "    FROM HistoryCTE " +
            ") AS ResultTable WHERE 1=1 ";

        if (maHangLoc != null && !maHangLoc.trim().isEmpty()) {
            sql += " AND MaHang = ? ";
        }
        
        sql += " ORDER BY ThoiGian DESC, ChungTu DESC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (maHangLoc != null && !maHangLoc.trim().isEmpty()) {
                ps.setString(1, maHangLoc);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new LichSuDTO(
                        rs.getDate("ThoiGian"), rs.getString("MaHang"), rs.getString("TenHang"),
                        rs.getString("Loai"), rs.getInt("ThayDoi"), rs.getInt("TonSau"),
                        rs.getString("ChungTu"), rs.getString("GhiChu")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}