package controller;

import dao.NhaCungCapDAO;
import model.NhaCungCap;
import model.taikhoan;
import java.io.IOException;
import java.net.URLEncoder;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/QuanLyNhaCungCapServlet")
public class QuanLyNhaCungCapServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        taikhoan acc = (taikhoan) request.getSession().getAttribute("account");
        if (acc == null) { response.sendRedirect("login.jsp"); return; }

        NhaCungCapDAO dao = new NhaCungCapDAO();
        String action = request.getParameter("action");

        if ("delete".equals(action)) {
            String maNCC = request.getParameter("id");
            String result = dao.xoaNCC(maNCC);
            if (!result.equals("SUCCESS")) {
                response.sendRedirect("QuanLyNhaCungCapServlet?error=" + URLEncoder.encode(result, "UTF-8"));
                return;
            }
        }
        
        // Load danh sách và đẩy lên trang JSP
        request.setAttribute("listNCC", dao.getAll());
        request.getRequestDispatcher("quanly_nhacungcap.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        NhaCungCapDAO dao = new NhaCungCapDAO();
        
        String action = request.getParameter("action");
        String maNCC = request.getParameter("maNCC");
        String tenNCC = request.getParameter("tenNCC");
        String diaChi = request.getParameter("diaChi");
        String sdt = request.getParameter("sdt");
        String email = request.getParameter("email");

        // Kiểm tra trùng tên
        if (dao.kiemTraTrungTen(tenNCC, "edit".equals(action) ? maNCC : null)) {
            response.sendRedirect("QuanLyNhaCungCapServlet?error=" + URLEncoder.encode("Tên nhà cung cấp đã tồn tại!", "UTF-8"));
            return;
        }

        NhaCungCap ncc = new NhaCungCap(maNCC, tenNCC, diaChi, sdt, email);

        if ("add".equals(action)) {
            dao.themNCC(ncc);
        } else if ("edit".equals(action)) {
            dao.suaNCC(ncc);
        }

        response.sendRedirect("QuanLyNhaCungCapServlet?success=1");
    }
}