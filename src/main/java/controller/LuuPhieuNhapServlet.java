package controller;

import dao.PhieuNhapDAO;
import model.PhieuNhapKho;
import model.taikhoan;
import java.io.IOException;
import java.sql.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/LuuPhieuNhapServlet")
public class LuuPhieuNhapServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        taikhoan acc = (taikhoan) request.getSession().getAttribute("account");
        if (acc == null) { response.sendRedirect("login.jsp"); return; }

        String[] maHangArr = request.getParameterValues("maHang[]");
        if (maHangArr == null || maHangArr.length == 0) {
            request.setAttribute("thongBaoLoi", "Vui lòng thêm mặt hàng vào danh sách nhập!");
            request.getRequestDispatcher("LapPhieuNhapServlet").forward(request, response);
            return;
        }

        PhieuNhapKho pn = new PhieuNhapKho();
        pn.setMaPhieu(request.getParameter("maPhieu"));
        pn.setNgayLap(Date.valueOf(request.getParameter("ngayLap")));
        pn.setMaNCC(request.getParameter("maNCC"));
        pn.setSoHoaDon(request.getParameter("soHoaDon"));
        pn.setGhiChu(request.getParameter("ghiChu"));

        String ketQua = new PhieuNhapDAO().luuPhieuNhapMoi(pn, acc.getTenDangNhap(), maHangArr, request.getParameterValues("soLuong[]"), request.getParameterValues("donGia[]"));

        if ("SUCCESS".equals(ketQua)) {
            // Tạm thời mình cho quay về Quản lý Hàng Hóa (khi nào bạn làm trang QuanLyNhapKho thì đổi link sau)
            response.sendRedirect("LoadDanhSachKhoServlet?msg=success"); 
        } else {
            request.setAttribute("thongBaoLoi", ketQua);
            request.getRequestDispatcher("LapPhieuNhapServlet").forward(request, response);
        }
    }
}