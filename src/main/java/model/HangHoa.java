package model;

public class HangHoa {
    private String maHang;
    private String tenHang;
    private String dvt;
    private int soLuongTonKho;
    private String quyCach;
    private int hanMucTonToiThieu;
    private String maLoai;
    private String maViTri;
    
    private String tenLoai;
    private String tenViTri;

    public HangHoa() {}

    public HangHoa(String maHang, String tenHang, String dvt, int soLuongTonKho, String quyCach, int hanMucTonToiThieu, String maLoai, String maViTri, String tenLoai, String tenViTri) {
        this.maHang = maHang;
        this.tenHang = tenHang;
        this.dvt = dvt;
        this.soLuongTonKho = soLuongTonKho;
        this.quyCach = quyCach;
        this.hanMucTonToiThieu = hanMucTonToiThieu;
        this.maLoai = maLoai;
        this.maViTri = maViTri;
        this.tenLoai = tenLoai;
        this.tenViTri = tenViTri;
    }

    // Constructor rút gọn để xử lý dữ liệu Form nhận về
    public HangHoa(String maHang, String tenHang, String dvt, int soLuongTonKho, String quyCach, int hanMucTonToiThieu, String maLoai, String maViTri) {
        this.maHang = maHang;
        this.tenHang = tenHang;
        this.dvt = dvt;
        this.soLuongTonKho = soLuongTonKho;
        this.quyCach = quyCach;
        this.hanMucTonToiThieu = hanMucTonToiThieu;
        this.maLoai = maLoai;
        this.maViTri = maViTri;
    }

    // Các hàm Getter và Setter
    public String getMaHang() { return maHang; }
    public void setMaHang(String maHang) { this.maHang = maHang; }
    public String getTenHang() { return tenHang; }
    public void setTenHang(String tenHang) { this.tenHang = tenHang; }
    public String getDvt() { return dvt; }
    public void setDvt(String dvt) { this.dvt = dvt; }
    public int getSoLuongTonKho() { return soLuongTonKho; }
    public void setSoLuongTonKho(int soLuongTonKho) { this.soLuongTonKho = soLuongTonKho; }
    public String getQuyCach() { return quyCach; }
    public void setQuyCach(String quyCach) { this.quyCach = quyCach; }
    public int getHanMucTonToiThieu() { return hanMucTonToiThieu; }
    public void setHanMucTonToiThieu(int hanMucTonToiThieu) { this.hanMucTonToiThieu = hanMucTonToiThieu; }
    public String getMaLoai() { return maLoai; }
    public void setMaLoai(String maLoai) { this.maLoai = maLoai; }
    public String getMaViTri() { return maViTri; }
    public void setMaViTri(String maViTri) { this.maViTri = maViTri; }
    public String getTenLoai() { return tenLoai; }
    public void setTenLoai(String tenLoai) { this.tenLoai = tenLoai; }
    public String getTenViTri() { return tenViTri; }
    public void setTenViTri(String tenViTri) { this.tenViTri = tenViTri; }
}