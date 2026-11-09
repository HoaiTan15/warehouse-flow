package model;

import java.io.Serializable;

public class DailyWarehouseStats implements Serializable {
    private static final long serialVersionUID = 1L;

    private String date; // yyyy-MM-dd
    private long soPhieuNhap;
    private long soPhieuXuat;
    private double tongTienNhap;
    private double tongTienXuat;

    public DailyWarehouseStats() {
        this.date = "";
        this.soPhieuNhap = 0;
        this.soPhieuXuat = 0;
        this.tongTienNhap = 0.0;
        this.tongTienXuat = 0.0;
    }

    public DailyWarehouseStats(String date, long soPhieuNhap, long soPhieuXuat,
                               double tongTienNhap, double tongTienXuat) {
        this.date = date;
        this.soPhieuNhap = soPhieuNhap;
        this.soPhieuXuat = soPhieuXuat;
        this.tongTienNhap = tongTienNhap;
        this.tongTienXuat = tongTienXuat;
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public long getSoPhieuNhap() { return soPhieuNhap; }
    public void setSoPhieuNhap(long soPhieuNhap) { this.soPhieuNhap = soPhieuNhap; }

    public long getSoPhieuXuat() { return soPhieuXuat; }
    public void setSoPhieuXuat(long soPhieuXuat) { this.soPhieuXuat = soPhieuXuat; }

    public double getTongTienNhap() { return tongTienNhap; }
    public void setTongTienNhap(double tongTienNhap) { this.tongTienNhap = tongTienNhap; }

    public double getTongTienXuat() { return tongTienXuat; }
    public void setTongTienXuat(double tongTienXuat) { this.tongTienXuat = tongTienXuat; }

    public long getTongSoPhieu() { return soPhieuNhap + soPhieuXuat; }
    public double getTongGiaTriGiaoDich() { return tongTienNhap + tongTienXuat; }
}

