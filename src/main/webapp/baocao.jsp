<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, model.taikhoan, model.PhieuNhapDTO, model.PhieuXuatDTO, model.KiemKeDTO" %>
<%
    // 1. Kiểm tra đăng nhập
    taikhoan acc = (taikhoan) session.getAttribute("account");
    if(acc == null) { 
        response.sendRedirect("login.jsp"); 
        return; 
    }
    
    // 2. Lấy role và phân quyền Sidebar
    String role = acc.getVaiTro() != null ? acc.getVaiTro() : "";
    
    boolean showTaiKhoan     = role.equalsIgnoreCase("Admin");
    boolean showHangHoa      = role.equalsIgnoreCase("Thủ kho") || role.equalsIgnoreCase("Trưởng kho") || role.equalsIgnoreCase("Giám đốc");
    boolean showMenuNhapXuat = role.equalsIgnoreCase("Trưởng kho") || role.equalsIgnoreCase("Giám đốc");
    boolean showBaoCao       = role.equalsIgnoreCase("Thủ kho") || role.equalsIgnoreCase("Trưởng kho") || role.equalsIgnoreCase("Giám đốc") || role.equalsIgnoreCase("Admin") || role.equalsIgnoreCase("Kế toán");
    boolean showLichSu       = role.equalsIgnoreCase("Thủ kho") || role.equalsIgnoreCase("Trưởng kho") || role.equalsIgnoreCase("Giám đốc") || role.equalsIgnoreCase("Admin") || role.equalsIgnoreCase("Kế toán") ;
    boolean showKeToan       = role.equalsIgnoreCase("Kế toán") || role.equalsIgnoreCase("Giám đốc");
    // 3. Lấy dữ liệu hiển thị
    String loai = (String) request.getAttribute("loaiHienTai");
    if(loai == null) loai = "nhapkho";
    String filter = (String) request.getAttribute("filterHienTai");
    if(filter == null) filter = "all";
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Trung Tâm Báo Cáo Thống Kê</title>
    <style>
        /* CSS Layout: Sidebar & Topbar */
        body { margin: 0; padding: 0; display: flex; height: 100vh; background: #f5f6f8; font-family: 'Segoe UI', Tahoma, sans-serif; overflow: hidden; }
        
        .sidebar { width: 250px; min-width: 250px; flex: 0 0 250px; background-color: #ffffff; border-right: 1px solid #e0e0e0; padding: 20px 0; overflow-y: auto; }
        .sidebar-logo { padding: 20px; font-size: 20px; font-weight: bold; color: #2c3e50; border-bottom: 1px solid #eee; display: flex; align-items: center; gap: 10px; }
        .menu-title { padding: 15px 20px 5px; font-size: 11px; font-weight: bold; color: #888; text-transform: uppercase; }
        .menu-item { padding: 12px 20px; color: #555; text-decoration: none; font-size: 14px; display: block; font-weight: 500; transition: 0.2s; }
        .menu-item:hover { background-color: #e6efff; color: #4b49ac; }
        .menu-item.active { background-color: #e6efff; color: #4b49ac; border-right: 3px solid #4b49ac; font-weight: bold; }
        
        .main-content { flex: 1; display: flex; flex-direction: column; overflow-y: auto; }
        .topbar { background-color: #ffffff; min-height: 60px; display: flex; align-items: center; justify-content: space-between; padding: 0 30px; border-bottom: 1px solid #e0e0e0; }
        
        /* CSS Giao diện Card & Table của bạn */
        .card { background: #fff; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); padding: 25px; margin: 20px 30px; border: 1px solid #eee; }
        .filter-container { display: flex; gap: 15px; align-items: center; background: #f8f9fa; padding: 15px; border-radius: 5px; margin-bottom: 20px; border: 1px solid #eee; }
        table { width: 100%; border-collapse: collapse; margin-top: 15px; }
        th { background: #f8f9fa; padding: 12px; text-align: left; border-bottom: 2px solid #eee; color: #333; }
        td { padding: 12px; border-bottom: 1px solid #eee; font-size: 14px; }
        
        /* Badges & Buttons */
        .badge-green { background: #d4edda; color: #155724; padding: 5px 10px; border-radius: 4px; font-weight: bold; font-size: 12px; }
        .badge-red { background: #f8d7da; color: #721c24; padding: 5px 10px; border-radius: 4px; font-weight: bold; font-size: 12px; }
        .badge-yellow { background: #fff3cd; color: #856404; padding: 5px 10px; border-radius: 4px; font-weight: bold; font-size: 12px; }
        button { background-color: #4b49ac; color: white; border: none; padding: 8px 16px; border-radius: 4px; cursor: pointer; font-weight: bold; }
        button:hover { background-color: #3f3e91; }
        select, input { padding: 8px; border: 1px solid #ccc; border-radius: 4px; outline: none; }
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
            <a href="BaoCaoServlet" class="menu-item active">Báo cáo</a>
        <% } %>
        
        <% if(showLichSu) { %>
            <a href="LichSuServlet" class="menu-item">Lịch sử tồn kho</a>
        <% } %>
          <% if(showKeToan) { %>
             <a href="CongNoServlet" class="menu-item">Quản lý Công nợ</a>
              <a href="KiemKeDuyetServlet" class="menu-item">Duyệt Kiểm Kê</a>
        <% } %>
       
    </div>

    <div class="main-content">
        <div class="topbar">
            <div style="font-size: 16px; font-weight: bold; color: #333;">Hệ thống Quản lý Kho</div>
            <div style="font-size: 13px;">
                <b><%= acc.getHoTen() != null ? acc.getHoTen() : "Người dùng" %></b> | <%= acc.getVaiTro() != null ? acc.getVaiTro() : "Nhân viên" %> 
                <a href="LogoutServlet" style="color: #e74c3c; text-decoration: none; margin-left: 10px;">[Đăng xuất]</a>
            </div>
        </div>

        <div class="card">
            <form action="BaoCaoServlet" method="GET" class="filter-container">
                <label><strong>Loại Báo Cáo:</strong></label>
                <select name="loai" id="loaiSelect" onchange="updateFilterOptions()">
                    <option value="nhapkho" <%= "nhapkho".equals(loai) ? "selected" : "" %>>Báo Cáo Nhập Kho</option>
                    <option value="xuatkho" <%= "xuatkho".equals(loai) ? "selected" : "" %>>Báo Cáo Xuất Kho</option>
                    <option value="kiemke" <%= "kiemke".equals(loai) ? "selected" : "" %>>Báo Cáo Kiểm Kê</option>
                </select>

                <label><strong>Bộ Lọc:</strong></label>
                <select name="filter" id="filterSelect">
                    </select>
                
                <button type="submit">Lọc Dữ Liệu</button>
            </form>

            <table>
                <thead>
                    <tr>
                        <th>Mã Phiếu</th>
                        <th>Ngày Lập</th>
                        <th>Đối Tượng</th>
                        <th>Số Lượng</th>
                        <th>Trạng Thái</th>
                    </tr>
                </thead>
                <tbody>
    <% List<?> danhSach = (List<?>) request.getAttribute("danhSach");
       if(danhSach != null && !danhSach.isEmpty()) {
           for(Object obj : danhSach) { %>
            <tr>
                <% if (obj instanceof PhieuNhapDTO) { 
                    PhieuNhapDTO pn = (PhieuNhapDTO) obj; 
                    boolean isRejected = (pn.getGhiChu() != null && pn.getGhiChu().startsWith("Từ chối:"));
                    boolean isPending = (pn.getMaNguoiDuyet() == null || pn.getMaNguoiDuyet().trim().isEmpty());
                %>
                    <td><b><%= pn.getMaPhieu() %></b></td>
                    <td><%= pn.getNgayLap() %></td>
                    <td><%= pn.getTenNCC() %></td>
                    <td><%= pn.getSoLuong() %></td>
                    <td>
                        <% if(isRejected) { %> <span class="badge-red">❌ Từ chối</span>
                        <% } else if(isPending) { %> <span class="badge-yellow">⏳ Chờ duyệt</span>
                        <% } else { %> <span class="badge-green">✅ Đã duyệt</span> <% } %>
                    </td>
                    
                <% } else if (obj instanceof PhieuXuatDTO) { 
                    PhieuXuatDTO px = (PhieuXuatDTO) obj; 
                    boolean isRejected = (px.getMucDich() != null && px.getMucDich().startsWith("Từ chối:"));
                    boolean isPending = (px.getMaNguoiDuyet() == null || px.getMaNguoiDuyet().trim().isEmpty());
                %>
                    <td><b><%= px.getMaPhieu() %></b></td>
                    <td><%= px.getNgayLap() %></td>
                    <td><%= px.getTenHang() %></td>
                    <td><%= px.getSoLuong() %></td>
                    <td>
                        <% if(isRejected) { %> <span class="badge-red">❌ Từ chối</span>
                        <% } else if(isPending) { %> <span class="badge-yellow">⏳ Chờ duyệt</span>
                        <% } else { %> <span class="badge-green">✅ Đã duyệt</span> <% } %>
                    </td>
                    
                <% } else if (obj instanceof KiemKeDTO) { 
                    KiemKeDTO k = (KiemKeDTO) obj; %>
                    <td><b><%= k.getMaKiemKe() %></b></td>
                    <td><%= k.getNgayKiem() %></td>
                    <td><%= k.getTenHang() %></td>
                    <td><%= k.getThucTe() %></td>
                    <td>
                        <% if("Đã duyệt".equals(k.getTrangThai())) { %> <span class="badge-green">✅ Đã duyệt</span>
                        <% } else if("Từ chối".equals(k.getTrangThai())) { %> <span class="badge-red">❌ Từ chối</span>
                        <% } else { %> <span class="badge-yellow">⏳ Chờ duyệt</span> <% } %>
                    </td>
                <% } %>
            </tr>
    <%     } 
       } else { %>
        <tr><td colspan="5" style="text-align:center;">Không có dữ liệu phù hợp.</td></tr>
    <% } %>
</tbody>
            </table>
        </div>
    </div>

    <script>
        function updateFilterOptions() {
            var loai = document.getElementById("loaiSelect").value;
            var filterSelect = document.getElementById("filterSelect");
            var currentFilterValue = filterSelect.value; // Lưu lại lựa chọn hiện tại nếu có
            
            // Xóa hết option cũ
            filterSelect.innerHTML = "";
            
            // Thêm option mặc định
            filterSelect.options.add(new Option("Tất cả", "all"));
            filterSelect.options.add(new Option("Đã duyệt", "daduyet"));
            filterSelect.options.add(new Option("Từ chối", "tuchoi"));
            
            // Nếu là kiểm kê, thêm option Chờ duyệt
            if (loai === "kiemke") {
                filterSelect.options.add(new Option("Chờ duyệt", "choduyet"));
            }
            
            // Cố gắng giữ lại giá trị đang được chọn (tránh bị reset về "Tất cả" khi người dùng chỉ đổi loại báo cáo)
            for(var i = 0; i < filterSelect.options.length; i++) {
                if(filterSelect.options[i].value === currentFilterValue) {
                    filterSelect.selectedIndex = i;
                    break;
                }
            }
        }

        // Chạy lần đầu khi load trang để render đúng option và select đúng bộ lọc từ server trả về
        window.onload = function() {
            updateFilterOptions();
            var serverFilter = "<%= filter %>";
            var filterSelect = document.getElementById("filterSelect");
            
            for(var i = 0; i < filterSelect.options.length; i++) {
                if(filterSelect.options[i].value === serverFilter) {
                    filterSelect.selectedIndex = i;
                    break;
                }
            }
        };
    </script>
</body>
</html>