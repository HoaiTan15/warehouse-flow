package controller;

import dao.KiemKeDAO;
import model.taikhoan;
import java.io.IOException;
import java.sql.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/LuuKiemKeServlet")
public class LuuKiemKeServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        taikhoan acc = (taikhoan) request.getSession().getAttribute("account");
        if (acc == null) { response.sendRedirect("login.jsp"); return; }

        String maKiemKe = request.getParameter("maKiemKe");
        Date ngayKiem = Date.valueOf(request.getParameter("ngayKiem"));
        
        String[] maHangArr = request.getParameterValues("maHang[]");
        String[] thucTeArr = request.getParameterValues("thucTe[]");
        String[] chenhLechArr = request.getParameterValues("chenhLech[]");
        String[] nguyenNhanArr = request.getParameterValues("nguyenNhan[]");

        if (maHangArr == null || maHangArr.length == 0) {
            request.setAttribute("thongBaoLoi", "Chưa có mặt hàng nào được kiểm kê!");
            request.getRequestDispatcher("KiemKeServlet").forward(request, response);
            return;
        }

        KiemKeDAO dao = new KiemKeDAO();
        String ketQua = dao.luuPhieuKiemKe(maKiemKe, ngayKiem, acc.getTenDangNhap(), maHangArr, thucTeArr, chenhLechArr, nguyenNhanArr);

        if ("SUCCESS".equals(ketQua)) {
            // Đúng yêu cầu của bạn: Chuyển sang chức năng báo cáo với loại 'kiemke'
            response.sendRedirect("BaoCaoServlet?loai=kiemke&msg=success");
        } else {
            request.setAttribute("thongBaoLoi", ketQua);
            request.getRequestDispatcher("KiemKeServlet").forward(request, response);
        }
    }
}