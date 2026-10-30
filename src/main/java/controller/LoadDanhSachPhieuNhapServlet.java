package controller;

import dao.PhieuNhapDAO;
import model.PhieuNhapDTO;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/LoadDanhSachPhieuNhapServlet")
public class LoadDanhSachPhieuNhapServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        String tuKhoa = request.getParameter("tuKhoa");

        PhieuNhapDAO dao = new PhieuNhapDAO();
        List<PhieuNhapDTO> listPN = dao.getDanhSachPhieuNhap(tuKhoa);

        request.setAttribute("danhSachPN", listPN);
        request.getRequestDispatcher("quanlynhapkho.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}