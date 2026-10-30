package controller;

import dao.HangHoaDAO;
import dao.PhieuNhapDAO;
import model.taikhoan;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/LapPhieuNhapServlet")
public class LapPhieuNhapServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        
        taikhoan acc = (taikhoan) request.getSession().getAttribute("account");
        if (acc == null) { response.sendRedirect("login.jsp"); return; }

        // TỰ ĐỘNG SINH MÃ PHIẾU NHẬP
        String timeStamp = new java.text.SimpleDateFormat("yyMMdd_HHmmss").format(new java.util.Date());
        String maPhieuAuto = "PN_" + timeStamp;
        request.setAttribute("maPhieuAuto", maPhieuAuto);

        // Load danh sách Hàng Hóa và Nhà Cung Cấp
        request.setAttribute("danhSachHH", new HangHoaDAO().getAllHangHoa());
        request.setAttribute("danhSachNCC", new PhieuNhapDAO().getAllNhaCungCap());
        
        request.getRequestDispatcher("lapphieunhap.jsp").forward(request, response);
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}