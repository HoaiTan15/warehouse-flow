package model;

import java.sql.Date;

public class PhieuKiemKe {
    private String maKiemKe;
    private Date ngayKiem;
    private String maNV; // Tên hoặc Mã nhân viên lập phiếu
    private String trangThai; 

    public PhieuKiemKe() {}
    public PhieuKiemKe(String maKiemKe, Date ngayKiem, String maNV, String trangThai) {
        this.maKiemKe = maKiemKe;
        this.ngayKiem = ngayKiem;
        this.maNV = maNV;
        this.trangThai = trangThai;
    }

    // Getter và Setter
    public String getMaKiemKe() { return maKiemKe; }
    public Date getNgayKiem() { return ngayKiem; }
    public String getMaNV() { return maNV; }
    public String getTrangThai() { return trangThai; }
}