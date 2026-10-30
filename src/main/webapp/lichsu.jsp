<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="model.taikhoan" %>
<%@ page import="model.HangHoa" %>
<%@ page import="model.LichSuDTO" %>
<%
 // 1. KÉO THÔNG TIN TÀI KHOẢN TỪ SESSION 
    taikhoan acc = (taikhoan) session.getAttribute("account");
    if(acc == null) { response.sendRedirect("login.jsp"); return; }
    
     // 2. Lấy role và phân quyền Sidebar
    String role = acc.getVaiTro() != null ? acc.getVaiTro() : "";
    
    boolean showTaiKhoan     = role.equalsIgnoreCase("Admin");
    boolean showHangHoa      = role.equalsIgnoreCase("Thủ kho") || role.equalsIgnoreCase("Trưởng kho") || role.equalsIgnoreCase("Giám đốc");
    boolean showMenuNhapXuat = role.equalsIgnoreCase("Trưởng kho") || role.equalsIgnoreCase("Giám đốc");
    boolean showBaoCao       = role.equalsIgnoreCase("Thủ kho") || role.equalsIgnoreCase("Trưởng kho") || role.equalsIgnoreCase("Giám đốc") || role.equalsIgnoreCase("Admin") || role.equalsIgnoreCase("Kế toán");
    boolean showLichSu       = role.equalsIgnoreCase("Thủ kho") || role.equalsIgnoreCase("Trưởng kho") || role.equalsIgnoreCase("Giám đốc") || role.equalsIgnoreCase("Admin") || role.equalsIgnoreCase("Kế toán") ;
    boolean showKeToan       = role.equalsIgnoreCase("Kế toán") || role.equalsIgnoreCase("Giám đốc");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Lịch Sử Kho Hàng - WMS</title>
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Segoe UI', Arial, sans-serif; }
        body { display: flex; background-color: #f5f6f8; height: 100vh; overflow: hidden; }
        .sidebar { width: 250px; background-color: #ffffff; border-right: 1px solid #e0e0e0; padding: 20px 0; overflow-y: auto;}
        .sidebar-logo { font-size: 20px; font-weight: bold; color: #2c3e50; padding: 0 20px 20px; border-bottom: 1px solid #eee; margin-bottom: 10px; display: flex; align-items: center; gap: 10px;}
        .menu-title { font-size: 11px; color: #888; font-weight: bold; padding: 10px 20px; text-transform: uppercase; }
        .menu-item { padding: 12px 20px; color: #555; text-decoration: none; display: flex; align-items: center; gap: 10px; font-weight: 500; font-size: 14px; transition: 0.2s;}
        .menu-item:hover { background-color: #f0f4ff; color: #4b49ac; }
        .menu-item.active { background-color: #e6efff; color: #4b49ac; border-right: 3px solid #4b49ac; font-weight: bold;}

        .main-content { flex: 1; display: flex; flex-direction: column; overflow-y: auto; }
        .topbar { height: 60px; background-color: #ffffff; border-bottom: 1px solid #e0e0e0; display: flex; justify-content: space-between; align-items: center; padding: 0 30px; }
        .user-info { display: flex; align-items: center; gap: 10px; font-size: 13px; }
        .badge-role { background-color: #eaf4fc; color: #3498db; padding: 3px 8px; border-radius: 12px; font-size: 11px; font-weight: bold; margin-left: 5px; }

        .content-body { padding: 30px; }
        .page-header { margin-bottom: 25px; }
        .page-title { font-size: 22px; font-weight: bold; color: #2c3e50; display: flex; align-items: center; gap: 10px; }
        .page-subtitle { font-size: 13px; color: #7f8c8d; margin-top: 5px; margin-left: 35px;}

        .filter-card { background: white; padding: 15px 25px; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.02); margin-bottom: 20px; border: 1px solid #eee; display: flex; align-items: center; gap: 15px;}
        .form-control { padding: 10px; border: 1px solid #dcdde1; border-radius: 5px; font-size: 14px; outline: none; min-width: 300px; }
        .btn-filter { background-color: #f8f9fa; border: 1px solid #dcdde1; padding: 10px 15px; border-radius: 5px; cursor: pointer; font-weight: bold; color: #333;}
        .btn-filter:hover { background-color: #e9ecef; }

        .table-card { background: white; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.02); border: 1px solid #eee; overflow: hidden; }
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 15px; text-align: left; border-bottom: 1px solid #f1f2f6; font-size: 14px;}
        th { background-color: #fcfcfd; color: #333; font-weight: bold; }
        tr:hover { background-color: #fcfcfd; }
        
        /* CỘT THAY ĐỔI VÀ BADGE CHỨNG TỪ */
        .text-green { color: #2ecc71; font-weight: bold; }
        .text-red { color: #e74c3c; font-weight: bold; }
        .text-orange { color: #f39c12; font-weight: bold; }
        .badge-chungtu { background-color: #eaf4fc; color: #3498db; padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: bold; letter-spacing: 0.5px;}
    </style>
</head>
<body>

<!-- ================= SIDEBAR ================= -->
       <div class="sidebar">
        <div class="sidebar-logo">📦 Minh Phát</div>
        <div class="menu-title">MENU CHÍNH</div>
        
        <a href="home.jsp" class="menu-item">Dashboard</a>
        
         <% if(showMenuNhapXuat) { %>
            <a href="QuanLyDanhSachNhapServlet" class="menu-item">Quản lý Nhập kho</a>
            <a href="QuanLyDanhSachXuatServlet" class="menu-item">Quản lý Xuất kho</a>
             <a href="QuanLyNhaCungCapServlet" class="menu-item">Nhà cung cấp</a>
        <% } %>

        <% if(showHangHoa) { %>
            <a href="LoadDanhSachKhoServlet" class="menu-item">Quản lý hàng hóa</a>
            <a href="KiemKeServlet" class="menu-item">Kiểm kê</a>
        <% } %>
        
        <% if(showBaoCao) { %>
            <a href="BaoCaoServlet" class="menu-item">Báo cáo</a>
        <% } %>
        
        <% if(showLichSu) { %>
            <a href="LichSuServlet" class="menu-item active">Lịch sử tồn kho</a>
        <% } %>
          <% if(showKeToan) { %>
             <a href="CongNoServlet" class="menu-item">Quản lý Công nợ</a>
               <a href="KiemKeDuyetServlet" class="menu-item">Duyệt Kiểm Kê</a>
        <% } %>

     <% if(showTaiKhoan) { %>
         <div class="menu-title">HỆ THỐNG</div>
            <a href="QuanLyTaiKhoanServlet" class="menu-item active">Quản lý tài khoản</a>
        <% } %>
    </div>
    <div class="main-content">
        <div class="topbar">
            <div style="font-weight: bold; font-size: 16px; color: #333;">Hệ thống Quản lý Kho</div>
            <div class="user-info">
                <b><%= acc.getHoTen() != null ? acc.getHoTen() : acc.getTenDangNhap() %></b>
                <span class="badge-role"><%= acc.getVaiTro() %></span>
                <a href="LogoutServlet" style="color: #e74c3c; text-decoration: none; font-size: 18px; margin-left: 10px;">🚪</a>
            </div>
        </div>

        <div class="content-body">
            <div class="page-header">
                <div class="page-title"><span>🕒</span> Lịch Sử Biến Động Tồn Kho</div>
                <div class="page-subtitle">Sổ kho ghi lại mọi thay đổi số lượng tồn kho theo thời gian</div>
            </div>

            <form action="LichSuServlet" method="GET" class="filter-card">
                <span style="font-size: 18px; color: #888;">⚲</span>
                <% String maHangLoc = (String) request.getAttribute("maHangHienTai"); %>
                <select name="maHangLoc" class="form-control" onchange="this.form.submit()">
                    <option value="">-- Tất cả mặt hàng --</option>
                    <%
                        List<HangHoa> listHH = (List<HangHoa>) request.getAttribute("danhSachHH");
                        if(listHH != null) {
                            for(HangHoa hh : listHH) {
                               String sel = (maHangLoc != null && maHangLoc.equals(hh.getMaHang())) ? "selected" : "";
                    %>
                    <option value="<%= hh.getMaHang() %>" <%= sel %>><%= hh.getMaHang() %> - <%= hh.getTenHang() %></option>
                    <%      }
                        } 
                    %>
                </select>
                <noscript><button type="submit" class="btn-filter">Lọc</button></noscript>
            </form>

            <div class="table-card">
                <table>
                    <thead>
                        <tr>
                            <th>Thời gian</th>
                            <th>Mã Hàng</th>
                            <th>Tên Hàng</th>
                            <th>Loại</th>
                            <th style="text-align: center;">Thay đổi</th>
                            <th style="text-align: center;">Tồn sau</th>
                            <th>Chứng từ</th>
                            <th>Ghi chú</th>
                        </tr>
                    </thead>
                   <tbody>
                        <% 
                            List<LichSuDTO> dsLichSu = (List<LichSuDTO>) request.getAttribute("danhSachLichSu");
                            if(dsLichSu != null && !dsLichSu.isEmpty()) {
                                for(LichSuDTO ls : dsLichSu) {
                                    String colorSoLuong = "";
                                    String styleLoai = "color: #3498db; font-weight: bold;"; // Xanh dương cho Kiểm kê
                                    String icon = "⟲";
                                    String prefix = "";
                                    
                                    if(ls.getThayDoi() > 0) { 
                                        colorSoLuong = "text-green"; // Cộng thì màu Xanh
                                        styleLoai = "color: #2ecc71; font-weight: bold;"; 
                                        icon = "↓"; 
                                        prefix = "+";
                                    } else if (ls.getThayDoi() < 0) { 
                                        colorSoLuong = "text-red";   // Trừ thì màu Đỏ
                                        styleLoai = "color: #f39c12; font-weight: bold;"; // Chữ Xuất màu Cam
                                        icon = "↑"; 
                                    } else {
                                        colorSoLuong = "text-orange"; // Kiểm kê không lệch màu cam
                                    }
                        %>
                        <tr>
                            <td style="color: #666;"><%= ls.getThoiGian() %></td>
                            <td><b><%= ls.getMaHang() %></b></td>
                            <td><%= ls.getTenHang() %></td>
                            <!-- Cột Loại được tô màu thanh lịch -->
                            <td style="<%= styleLoai %>"><%= icon %> <%= ls.getLoai() %></td>
                            <!-- Cột Số lượng thay đổi -->
                            <td class="<%= colorSoLuong %>" style="text-align: center; font-size: 15px;"><%= prefix %><%= ls.getThayDoi() %></td>
                            <td style="text-align: center; font-weight: bold;"><%= ls.getTonSau() %></td>
                            <td><span class="badge-chungtu"><%= ls.getChungTu() %></span></td>
                            <td style="color: #888; font-style: italic; font-size: 13px;"><%= ls.getGhiChu() != null ? ls.getGhiChu() : "" %></td>
                        </tr>
                        <%      }
                            } else { out.print("<tr><td colspan='8' style='text-align:center; padding: 30px; color:#888;'>Chưa có dữ liệu biến động kho nào.</td></tr>"); }
                        %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

</body>
</html>