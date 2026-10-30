package controller;

import dao.HangHoaDAO;
import model.HangHoa;
import model.taikhoan;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/LapPhieuXuatServlet")
public class LapPhieuXuatServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        // 1. Kiểm tra đăng nhập
        HttpSession session = request.getSession();
        taikhoan acc = (taikhoan) session.getAttribute("account");
        if (acc == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        // 2. TỰ ĐỘNG SINH MÃ PHIẾU XUẤT (VD: PX_260602_143005)
        String timeStamp = new java.text.SimpleDateFormat("yyMMdd_HHmmss").format(new java.util.Date());
        String maPhieuAuto = "PX_" + timeStamp;
        request.setAttribute("maPhieuAuto", maPhieuAuto);

        // 3. Lấy danh sách Hàng Hóa để đổ vào cái khung Dropdown chọn hàng
        HangHoaDAO dao = new HangHoaDAO();
        List<HangHoa> listHH = dao.getAllHangHoa();
        request.setAttribute("danhSachHH", listHH);
        
        // 4. Chuyển hướng người dùng qua trang giao diện Lập phiếu xuất
        request.getRequestDispatcher("lapphieuxuat.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}