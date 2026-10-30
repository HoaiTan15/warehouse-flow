package controller;

import dao.CongNoDAO;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/CongNoServlet")
public class CongNoServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Gửi danh sách công nợ ra JSP
        request.setAttribute("listCongNo", new CongNoDAO().getDanhSachCongNo());
        request.getRequestDispatcher("quanlycongno.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("pay".equals(action)) {
            String maCongNo = request.getParameter("maCongNo");
            double soTien = Double.parseDouble(request.getParameter("soTien"));
            new CongNoDAO().thanhToan(maCongNo, soTien);
        }
        response.sendRedirect("CongNoServlet");
    }
}