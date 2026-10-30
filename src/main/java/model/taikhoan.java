package model;

public class taikhoan {
    private String tenDangNhap;
    private String hoTen;
    private String vaiTro;
    private String trangThai;
 // 1. Constructor k tham số
    public taikhoan() {
    }

    // 2. Constructor đầy đủ
    public taikhoan(String tenDangNhap, String hoTen, String vaiTro, String trangThai) {
        this.tenDangNhap = tenDangNhap;
        this.hoTen = hoTen;
        this.vaiTro = vaiTro;
        this.trangThai = trangThai;
    }

    // --- CÁC HÀM GETTER ---
    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public String getHoTen() {
        return hoTen;
    }

    public String getVaiTro() {
        return vaiTro;
    }

    public String getTrangThai() {
        return trangThai;
    }

    // --- CÁC HÀM SETTER 
    public void setTenDangNhap(String tenDangNhap) {
        this.tenDangNhap = tenDangNhap;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public void setVaiTro(String vaiTro) {
        this.vaiTro = vaiTro;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}