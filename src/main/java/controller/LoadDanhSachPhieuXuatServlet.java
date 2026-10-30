package controller;

import dao.PhieuXuatDAO;
import model.PhieuXuatDTO;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/LoadDanhSachPhieuXuatServlet")
public class LoadDanhSachPhieuXuatServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        // Nhận từ khóa tìm kiếm (nếu có)
        String tuKhoa = request.getParameter("tuKhoa");

        // Gọi DAO lấy danh sách
        PhieuXuatDAO dao = new PhieuXuatDAO();
        List<PhieuXuatDTO> listPX = dao.getDanhSachPhieuXuat(tuKhoa);

        // Đẩy sang giao diện
        request.setAttribute("danhSachPX", listPX);
        request.getRequestDispatcher("quanlyxuatkho.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}