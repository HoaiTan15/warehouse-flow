package controller;

import dao.KiemKeDuyetDAO;
import model.PhieuKiemKe;
import model.taikhoan;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/KiemKeDuyetServlet")
public class KiemKeDuyetServlet extends HttpServlet {
    
    // Hiển thị danh sách (khi load trang)
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        taikhoan acc = (taikhoan) request.getSession().getAttribute("account");
        if (acc == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        KiemKeDuyetDAO dao = new KiemKeDuyetDAO();
        List<PhieuKiemKe> danhSach = dao.getDanhSachChoXetDuyet();

        request.setAttribute("danhSachPhieu", danhSach);
        // SỬA LẠI TÊN FILE JSP CHO ĐÚNG VỚI FILE CỦA BẠN
        request.getRequestDispatcher("quanlyduyetkiemke.jsp").forward(request, response); 
    }

    // Xử lý khi bấm nút "Duyệt" hoặc "Từ chối" ở Modal
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
    request.setCharacterEncoding("UTF-8");
    
    // 1. Lấy dữ liệu từ Form ẩn
    String maKiemKe = request.getParameter("maKiemKe");
    String hanhDong = request.getParameter("hanhDong"); // "Đã duyệt" hoặc "Từ chối"

    // 2. Gọi DAO để cập nhật Database
    if (maKiemKe != null && hanhDong != null) {
        KiemKeDuyetDAO dao = new KiemKeDuyetDAO();
        boolean isSuccess = dao.capNhatTrangThai(maKiemKe, hanhDong);
        
        // 3. Tạo thông báo để hiển thị bên JSP
        if (isSuccess) {
            request.getSession().setAttribute("thongBao", "Thao tác [" + hanhDong + "] thành công cho phiếu " + maKiemKe);
        } else {
            request.getSession().setAttribute("thongBao", "Lỗi: Không thể cập nhật trạng thái!");
        }
    }

    // 4. Load lại trang danh sách (Để nó gọi lại doGet và load lại bảng mới)
    // Lưu ý: Đổi URL này cho khớp với mapping Servlet hiển thị danh sách của bạn
    response.sendRedirect("LoadKiemKeDuyetServlet"); 
}
}