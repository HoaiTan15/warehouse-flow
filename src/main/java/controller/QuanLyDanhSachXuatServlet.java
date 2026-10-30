package controller;

import dao.PhieuXuatDAO;
import model.PhieuXuatDTO;
import model.taikhoan;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/QuanLyDanhSachXuatServlet")
public class QuanLyDanhSachXuatServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        taikhoan acc = (taikhoan) request.getSession().getAttribute("account");
        if (acc == null || (!acc.getVaiTro().equals("Trưởng kho") && !acc.getVaiTro().equals("Giám đốc"))) {
            response.sendRedirect("login.jsp"); return;
        }

        try {
            PhieuXuatDAO dao = new PhieuXuatDAO();
            List<PhieuXuatDTO> list = dao.getDanhSachPhieuXuat(); 
            request.setAttribute("danhSachPhieu", list);
            request.getRequestDispatcher("quanly_danhsachxuat.jsp").forward(request, response);
        } catch (Exception e) { e.printStackTrace(); }
    }
}