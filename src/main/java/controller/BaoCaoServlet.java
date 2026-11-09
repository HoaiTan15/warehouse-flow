package controller;

import dao.BaoCaoDAO;
import model.taikhoan;
import java.io.IOException; 
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/BaoCaoServlet")
public class BaoCaoServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        taikhoan acc = (taikhoan) request.getSession().getAttribute("account");
        if (acc == null) { response.sendRedirect("login.jsp"); return; }

        // Nhận loại báo cáo (mặc định là nhapkho)
        String loai = request.getParameter("loai");
        if (loai == null || loai.isEmpty()) loai = "nhapkho";
        
        // Nhận điều kiện lọc (mặc định là tất cả)
        String filter = request.getParameter("filter");
        if (filter == null) filter = "all";

        BaoCaoDAO dao = new BaoCaoDAO();
        nosql.neo4j.WarehouseGraphService graphService = new nosql.neo4j.WarehouseGraphService();

        // Luôn chuẩn bị danh sách Nhà Cung Cấp cho bộ lọc báo cáo
        java.util.List<java.util.Map<String, String>> suppliers = graphService.getAllSuppliersInGraph();
        if (suppliers == null || suppliers.isEmpty()) {
            dao.NhaCungCapDAO nccDAO = new dao.NhaCungCapDAO();
            java.util.List<model.NhaCungCap> nccList = nccDAO.getAll();
            suppliers = new java.util.ArrayList<>();
            for (model.NhaCungCap n : nccList) {
                java.util.Map<String, String> m = new java.util.HashMap<>();
                m.put("maNCC", n.getMaNCC());
                m.put("tenNCC", n.getTenNCC());
                suppliers.add(m);
            }
        }
        request.setAttribute("supplierList", suppliers);

        // Dựa vào Loại báo cáo để kéo dữ liệu tương ứng
        if ("nhapkho".equals(loai)) {
            request.setAttribute("danhSach", dao.getBaoCaoNhapKho(filter));
        } else if ("xuatkho".equals(loai)) {
            request.setAttribute("danhSach", dao.getBaoCaoXuatKho(filter));
        } else if ("kiemke".equals(loai)) {
            request.setAttribute("danhSach", dao.getBaoCaoKiemKe(filter));
        } else if ("ruiro_ncc".equals(loai)) {
            // Chức năng 3: Phân tích rủi ro & phụ thuộc chuỗi cung ứng từ Neo4j Graph Database
            String maNCC = request.getParameter("maNCC");
            if ((maNCC == null || maNCC.trim().isEmpty()) && !suppliers.isEmpty()) {
                maNCC = suppliers.get(0).get("maNCC");
            }

            if (maNCC != null && !maNCC.trim().isEmpty()) {
                model.SupplierImpactAnalysisDTO impactDTO = graphService.analyzeSupplierImpact(maNCC);
                request.setAttribute("selectedMaNCC", maNCC);
                request.setAttribute("supplierImpact", impactDTO);
                request.setAttribute("supplierGraphJson", impactDTO != null ? impactDTO.toJsonGraph() : "{\"nodes\":[],\"edges\":[]}");
            }
        }

        request.setAttribute("loaiHienTai", loai);
        request.setAttribute("filterHienTai", filter);
        
        request.getRequestDispatcher("baocao.jsp").forward(request, response);
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}