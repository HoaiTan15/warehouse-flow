package model;

import java.sql.Date;

public class PhieuNhapDTO {
    private String maPhieu;
    private Date ngayLap;
    private String tenNCC;
    private String tenHang;
    private int soLuong;
    private double donGia;
    private String maNguoiDuyet;
    private String ghiChu;
    
    public PhieuNhapDTO() {}

    public PhieuNhapDTO(String maPhieu, Date ngayLap, String tenNCC, String tenHang, int soLuong, double donGia, String maNguoiDuyet, String ghiChu) {
        this.maPhieu = maPhieu;
        this.ngayLap = ngayLap;
        this.tenNCC = tenNCC;
        this.tenHang = tenHang;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.maNguoiDuyet = maNguoiDuyet;
        this.ghiChu = ghiChu;
    }

    public String getMaPhieu() { return maPhieu; }
    public void setMaPhieu(String maPhieu) { this.maPhieu = maPhieu; }

    public Date getNgayLap() { return ngayLap; }
    public void setNgayLap(Date ngayLap) { this.ngayLap = ngayLap; }

    public String getTenNCC() { return tenNCC; }
    public void setTenNCC(String tenNCC) { this.tenNCC = tenNCC; }

    public String getTenHang() { return tenHang; }
    public void setTenHang(String tenHang) { this.tenHang = tenHang; }

    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }

    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { this.donGia = donGia; }

    public String getMaNguoiDuyet() { return maNguoiDuyet; }
    public void setMaNguoiDuyet(String maNguoiDuyet) { this.maNguoiDuyet = maNguoiDuyet; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}