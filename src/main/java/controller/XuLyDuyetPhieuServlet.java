package controller;

import dao.PhieuNhapDAO;
import dao.PhieuXuatDAO;
import model.taikhoan;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/XuLyDuyetPhieuServlet")
public class XuLyDuyetPhieuServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        taikhoan acc = (taikhoan) request.getSession().getAttribute("account");
        if (acc == null) { response.sendRedirect("login.jsp"); return; }

        String action = request.getParameter("action");
        String loai = request.getParameter("loai");
        String maPhieu = request.getParameter("maPhieu");
        String lyDo = request.getParameter("lyDo"); 
        String maNguoiDuyet = acc.getTenDangNhap();

        try {
            if ("nhap".equals(loai)) {
                PhieuNhapDAO daoNhap = new PhieuNhapDAO();
                if ("duyet".equals(action)) daoNhap.duyetPhieu(maPhieu, maNguoiDuyet);
                else if ("tuchoi".equals(action)) daoNhap.tuChoiPhieu(maPhieu, maNguoiDuyet, "Từ chối: " + lyDo);
                
                response.sendRedirect("QuanLyDanhSachNhapServlet");
                
            } else if ("xuat".equals(loai)) {
                PhieuXuatDAO daoXuat = new PhieuXuatDAO();
                if ("duyet".equals(action)) daoXuat.duyetPhieu(maPhieu, maNguoiDuyet);
                else if ("tuchoi".equals(action)) daoXuat.tuChoiPhieu(maPhieu, maNguoiDuyet, "Từ chối: " + lyDo);
                
                response.sendRedirect("QuanLyDanhSachXuatServlet");
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}