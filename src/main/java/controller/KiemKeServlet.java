package controller;

import dao.HangHoaDAO;
import model.taikhoan;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/KiemKeServlet")
public class KiemKeServlet extends HttpServlet {
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        // 1. Kiểm tra session đăng nhập
        taikhoan acc = (taikhoan) request.getSession().getAttribute("account");
        if (acc == null) { 
            response.sendRedirect("login.jsp"); 
            return; 
        }

        // 2. TỰ ĐỘNG SINH MÃ PHIẾU KIỂM KÊ (PKK_NămThángNgày_GiờPhútGiây)
        String timeStamp = new java.text.SimpleDateFormat("yyMMdd_HHmmss").format(new java.util.Date());
        String maKiemKeAuto = "PKK_" + timeStamp;
        
        // Gắn mã vừa sinh vào request để ném sang trang JSP
        request.setAttribute("maKiemKeAuto", maKiemKeAuto);

        // 3. Lấy danh sách hàng hóa và chuyển trang
        request.setAttribute("danhSachHH", new HangHoaDAO().getAllHangHoa());
        request.getRequestDispatcher("kiemke.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}