package controller;

import dao.TaiKhoanDAO;
import model.taikhoan;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {

   @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Hỗ trợ đọc tiếng Việt từ form (nếu có)
        request.setCharacterEncoding("UTF-8");
        
        // 1. Lấy dữ liệu người dùng nhập từ login.jsp
        String user = request.getParameter("username");
        String pass = request.getParameter("password");
        
        // 2. Gọi DAO để kiểm tra với Database 
        TaiKhoanDAO tkDao = new TaiKhoanDAO();
        taikhoan acc = tkDao.checkLogin(user, pass); // Lưu kết quả vào biến 'acc'
        
        // 3. Xử lý kết quả
        if (acc != null) {
            
            if ("Bị khóa".equalsIgnoreCase(acc.getTrangThai())) {
                request.setAttribute("errorMessage", "Tài khoản tạm thời bị khóa! Vui lòng liên hệ admin"); 
                request.getRequestDispatcher("login.jsp").forward(request, response);
                return; // Ngắt luôn, không cho chạy tiếp
            }
            // -----------------------------------------------

            // Đúng và không bị khóa: Tạo Session lưu thông tin người dùng
            HttpSession session = request.getSession();
            session.setAttribute("account", acc);
            
            // Chuyển hướng sang trang chủ
            response.sendRedirect("home.jsp");
            
        } else {
            // Sai: Tạo thông báo lỗi và quay lại trang đăng nhập
            request.setAttribute("errorMessage", "Tên đăng nhập hoặc mật khẩu không chính xác!");
            request.getRequestDispatcher("login.jsp").forward(request, response);
        }
    }
}