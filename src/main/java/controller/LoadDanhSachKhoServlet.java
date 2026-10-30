package controller;

import dao.HangHoaDAO;
import model.HangHoa;
import model.taikhoan;
import java.io.IOException;
import java.net.URLEncoder;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/LoadDanhSachKhoServlet")
public class LoadDanhSachKhoServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        taikhoan acc = (taikhoan) request.getSession().getAttribute("account");
        if (acc == null) { response.sendRedirect("login.jsp"); return; }

        HangHoaDAO dao = new HangHoaDAO();
        String action = request.getParameter("action");

        // Xử lý lệnh Xóa từ giao diện gửi xuống
        if ("delete".equals(action)) {
            String maHang = request.getParameter("id");
            int soLuong = Integer.parseInt(request.getParameter("sl"));
            
            // Backend chặn thêm 1 lớp nữa cho chắc ăn (Dưới 10 mới cho xóa)
            if(soLuong >= 10) {
                response.sendRedirect("QuanLyHangHoaServlet?error=" + URLEncoder.encode("Lỗi: Chỉ được xóa hàng hóa có tồn kho dưới 10!", "UTF-8"));
                return;
            }
            
            String result = dao.xoaHangHoa(maHang);
            if (!result.equals("SUCCESS")) {
                response.sendRedirect("QuanLyHangHoaServlet?error=" + URLEncoder.encode(result, "UTF-8"));
                return;
            }
            response.sendRedirect("QuanLyHangHoaServlet?success=1");
            return;
        }

        // Đẩy danh sách Hàng hóa, Loại hàng, Vị trí lên JSP
        request.setAttribute("listHH", dao.getAllHangHoa());
        request.setAttribute("mapLoai", dao.getMapLoaiHang());
        request.setAttribute("mapViTri", dao.getMapViTriKho());
        request.getRequestDispatcher("quanlyhanghoa.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HangHoaDAO dao = new HangHoaDAO();

        String action = request.getParameter("action");
        String maHang = request.getParameter("maHang");
        String tenHang = request.getParameter("tenHang");
        String dvt = request.getParameter("dvt");
        int soLuong = Integer.parseInt(request.getParameter("soLuongTonKho"));
        String quyCach = request.getParameter("quyCach");
        int hanMuc = Integer.parseInt(request.getParameter("hanMucTonToiThieu"));
        String maLoai = request.getParameter("maLoai");
        String maViTri = request.getParameter("maViTri");

        // Chặn trùng tên
        if (dao.kiemTraTrungTen(tenHang, "edit".equals(action) ? maHang : null)) {
            response.sendRedirect("QuanLyHangHoaServlet?error=" + URLEncoder.encode("Tên hàng hóa đã tồn tại!", "UTF-8"));
            return;
        }

        HangHoa hh = new HangHoa(maHang, tenHang, dvt, soLuong, quyCach, hanMuc, maLoai, maViTri);

        if ("add".equals(action)) {
            dao.themHangHoa(hh);
        } else if ("edit".equals(action)) {
            dao.suaHangHoa(hh);
        }

        response.sendRedirect("QuanLyHangHoaServlet?success=1");
    }
}