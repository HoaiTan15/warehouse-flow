package model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TransactionFeedItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private String maPhieu;
    private String loaiPhieu; // "NHAP" hoặc "XUAT"
    private String nguoiThucHien;
    private double tongTien;
    private int soLuongMatHang;
    private String thoiGian; // "yyyy-MM-dd HH:mm:ss"
    private String ghiChu;

    public TransactionFeedItem() {
        this.thoiGian = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    public TransactionFeedItem(String maPhieu, String loaiPhieu, String nguoiThucHien,
                               double tongTien, int soLuongMatHang, String thoiGian, String ghiChu) {
        this.maPhieu = maPhieu;
        this.loaiPhieu = loaiPhieu;
        this.nguoiThucHien = nguoiThucHien;
        this.tongTien = tongTien;
        this.soLuongMatHang = soLuongMatHang;
        this.thoiGian = thoiGian != null ? thoiGian : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        this.ghiChu = ghiChu;
    }

    public String getMaPhieu() { return maPhieu; }
    public void setMaPhieu(String maPhieu) { this.maPhieu = maPhieu; }

    public String getLoaiPhieu() { return loaiPhieu; }
    public void setLoaiPhieu(String loaiPhieu) { this.loaiPhieu = loaiPhieu; }

    public String getNguoiThucHien() { return nguoiThucHien; }
    public void setNguoiThucHien(String nguoiThucHien) { this.nguoiThucHien = nguoiThucHien; }

    public double getTongTien() { return tongTien; }
    public void setTongTien(double tongTien) { this.tongTien = tongTien; }

    public int getSoLuongMatHang() { return soLuongMatHang; }
    public void setSoLuongMatHang(int soLuongMatHang) { this.soLuongMatHang = soLuongMatHang; }

    public String getThoiGian() { return thoiGian; }
    public void setThoiGian(String thoiGian) { this.thoiGian = thoiGian; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}

