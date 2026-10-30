<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, model.CongNo, model.taikhoan, java.text.DecimalFormat" %>
<% 
    taikhoan acc = (taikhoan) session.getAttribute("account");
    if(acc == null) { response.sendRedirect("login.jsp"); return; }
    DecimalFormat df = new DecimalFormat("#,###");
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
    <title>Quản Lý Công Nợ - WMS</title>
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Segoe UI', Arial, sans-serif; }
        body { display: flex; background-color: #f5f6f8; height: 100vh; }
        
        /* Sidebar */
        .sidebar { width: 250px; min-width: 250px; flex: 0 0 250px; background-color: #ffffff; border-right: 1px solid #e0e0e0; padding: 20px 0; overflow-y: auto; }
        .sidebar-logo { font-weight: bold; color: #2c3e50; padding: 0 20px 20px; font-size: 20px; border-bottom: 1px solid #eee; margin-bottom: 10px; }
        .menu-item { padding: 12px 20px; color: #555; text-decoration: none; display: block; font-weight: 500; }
        .menu-item.active { background-color: #e6efff; color: #4b49ac; border-right: 3px solid #4b49ac; }
        
        /* Main Content */
        .main-content { flex: 1; display: flex; flex-direction: column; }
        .topbar { height: 60px; background: #fff; border-bottom: 1px solid #e0e0e0; display: flex; justify-content: space-between; align-items: center; padding: 0 30px; }
        .content-body { padding: 30px; }
        
        /* Table Styles */
        .card { background: #fff; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); padding: 25px; }
        table { width: 100%; border-collapse: collapse; margin-top: 20px; }
        th { background: #f8f9fa; padding: 15px; border-bottom: 2px solid #eee; text-align: left; }
        td { padding: 15px; border-bottom: 1px solid #eee; }
        .btn-pay { background: #27ae60; color: white; border: none; padding: 6px 12px; cursor: pointer; border-radius: 4px; font-weight: bold; }
        .status-unpaid { color: #e74c3c; font-weight: bold; }
        .status-paid { color: #27ae60; font-weight: bold; }
    </style>
</head>
<body>
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
            <a href="LichSuServlet" class="menu-item">Lịch sử tồn kho</a>
        <% } %>
          <% if(showKeToan) { %>
             <a href="CongNoServlet" class="menu-item  active">Quản lý Công nợ</a>
              <a href="KiemKeDuyetServlet" class="menu-item">Duyệt Kiểm Kê</a>
        <% } %>
       
    </div>

    <div class="main-content">
        <div class="topbar">
            <div><b>Quản Lý Công Nợ Kế Toán</b></div>
            <div><%= acc.getHoTen() %> | <a href="LogoutServlet" style="color:red">Đăng xuất</a></div>
        </div>

        <div class="content-body">
            <div class="card">
                <h2>Danh Sách Công Nợ</h2>
                <table>
                    <thead>
                        <tr>
                            <th>Mã Công Nợ</th><th>Mã Phiếu</th><th>Loại</th>
                            <th>Phải Trả</th><th>Đã Trả</th><th>Trạng thái</th><th>Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% 
                            List<CongNo> list = (List<CongNo>) request.getAttribute("listCongNo");
                            if(list != null) { for(CongNo cn : list) { 
                        %>
                        <tr>
                            <td><b><%= cn.getMaCongNo() %></b></td>
                            <td><%= cn.getMaPhieu() %></td>
                            <td><%= cn.getLoaiPhieu() %></td>
                            <td><%= df.format(cn.getSoTienPhaiTra()) %> đ</td>
                            <td><%= df.format(cn.getSoTienDaTra()) %> đ</td>
                            <td>
                                <% if(cn.getTrangThai().equals("Hoàn tất")) { %>
                                    <span class="status-paid">✅ Hoàn tất</span>
                                <% } else { %> 
                                    <span class="status-unpaid">⏳ Chưa thanh toán</span> 
                                <% } %>
                            </td>
                            <td>
                                <% if(!cn.getTrangThai().equals("Hoàn tất")) { %>
                                <form action="CongNoServlet" method="POST" style="display:flex; gap:5px;">
                                    <input type="hidden" name="action" value="pay">
                                    <input type="hidden" name="maCongNo" value="<%= cn.getMaCongNo() %>">
                                    <input type="number" name="soTien" min="0" placeholder="Số tiền..." required style="width:100px; padding:5px;">
                                    <button type="submit" class="btn-pay">THANH TOÁN</button>
                                </form>
                                <% } else { %> - <% } %>
                            </td>
                        </tr>
                        <% }} %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</body>
</html>