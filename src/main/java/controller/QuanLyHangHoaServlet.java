package controller;

import dao.HangHoaDAO;
import model.HangHoa;
import nosql.mongo.NhatKyThaoTacRepository;
import nosql.redis.CacheService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/QuanLyHangHoaServlet")
public class QuanLyHangHoaServlet extends HttpServlet {

    private HangHoaDAO hangHoaDAO;
    private NhatKyThaoTacRepository nhatKyRepo;

    @Override
    public void init() throws ServletException {
        hangHoaDAO = new HangHoaDAO();
        try {
            nhatKyRepo = new NhatKyThaoTacRepository();
        } catch (Exception e) {
            System.err.println("Failed to initialize NhatKyThaoTacRepository: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");

        if ("add".equals(action)) {
            themHangHoa(request, response);
        } else if ("edit".equals(action)) {
            suaHangHoa(request, response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("delete".equals(action)) {
            xoaHangHoa(request, response);
        }
    }

    private void themHangHoa(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String maHang = request.getParameter("maHang");
            String tenHang = request.getParameter("tenHang");
            String dvt = request.getParameter("dvt");
            int soLuong = Integer.parseInt(request.getParameter("soLuong"));
            String quyCach = request.getParameter("quyCach");
            int hanMuc = Integer.parseInt(request.getParameter("hanMuc"));
            String maLoai = request.getParameter("maLoai");
            String maViTri = request.getParameter("maViTri");

            HangHoa hh = new HangHoa();
            hh.setMaHang(maHang);
            hh.setTenHang(tenHang);
            hh.setDvt(dvt);
            hh.setSoLuongTonKho(soLuong);
            hh.setQuyCach(quyCach);
            hh.setHanMucTonToiThieu(hanMuc);
            hh.setMaLoai(maLoai);
            hh.setMaViTri(maViTri);

            hangHoaDAO.themHangHoa(hh);
            response.sendRedirect("LoadDanhSachKhoServlet?success=add");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("LoadDanhSachKhoServlet?error=exception");
        }
    }

    private void suaHangHoa(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String maHang = request.getParameter("maHang");
            String tenHang = request.getParameter("tenHang");
            String dvt = request.getParameter("dvt");
            int soLuong = Integer.parseInt(request.getParameter("soLuong"));
            String quyCach = request.getParameter("quyCach");
            int hanMuc = Integer.parseInt(request.getParameter("hanMuc"));
            String maLoai = request.getParameter("maLoai");
            String maViTri = request.getParameter("maViTri");

            HangHoa hh = new HangHoa();
            hh.setMaHang(maHang);
            hh.setTenHang(tenHang);
            hh.setDvt(dvt);
            hh.setSoLuongTonKho(soLuong);
            hh.setQuyCach(quyCach);
            hh.setHanMucTonToiThieu(hanMuc);
            hh.setMaLoai(maLoai);
            hh.setMaViTri(maViTri);

            hangHoaDAO.suaHangHoa(hh);
            response.sendRedirect("LoadDanhSachKhoServlet?success=edit");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("LoadDanhSachKhoServlet?error=exception");
        }
    }

    private void xoaHangHoa(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String maHang = request.getParameter("id");
            String soLuong = request.getParameter("sl");

            // Kiểm tra số lượng tồn kho
            if (soLuong != null && Integer.parseInt(soLuong) > 0) {
                response.sendRedirect("LoadDanhSachKhoServlet?error=stock");
                return;
            }

            boolean success = "SUCCESS".equals(hangHoaDAO.xoaHangHoa(maHang));

            if (success) {
                // Ghi nhật ký MongoDB
                if (nhatKyRepo != null) {
                    try {
                        nhatKyRepo.ghiNhatKy("XOA_HANG_HOA", maHang, "SYSTEM", "THANH_CONG", "Xóa hàng hóa: " + maHang);
                    } catch (Exception e) {
                        System.err.println("Failed to log to MongoDB: " + e.getMessage());
                    }
                }

                // Xóa cache Redis
                CacheService.invalidateHangHoaCache();

                response.sendRedirect("LoadDanhSachKhoServlet?success=delete");
            } else {
                response.sendRedirect("LoadDanhSachKhoServlet?error=delete");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("LoadDanhSachKhoServlet?error=exception");
        }
    }
}
