package model;

import java.sql.Date;

public class PhieuXuatKho {
    private String maPhieu;
    private Date ngayLap;
    private String maDeNghi;
    private String boPhanNhan;
    private String mucDich;

    // Constructor rỗng
    public PhieuXuatKho() {}

    // Constructor đầy đủ
    public PhieuXuatKho(String maPhieu, Date ngayLap, String maDeNghi, String boPhanNhan, String mucDich) {
        this.maPhieu = maPhieu;
        this.ngayLap = ngayLap;
        this.maDeNghi = maDeNghi;
        this.boPhanNhan = boPhanNhan;
        this.mucDich = mucDich;
    }

    // Các hàm Getter và Setter
    public String getMaPhieu() { return maPhieu; }
    public void setMaPhieu(String maPhieu) { this.maPhieu = maPhieu; }

    public Date getNgayLap() { return ngayLap; }
    public void setNgayLap(Date ngayLap) { this.ngayLap = ngayLap; }

    public String getMaDeNghi() { return maDeNghi; }
    public void setMaDeNghi(String maDeNghi) { this.maDeNghi = maDeNghi; }

    public String getBoPhanNhan() { return boPhanNhan; }
    public void setBoPhanNhan(String boPhanNhan) { this.boPhanNhan = boPhanNhan; }

    public String getMucDich() { return mucDich; }
    public void setMucDich(String mucDich) { this.mucDich = mucDich; }
}