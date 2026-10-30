package model;

public class TaiKhoanDTO {
    private String tenDangNhap;
    private String matKhau;
    private String vaiTro;
    private String trangThai;
    private String maNV;
    private String hoTenNV; // Lấy từ bảng NhanVien

    public TaiKhoanDTO() {}

    public TaiKhoanDTO(String tenDangNhap, String matKhau, String vaiTro, String trangThai, String maNV, String hoTenNV) {
        this.tenDangNhap = tenDangNhap;
        this.matKhau = matKhau;
        this.vaiTro = vaiTro;
        this.trangThai = trangThai;
        this.maNV = maNV;
        this.hoTenNV = hoTenNV;
    }

    public String getTenDangNhap() { return tenDangNhap; }
    public String getMatKhau() { return matKhau; }
    public String getVaiTro() { return vaiTro; }
    public String getTrangThai() { return trangThai; }
    public String getMaNV() { return maNV; }
    public String getHoTenNV() { return hoTenNV; }
}