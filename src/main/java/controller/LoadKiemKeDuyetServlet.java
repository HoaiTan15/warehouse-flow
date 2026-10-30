package controller;

import dao.KiemKeDuyetDAO;
import model.PhieuKiemKe;
import model.taikhoan;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/LoadKiemKeDuyetServlet")
public class LoadKiemKeDuyetServlet extends HttpServlet {
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        // 1. Kiểm tra session
        taikhoan acc = (taikhoan) request.getSession().getAttribute("account");
        if (acc == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        // 2. Lấy dữ liệu từ DAO
        KiemKeDuyetDAO dao = new KiemKeDuyetDAO();
        List<PhieuKiemKe> danhSach = dao.getDanhSachChoXetDuyet();

        // 3. Đẩy dữ liệu vào request với tên key là "danhSachPhieu"
        // (Phải khớp chính xác với tên trong file kiemkeduyet.jsp)
        request.setAttribute("danhSachPhieu", danhSach);
        
        // 4. Chuyển hướng
        request.getRequestDispatcher("quanlyduyetkiemke.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}