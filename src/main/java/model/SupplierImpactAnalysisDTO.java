package model;

import java.io.Serializable;
import java.util.*;

public class SupplierImpactAnalysisDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String maNCC;
    private String tenNCC;
    private String sdtNCC;
    private String emailNCC;

    private Set<String> danhSachHangCungCap = new LinkedHashSet<>();
    private List<DepartmentImpact> danhSachBoPhanAnhHuong = new ArrayList<>();
    private String mucDoRuiRo; // "CAO", "TRUNG BÌNH", "THẤP"

    public static class DepartmentImpact implements Serializable {
        private static final long serialVersionUID = 1L;

        private String tenBoPhan;
        private Set<String> danhSachHangPhuThuoc = new LinkedHashSet<>();
        private int tongSoLuongDaNhan;
        private int soDonHangLienQuan;

        public DepartmentImpact() {}

        public DepartmentImpact(String tenBoPhan) {
            this.tenBoPhan = tenBoPhan;
        }

        public String getTenBoPhan() { return tenBoPhan; }
        public void setTenBoPhan(String tenBoPhan) { this.tenBoPhan = tenBoPhan; }

        public Set<String> getDanhSachHangPhuThuoc() { return danhSachHangPhuThuoc; }
        public void setDanhSachHangPhuThuoc(Set<String> danhSachHangPhuThuoc) {
            this.danhSachHangPhuThuoc = danhSachHangPhuThuoc != null ? danhSachHangPhuThuoc : new LinkedHashSet<>();
        }

        public void addHangPhuThuoc(String hang) {
            if (hang != null && !hang.trim().isEmpty()) {
                this.danhSachHangPhuThuoc.add(hang.trim());
            }
        }

        public int getTongSoLuongDaNhan() { return tongSoLuongDaNhan; }
        public void setTongSoLuongDaNhan(int tongSoLuongDaNhan) { this.tongSoLuongDaNhan = tongSoLuongDaNhan; }
        public void addSoLuong(int sl) { this.tongSoLuongDaNhan += Math.max(0, sl); }

        public int getSoDonHangLienQuan() { return soDonHangLienQuan; }
        public void setSoDonHangLienQuan(int soDonHangLienQuan) { this.soDonHangLienQuan = soDonHangLienQuan; }
        public void incrementSoDonHang() { this.soDonHangLienQuan++; }
    }

    public SupplierImpactAnalysisDTO() {
        this.mucDoRuiRo = "THẤP";
    }

    public SupplierImpactAnalysisDTO(String maNCC, String tenNCC, String sdtNCC, String emailNCC) {
        this.maNCC = maNCC;
        this.tenNCC = tenNCC;
        this.sdtNCC = sdtNCC;
        this.emailNCC = emailNCC;
        this.mucDoRuiRo = "THẤP";
    }

    public String getMaNCC() { return maNCC; }
    public void setMaNCC(String maNCC) { this.maNCC = maNCC; }

    public String getTenNCC() { return tenNCC; }
    public void setTenNCC(String tenNCC) { this.tenNCC = tenNCC; }

    public String getSdtNCC() { return sdtNCC; }
    public void setSdtNCC(String sdtNCC) { this.sdtNCC = sdtNCC; }

    public String getEmailNCC() { return emailNCC; }
    public void setEmailNCC(String emailNCC) { this.emailNCC = emailNCC; }

    public Set<String> getDanhSachHangCungCap() { return danhSachHangCungCap; }
    public void setDanhSachHangCungCap(Set<String> danhSachHangCungCap) {
        this.danhSachHangCungCap = danhSachHangCungCap != null ? danhSachHangCungCap : new LinkedHashSet<>();
    }

    public void addHangCungCap(String hang) {
        if (hang != null && !hang.trim().isEmpty()) {
            this.danhSachHangCungCap.add(hang.trim());
        }
    }

    public List<DepartmentImpact> getDanhSachBoPhanAnhHuong() { return danhSachBoPhanAnhHuong; }
    public void setDanhSachBoPhanAnhHuong(List<DepartmentImpact> danhSachBoPhanAnhHuong) {
        this.danhSachBoPhanAnhHuong = danhSachBoPhanAnhHuong != null ? danhSachBoPhanAnhHuong : new ArrayList<>();
        tinhMucDoRuiRo();
    }

    public void addBoPhanImpact(DepartmentImpact impact) {
        if (impact != null) {
            this.danhSachBoPhanAnhHuong.add(impact);
            tinhMucDoRuiRo();
        }
    }

    public String getMucDoRuiRo() { return mucDoRuiRo; }
    public void setMucDoRuiRo(String mucDoRuiRo) { this.mucDoRuiRo = mucDoRuiRo; }

    public void tinhMucDoRuiRo() {
        int soBoPhan = danhSachBoPhanAnhHuong.size();
        int tongSoLuong = 0;
        for (DepartmentImpact d : danhSachBoPhanAnhHuong) {
            tongSoLuong += d.getTongSoLuongDaNhan();
        }

        if (soBoPhan >= 2 || tongSoLuong >= 50 || danhSachHangCungCap.size() >= 3) {
            this.mucDoRuiRo = "CAO";
        } else if (soBoPhan == 1 || tongSoLuong > 0) {
            this.mucDoRuiRo = "TRUNG BÌNH";
        } else {
            this.mucDoRuiRo = "THẤP";
        }
    }

    /**
     * Xuất dữ liệu đồ thị dạng JSON chuẩn hóa tương thích vis-network trên giao diện web.
     */
    public String toJsonGraph() {
        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> edges = new ArrayList<>();

        if (maNCC == null || maNCC.trim().isEmpty()) {
            Map<String, Object> emptyGraph = new HashMap<>();
            emptyGraph.put("nodes", nodes);
            emptyGraph.put("edges", edges);
            return new com.google.gson.Gson().toJson(emptyGraph);
        }

        // 1. Node Nhà Cung Cấp (Màu sắc theo mức độ rủi ro)
        String nccColor = "#e74c3c"; // Đỏ nếu CAO
        if ("TRUNG BÌNH".equalsIgnoreCase(mucDoRuiRo)) {
            nccColor = "#e67e22"; // Cam
        } else if ("THẤP".equalsIgnoreCase(mucDoRuiRo)) {
            nccColor = "#27ae60"; // Xanh lá
        }

        String nccNodeId = "NCC_" + maNCC;
        Map<String, Object> nccNode = new HashMap<>();
        nccNode.put("id", nccNodeId);
        nccNode.put("label", "🏢 " + (tenNCC != null ? tenNCC : maNCC) + "\n[Mức rủi ro: " + (mucDoRuiRo != null ? mucDoRuiRo : "THẤP") + "]");
        nccNode.put("group", "ncc");
        nccNode.put("shape", "box");
        nccNode.put("color", nccColor);
        nccNode.put("font", Collections.singletonMap("color", "#ffffff"));
        nodes.add(nccNode);

        // 2. Nodes Hàng Hóa và Edges NCC -> Hàng Hóa
        Set<String> addedGoods = new HashSet<>();
        for (String hang : danhSachHangCungCap) {
            if (hang == null || hang.trim().isEmpty()) continue;
            String hangId = "HH_" + hang.replaceAll("[^a-zA-Z0-9_]", "_");
            if (addedGoods.add(hangId)) {
                Map<String, Object> hNode = new HashMap<>();
                hNode.put("id", hangId);
                hNode.put("label", "📦 " + hang);
                hNode.put("group", "hanghoa");
                hNode.put("shape", "box");
                hNode.put("color", "#f39c12");
                hNode.put("font", Collections.singletonMap("color", "#ffffff"));
                nodes.add(hNode);

                Map<String, Object> edge = new HashMap<>();
                edge.put("from", nccNodeId);
                edge.put("to", hangId);
                edge.put("label", "Cung cấp");
                edge.put("arrows", "to");
                edge.put("color", Collections.singletonMap("color", "#f39c12"));
                edges.add(edge);
            }
        }

        // 3. Nodes Bộ Phận và Edges Hàng Hóa -> Bộ Phận
        Set<String> addedDepts = new HashSet<>();
        for (DepartmentImpact bp : danhSachBoPhanAnhHuong) {
            if (bp.getTenBoPhan() == null || bp.getTenBoPhan().trim().isEmpty()) continue;
            String bpId = "BP_" + bp.getTenBoPhan().replaceAll("[^a-zA-Z0-9_]", "_");
            if (addedDepts.add(bpId)) {
                Map<String, Object> bpNode = new HashMap<>();
                bpNode.put("id", bpId);
                bpNode.put("label", "👥 " + bp.getTenBoPhan() + "\n(Nhận: " + bp.getTongSoLuongDaNhan() + " SP | " + bp.getSoDonHangLienQuan() + " Đơn)");
                bpNode.put("group", "bophan");
                bpNode.put("shape", "box");
                bpNode.put("color", "#8e44ad");
                bpNode.put("font", Collections.singletonMap("color", "#ffffff"));
                nodes.add(bpNode);
            }

            // Nối Hàng Hóa phụ thuộc -> Bộ Phận
            for (String hang : bp.getDanhSachHangPhuThuoc()) {
                String hangId = "HH_" + hang.replaceAll("[^a-zA-Z0-9_]", "_");
                if (addedGoods.contains(hangId)) {
                    Map<String, Object> edge = new HashMap<>();
                    edge.put("from", hangId);
                    edge.put("to", bpId);
                    edge.put("label", "Lan truyền rủi ro");
                    edge.put("arrows", "to");
                    edge.put("color", Collections.singletonMap("color", "#c0392b"));
                    edges.add(edge);
                }
            }
        }

        Map<String, Object> graph = new HashMap<>();
        graph.put("nodes", nodes);
        graph.put("edges", edges);
        return new com.google.gson.Gson().toJson(graph);
    }
}
