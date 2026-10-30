package controller;

import dao.QuanLyTaiKhoanDAO;
import model.TaiKhoanDTO;
import model.taikhoan; 

import java.io.IOException;
import java.net.URLEncoder;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/QuanLyTaiKhoanServlet")
public class QuanLyTaiKhoanServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        taikhoan acc = (taikhoan) request.getSession().getAttribute("account");
        // CHỈ CÓ ADMIN MỚI ĐƯỢC VÀO TRANG NÀY
        if (acc == null || !acc.getVaiTro().equalsIgnoreCase("Admin")) {
            response.sendRedirect("login.jsp"); return;
        }

        QuanLyTaiKhoanDAO dao = new QuanLyTaiKhoanDAO();
        String action = request.getParameter("action");

        // Xử lý Xóa
        if ("delete".equals(action)) {
            dao.xoaTaiKhoan(request.getParameter("id"));
            response.sendRedirect("QuanLyTaiKhoanServlet?success=1");
            return;
        }
        
        // Đẩy danh sách TK và danh sách NV chưa có TK lên JSP
        request.setAttribute("listTK", dao.getAllTaiKhoan());
        request.setAttribute("mapNV", dao.getNhanVienChuaCoTaiKhoan());
        request.getRequestDispatcher("quanly_taikhoan.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        QuanLyTaiKhoanDAO dao = new QuanLyTaiKhoanDAO();
        
        String action = request.getParameter("action");
        String tenDN = request.getParameter("tenDangNhap");
        String matKhau = request.getParameter("matKhau");
        String vaiTro = request.getParameter("vaiTro");
        
        if ("add".equals(action)) {
            String maNV = request.getParameter("maNV");
            if (dao.kiemTraTrung(tenDN)) {
                response.sendRedirect("QuanLyTaiKhoanServlet?error=" + URLEncoder.encode("Tên đăng nhập đã tồn tại!", "UTF-8"));
                return;
            }
            dao.themTaiKhoan(tenDN, matKhau, vaiTro, maNV);
            
        } else if ("edit".equals(action)) {
            String trangThai = request.getParameter("trangThai");
            dao.suaTaiKhoan(tenDN, matKhau, vaiTro, trangThai);
        }

        response.sendRedirect("QuanLyTaiKhoanServlet?success=1");
    }
}