package model;

import java.io.Serializable;

public class ProductRecommendationDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String maHang;
    private String tenHang;
    private String dvt;
    private long soLanXuatCung;

    public ProductRecommendationDTO() {}

    public ProductRecommendationDTO(String maHang, String tenHang, String dvt, long soLanXuatCung) {
        this.maHang = maHang;
        this.tenHang = tenHang;
        this.dvt = dvt;
        this.soLanXuatCung = soLanXuatCung;
    }

    public String getMaHang() { return maHang; }
    public void setMaHang(String maHang) { this.maHang = maHang; }

    public String getTenHang() { return tenHang; }
    public void setTenHang(String tenHang) { this.tenHang = tenHang; }

    public String getDvt() { return dvt; }
    public void setDvt(String dvt) { this.dvt = dvt; }

    public long getSoLanXuatCung() { return soLanXuatCung; }
    public void setSoLanXuatCung(long soLanXuatCung) { this.soLanXuatCung = soLanXuatCung; }
}

