package model;

import java.sql.Date;

public class KiemKeDTO {
    private String maKiemKe;
    private Date ngayKiem;
    private String tenHang;
    private int lyThuyet;
    private int thucTe;
    private int chenhLech;
    private String nguyenNhan;
    private String trangThai; 

    public KiemKeDTO() {}

    public KiemKeDTO(String maKiemKe, Date ngayKiem, String tenHang, int lyThuyet, int thucTe, int chenhLech, String nguyenNhan, String trangThai) {
        this.maKiemKe = maKiemKe;
        this.ngayKiem = ngayKiem;
        this.tenHang = tenHang;
        this.lyThuyet = lyThuyet;
        this.thucTe = thucTe;
        this.chenhLech = chenhLech;
        this.nguyenNhan = nguyenNhan;
        this.trangThai = trangThai;
    }

    // Các hàm Getter / Setter
    public String getMaKiemKe() { return maKiemKe; }
    public void setMaKiemKe(String maKiemKe) { this.maKiemKe = maKiemKe; }
    
    public Date getNgayKiem() { return ngayKiem; }
    public void setNgayKiem(Date ngayKiem) { this.ngayKiem = ngayKiem; }
    
    public String getTenHang() { return tenHang; }
    public void setTenHang(String tenHang) { this.tenHang = tenHang; }
    
    public int getLyThuyet() { return lyThuyet; }
    public void setLyThuyet(int lyThuyet) { this.lyThuyet = lyThuyet; }
    
    public int getThucTe() { return thucTe; }
    public void setThucTe(int thucTe) { this.thucTe = thucTe; }
    
    public int getChenhLech() { return chenhLech; }
    public void setChenhLech(int chenhLech) { this.chenhLech = chenhLech; }
    
    public String getNguyenNhan() { return nguyenNhan; }
    public void setNguyenNhan(String nguyenNhan) { this.nguyenNhan = nguyenNhan; }
    
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}