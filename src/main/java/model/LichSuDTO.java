package model;

import java.sql.Date;

public class LichSuDTO {
    private Date thoiGian;
    private String maHang;
    private String tenHang;
    private String loai;
    private int thayDoi;
    private int tonSau;
    private String chungTu;
    private String ghiChu;

    public LichSuDTO() {}

    public LichSuDTO(Date thoiGian, String maHang, String tenHang, String loai, int thayDoi, int tonSau, String chungTu, String ghiChu) {
        this.thoiGian = thoiGian;
        this.maHang = maHang;
        this.tenHang = tenHang;
        this.loai = loai;
        this.thayDoi = thayDoi;
        this.tonSau = tonSau;
        this.chungTu = chungTu;
        this.ghiChu = ghiChu;
    }

    // Các hàm Getter (Tạo nhanh bằng Insert Code)
    public Date getThoiGian() { return thoiGian; }
    public String getMaHang() { return maHang; }
    public String getTenHang() { return tenHang; }
    public String getLoai() { return loai; }
    public int getThayDoi() { return thayDoi; }
    public int getTonSau() { return tonSau; }
    public String getChungTu() { return chungTu; }
    public String getGhiChu() { return ghiChu; }
}