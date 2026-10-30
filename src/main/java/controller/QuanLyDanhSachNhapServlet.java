package controller;

import dao.PhieuNhapDAO;
import model.PhieuNhapDTO;
import model.taikhoan;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/QuanLyDanhSachNhapServlet")
public class QuanLyDanhSachNhapServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        taikhoan acc = (taikhoan) request.getSession().getAttribute("account");
        // Chỉ Trưởng kho hoặc Giám đốc mới được vào
        if (acc == null || (!acc.getVaiTro().equals("Trưởng kho") && !acc.getVaiTro().equals("Giám đốc"))) {
            response.sendRedirect("login.jsp");
            return;
        }

        try {
            PhieuNhapDAO dao = new PhieuNhapDAO();
            // Hàm này bạn tự viết trong DAO: Lấy tất cả phiếu nhập (Sắp xếp mới nhất lên đầu)
            List<PhieuNhapDTO> list = dao.getDanhSachPhieuNhap(null); 
            request.setAttribute("danhSachPhieu", list);
            
            request.getRequestDispatcher("quanly_danhsachnhap.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}