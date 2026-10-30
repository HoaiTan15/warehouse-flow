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
        
        // Dựa vào Loại báo cáo để kéo dữ liệu tương ứng
        if ("nhapkho".equals(loai)) {
            request.setAttribute("danhSach", dao.getBaoCaoNhapKho(filter));
        } else if ("xuatkho".equals(loai)) {
            request.setAttribute("danhSach", dao.getBaoCaoXuatKho(filter));
        } else if ("kiemke".equals(loai)) {
            request.setAttribute("danhSach", dao.getBaoCaoKiemKe(filter));
        }

        request.setAttribute("loaiHienTai", loai);
        request.setAttribute("filterHienTai", filter);
        
        request.getRequestDispatcher("baocao.jsp").forward(request, response);
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}