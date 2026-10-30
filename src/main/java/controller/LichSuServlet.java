package controller;

import dao.DBConnect;
import dao.HangHoaDAO;
import dao.LichSuDAO;
import model.taikhoan;
import java.io.IOException;
import java.sql.Connection;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/LichSuServlet")
public class LichSuServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        taikhoan acc = (taikhoan) request.getSession().getAttribute("account");
        if (acc == null) { 
            response.sendRedirect("login.jsp"); 
            return; 
        }

        // Đã sửa: Bắt đúng tên tham số từ URL
        String maHangLoc = request.getParameter("maHangLoc");

        try (Connection conn = DBConnect.getConnection()) {
            request.setAttribute("danhSachHH", new HangHoaDAO().getAllHangHoa());
            
            // Lấy lịch sử biến động theo mã hàng đã lọc
            request.setAttribute("danhSachLichSu", new LichSuDAO().getLichSuBienDong(conn, maHangLoc));
            
            // Đẩy lại biến này sang JSP để thẻ select hiển thị đúng mặt hàng đang chọn
            request.setAttribute("maHangHienTai", maHangLoc);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        request.getRequestDispatcher("lichsu.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}