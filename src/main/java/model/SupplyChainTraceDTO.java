package model;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.*;

public class SupplyChainTraceDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    // 1. Thông tin Hàng Hóa
    private String maHang;
    private String tenHang;
    private String dvt;
    private String quyCach;

    // 2. Nguồn gốc: Nhà Cung Cấp
    private String maNCC;
    private String tenNCC;
    private String sdtNCC;
    private String emailNCC;
    private String diaChiNCC;

    // 3. Lưu kho: Vị Trí Kệ Kho
    private String maViTri;
    private String tenViTri;

    // 4. Phân phối: Các Bộ Phận đã nhận hàng
    private List<XuatKhoDestination> danhSachXuat = new ArrayList<>();

    public static class XuatKhoDestination implements Serializable {
        private static final long serialVersionUID = 1L;
        private String maPhieu;
        private String ngayLap;
        private String boPhan;
        private int soLuong;
        private double donGia;
        private String ghiChu;

        public XuatKhoDestination() {}

        public XuatKhoDestination(String maPhieu, String ngayLap, String boPhan, int soLuong, double donGia, String ghiChu) {
            this.maPhieu = maPhieu;
            this.ngayLap = ngayLap;
            this.boPhan = boPhan;
            this.soLuong = soLuong;
            this.donGia = donGia;
            this.ghiChu = ghiChu;
        }

        public String getMaPhieu() { return maPhieu; }
        public void setMaPhieu(String maPhieu) { this.maPhieu = maPhieu; }

        public String getNgayLap() { return ngayLap; }
        public void setNgayLap(String ngayLap) { this.ngayLap = ngayLap; }

        public String getBoPhan() { return boPhan; }
        public void setBoPhan(String boPhan) { this.boPhan = boPhan; }

        public int getSoLuong() { return soLuong; }
        public void setSoLuong(int soLuong) { this.soLuong = soLuong; }

        public double getDonGia() { return donGia; }
        public void setDonGia(double donGia) { this.donGia = donGia; }

        public String getGhiChu() { return ghiChu; }
        public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    }

    public SupplyChainTraceDTO() {}

    public SupplyChainTraceDTO(String maHang, String tenHang, String dvt, String quyCach) {
        this.maHang = maHang;
        this.tenHang = tenHang;
        this.dvt = dvt;
        this.quyCach = quyCach;
    }

    // Getters and Setters
    public String getMaHang() { return maHang; }
    public void setMaHang(String maHang) { this.maHang = maHang; }

    public String getTenHang() { return tenHang; }
    public void setTenHang(String tenHang) { this.tenHang = tenHang; }

    public String getDvt() { return dvt; }
    public void setDvt(String dvt) { this.dvt = dvt; }

    public String getQuyCach() { return quyCach; }
    public void setQuyCach(String quyCach) { this.quyCach = quyCach; }

    public String getMaNCC() { return maNCC; }
    public void setMaNCC(String maNCC) { this.maNCC = maNCC; }

    public String getTenNCC() { return tenNCC; }
    public void setTenNCC(String tenNCC) { this.tenNCC = tenNCC; }

    public String getSdtNCC() { return sdtNCC; }
    public void setSdtNCC(String sdtNCC) { this.sdtNCC = sdtNCC; }

    public String getEmailNCC() { return emailNCC; }
    public void setEmailNCC(String emailNCC) { this.emailNCC = emailNCC; }

    public String getDiaChiNCC() { return diaChiNCC; }
    public void setDiaChiNCC(String diaChiNCC) { this.diaChiNCC = diaChiNCC; }

    public String getMaViTri() { return maViTri; }
    public void setMaViTri(String maViTri) { this.maViTri = maViTri; }

    public String getTenViTri() { return tenViTri; }
    public void setTenViTri(String tenViTri) { this.tenViTri = tenViTri; }

    public List<XuatKhoDestination> getDanhSachXuat() { return danhSachXuat; }
    public void setDanhSachXuat(List<XuatKhoDestination> danhSachXuat) {
        this.danhSachXuat = danhSachXuat != null ? danhSachXuat : new ArrayList<>();
    }

    public void addXuatKho(XuatKhoDestination dest) {
        if (dest != null) {
            this.danhSachXuat.add(dest);
        }
    }

    /**
     * Xuất dữ liệu đồ thị dạng JSON chuẩn hóa tương thích 100% với vis-network trên giao diện web.
     */
    public String toJsonGraph() {
        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> edges = new ArrayList<>();

        if (maHang == null || maHang.trim().isEmpty()) {
            Map<String, Object> emptyGraph = new HashMap<>();
            emptyGraph.put("nodes", nodes);
            emptyGraph.put("edges", edges);
            return new Gson().toJson(emptyGraph);
        }

        // 1. Node trung tâm: Hàng Hóa (Màu vàng cam)
        String hangNodeId = "HH_" + maHang;
        Map<String, Object> hangNode = new HashMap<>();
        hangNode.put("id", hangNodeId);
        hangNode.put("label", "📦 " + maHang + "\n" + (tenHang != null ? tenHang : ""));
        hangNode.put("group", "hanghoa");
        hangNode.put("shape", "box");
        hangNode.put("color", "#f39c12");
        hangNode.put("font", Collections.singletonMap("color", "#ffffff"));
        nodes.add(hangNode);

        // 2. Node Nhà Cung Cấp (Màu xanh dương)
        if (tenNCC != null && !tenNCC.trim().isEmpty()) {
            String nccNodeId = "NCC_" + (maNCC != null ? maNCC : "UNKNOWN");
            Map<String, Object> nccNode = new HashMap<>();
            nccNode.put("id", nccNodeId);
            nccNode.put("label", "🏢 " + tenNCC + "\n(Nhà Cung Cấp)");
            nccNode.put("group", "ncc");
            nccNode.put("shape", "box");
            nccNode.put("color", "#2980b9");
            nccNode.put("font", Collections.singletonMap("color", "#ffffff"));
            nodes.add(nccNode);

            Map<String, Object> nccEdge = new HashMap<>();
            nccEdge.put("from", nccNodeId);
            nccEdge.put("to", hangNodeId);
            nccEdge.put("label", "CUNG CẤP");
            nccEdge.put("arrows", "to");
            nccEdge.put("color", "#34495e");
            edges.add(nccEdge);
        }

        // 3. Node Vị Trí Kệ Kho (Màu xanh lá)
        if (tenViTri != null && !tenViTri.trim().isEmpty()) {
            String vtNodeId = "VT_" + (maViTri != null ? maViTri : "UNKNOWN");
            Map<String, Object> vtNode = new HashMap<>();
            vtNode.put("id", vtNodeId);
            vtNode.put("label", "📍 " + tenViTri + "\n(Kệ Lưu Kho)");
            vtNode.put("group", "vitri");
            vtNode.put("shape", "box");
            vtNode.put("color", "#27ae60");
            vtNode.put("font", Collections.singletonMap("color", "#ffffff"));
            nodes.add(vtNode);

            Map<String, Object> vtEdge = new HashMap<>();
            vtEdge.put("from", hangNodeId);
            vtEdge.put("to", vtNodeId);
            vtEdge.put("label", "LƯU TRỮ");
            vtEdge.put("arrows", "to");
            vtEdge.put("color", "#27ae60");
            edges.add(vtEdge);
        }

        // 4. Các Node Bộ Phận Nhận Xuất (Màu tím)
        Set<String> addedBoPhan = new HashSet<>();
        if (danhSachXuat != null) {
            for (int i = 0; i < danhSachXuat.size(); i++) {
                XuatKhoDestination dest = danhSachXuat.get(i);
                String bpName = dest.getBoPhan() != null ? dest.getBoPhan() : "Chưa xác định";
                String bpNodeId = "BP_" + bpName.replaceAll("\\s+", "_");

                if (!addedBoPhan.contains(bpNodeId)) {
                    Map<String, Object> bpNode = new HashMap<>();
                    bpNode.put("id", bpNodeId);
                    bpNode.put("label", "👥 " + bpName + "\n(Bộ Phận Nhận)");
                    bpNode.put("group", "bophan");
                    bpNode.put("shape", "box");
                    bpNode.put("color", "#8e44ad");
                    bpNode.put("font", Collections.singletonMap("color", "#ffffff"));
                    nodes.add(bpNode);
                    addedBoPhan.add(bpNodeId);
                }

                Map<String, Object> edge = new HashMap<>();
                edge.put("from", hangNodeId);
                edge.put("to", bpNodeId);
                edge.put("label", "XUẤT (" + dest.getSoLuong() + " " + (dvt != null ? dvt : "") + ")");
                edge.put("arrows", "to");
                edge.put("color", "#c0392b");
                edges.add(edge);
            }
        }

        Map<String, Object> graph = new HashMap<>();
        graph.put("nodes", nodes);
        graph.put("edges", edges);
        return new Gson().toJson(graph);
    }
}

