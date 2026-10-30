package model;

public class CongNo {
    private String maCongNo;
    private String maPhieu;
    private String loaiPhieu;
    private double soTienPhaiTra;
    private double soTienDaTra;
    private String trangThai;

    public CongNo() {}
    public CongNo(String maCongNo, String maPhieu, String loaiPhieu, double soTienPhaiTra, double soTienDaTra, String trangThai) {
        this.maCongNo = maCongNo;
        this.maPhieu = maPhieu;
        this.loaiPhieu = loaiPhieu;
        this.soTienPhaiTra = soTienPhaiTra;
        this.soTienDaTra = soTienDaTra;
        this.trangThai = trangThai;
    }
    // Getter và Setter...
    public String getMaCongNo() { return maCongNo; }
    public String getMaPhieu() { return maPhieu; }
    public String getLoaiPhieu() { return loaiPhieu; }
    public double getSoTienPhaiTra() { return soTienPhaiTra; }
    public double getSoTienDaTra() { return soTienDaTra; }
    public String getTrangThai() { return trangThai; }
}