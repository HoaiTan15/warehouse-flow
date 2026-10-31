package controller;

import dao.HangHoaDAO;
import model.HangHoa;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/QuanLyHangHoaServlet")
public class QuanLyHangHoaServlet extends HttpServlet {

    private HangHoaDAO hangHoaDAO;

    @Override
    public void init() throws ServletException {
        hangHoaDAO = new HangHoaDAO();
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
        } else {
            response.sendRedirect("LoadDanhSachKhoServlet");
        }
    }

    private void themHangHoa(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String maHang = request.getParameter("maHang");
            String tenHang = request.getParameter("tenHang");
            String dvt = request.getParameter("dvt");
            int soLuong = docSoNguyenKhongAm(request, "soLuongTonKho", 0);
            String quyCach = request.getParameter("quyCach");
            int hanMuc = docSoNguyenKhongAm(request, "hanMucTonToiThieu", null);
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

            boolean success = hangHoaDAO.themHangHoa(hh);
            response.sendRedirect("LoadDanhSachKhoServlet?" + (success ? "success=add" : "error=add"));
        } catch (IllegalArgumentException e) {
            response.sendRedirect("LoadDanhSachKhoServlet?error=invalid");
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
            int soLuong = docSoNguyenKhongAm(request, "soLuongTonKho", null);
            String quyCach = request.getParameter("quyCach");
            int hanMuc = docSoNguyenKhongAm(request, "hanMucTonToiThieu", null);
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

            boolean success = hangHoaDAO.suaHangHoa(hh);
            response.sendRedirect("LoadDanhSachKhoServlet?" + (success ? "success=edit" : "error=edit"));
        } catch (IllegalArgumentException e) {
            response.sendRedirect("LoadDanhSachKhoServlet?error=invalid");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("LoadDanhSachKhoServlet?error=exception");
        }
    }

    private int docSoNguyenKhongAm(HttpServletRequest request, String tenTruong, Integer macDinh) {
        String value = request.getParameter(tenTruong);
        if (value == null || value.trim().isEmpty()) {
            if (macDinh != null) return macDinh;
            throw new IllegalArgumentException("Thiếu " + tenTruong);
        }
        int number = Integer.parseInt(value.trim());
        if (number < 0) throw new IllegalArgumentException(tenTruong + " phải không âm");
        return number;
    }

    private void xoaHangHoa(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String maHang = request.getParameter("id");
            String result = hangHoaDAO.xoaHangHoa(maHang);
            if ("SUCCESS".equals(result)) response.sendRedirect("LoadDanhSachKhoServlet?success=delete");
            else if ("STOCK".equals(result)) response.sendRedirect("LoadDanhSachKhoServlet?error=stock");
            else if ("RELATED".equals(result)) response.sendRedirect("LoadDanhSachKhoServlet?error=related");
            else response.sendRedirect("LoadDanhSachKhoServlet?error=delete");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("LoadDanhSachKhoServlet?error=exception");
        }
    }
}
