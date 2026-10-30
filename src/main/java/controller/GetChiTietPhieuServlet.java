package controller;

import dao.KiemKeDuyetDAO;
import model.KiemKeDTO; // Giả sử class này chứa thông tin chi tiết hàng hóa
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/GetChiTietPhieuServlet")
public class GetChiTietPhieuServlet extends HttpServlet {
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        String maKiemKe = request.getParameter("maKiemKe");
        
        // Gọi DAO để lấy danh sách chi tiết (Bạn cần đảm bảo đã có hàm này trong KiemKeDuyetDAO)
        KiemKeDuyetDAO dao = new KiemKeDuyetDAO();
        List<KiemKeDTO> listChiTiet = dao.getChiTietPhieu(maKiemKe);
        
        StringBuilder html = new StringBuilder();
        if (listChiTiet != null && !listChiTiet.isEmpty()) {
            for (KiemKeDTO item : listChiTiet) {
                html.append("<tr>")
                    .append("<td>").append(item.getTenHang()).append("</td>")
                    .append("<td>").append(item.getThucTe()).append("</td>")
                    .append("<td>").append(item.getNguyenNhan()).append("</td>")
                    .append("</tr>");
            }
        } else {
            html.append("<tr><td colspan='3'>Không có dữ liệu chi tiết</td></tr>");
        }
        
        response.getWriter().write(html.toString());
    }
}