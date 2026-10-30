package controller;

import dao.PhieuXuatDAO;
import model.PhieuXuatKho;
import model.taikhoan;
import java.io.IOException;
import java.sql.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/LuuPhieuXuatServlet")
public class LuuPhieuXuatServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        // 1. BẢO MẬT: Kiểm tra xem người dùng đã đăng nhập chưa
        HttpSession session = request.getSession();
        taikhoan acc = (taikhoan) session.getAttribute("account");
        if (acc == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        // 2. Lấy thông tin chung của Phiếu Xuất từ form
        String maPhieu = request.getParameter("maPhieu");
        String ngayLapStr = request.getParameter("ngayLap");
        String maDeNghi = request.getParameter("maDeNghi");
        String boPhanNhan = request.getParameter("boPhanNhan");
        String mucDich = request.getParameter("mucDich");

        // 3. Lấy mảng các mặt hàng mà Thủ kho đã bấm "Thêm" vào bảng
        String[] maHangArr = request.getParameterValues("maHang[]");
        String[] soLuongArr = request.getParameterValues("soLuong[]");
        String[] donGiaArr = request.getParameterValues("donGia[]");

        // Chặn lỗi: Nếu Thủ kho chưa thêm mặt hàng nào mà đã bấm Lưu
        if (maHangArr == null || maHangArr.length == 0) {
            request.setAttribute("thongBaoLoi", "Vui lòng thêm ít nhất 1 mặt hàng vào danh sách xuất!");
            request.getRequestDispatcher("lapphieuxuat.jsp").forward(request, response);
            return;
        }

        // 4. Gói dữ liệu vào Model
        PhieuXuatKho px = new PhieuXuatKho();
        px.setMaPhieu(maPhieu);
        px.setNgayLap(Date.valueOf(ngayLapStr)); // Chuyển chuỗi ngày sang kiểu Date của SQL
        px.setMaDeNghi(maDeNghi);
        px.setBoPhanNhan(boPhanNhan);
        px.setMucDich(mucDich);

        // 5. Đẩy xuống DAO để tiến hành lưu vào SQL Server
        PhieuXuatDAO dao = new PhieuXuatDAO();
        String ketQua = dao.luuPhieuXuatMoi(px, acc.getTenDangNhap(), maHangArr, soLuongArr, donGiaArr);

        // 6. Xử lý kết quả trả về từ SQL
        if ("SUCCESS".equals(ketQua)) {
            // LƯU THÀNH CÔNG: Chuyển thẳng sang trang Quản lý xuất kho kèm thông báo
            response.sendRedirect("LoadDanhSachPhieuXuatServlet?msg=success");
        } else {
            // THẤT BẠI (Vd: kho thiếu hàng): Ở lại trang cũ và in lỗi đỏ ra màn hình
            request.setAttribute("thongBaoLoi", ketQua);
            request.getRequestDispatcher("lapphieuxuat.jsp").forward(request, response);
        }
    }
}