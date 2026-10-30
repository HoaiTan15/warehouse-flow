package controller;

import dao.HangHoaDAO;
import model.HangHoa;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/TimKiemHangHoaServlet")
public class TimKiemHangHoaServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Setup để đọc chuẩn tiếng Việt có dấu từ thanh tìm kiếm
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");
        if (request.getSession(false) == null || request.getSession(false).getAttribute("account") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        // 1. Lấy cái chữ mà Thủ kho vừa nhập vào ô tìm kiếm (biến name="tuKhoa" trong form)
        String tuKhoa = request.getParameter("tuKhoa");

        // 2. Gọi DAO đem từ khóa đó xuống SQL Server để lục lọi
        HangHoaDAO dao = new HangHoaDAO();
        List<HangHoa> listSearch = dao.searchHangHoa(tuKhoa);

        // 3. Đóng gói kết quả tìm được vào cái túi "danhSachHH" (Giống hệt tên túi ở lúc Load danh sách)
        request.setAttribute("listHH", listSearch);
        request.setAttribute("mapLoai", dao.getMapLoaiHang());
        request.setAttribute("mapViTri", dao.getMapViTriKho());

        // 4. Quăng cái túi dữ liệu đó trả ngược lại về trang quanlyhanghoa.jsp để nó in ra bảng
        request.getRequestDispatcher("quanlyhanghoa.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
