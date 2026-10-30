<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, model.PhieuKiemKe" %>
<%@ page import="model.taikhoan" %>
<%
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
    <title>Duyệt Kiểm Kê - Minh Phát WMS</title>
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body { font-family: 'Segoe UI', Tahoma, sans-serif; display: flex; height: 100vh; background: #f4f7f6; }
        
        /* Sidebar */
      .sidebar { width: 250px; min-width: 250px; flex: 0 0 250px; background-color: #ffffff; border-right: 1px solid #e0e0e0; padding: 20px 0; overflow-y: auto; }
        .sidebar-logo { padding: 20px; font-weight: bold; font-size: 18px; color: #333; }
        .menu-title { padding: 10px 20px; color: #7f8c8d; font-size: 12px; text-transform: uppercase; }
        .menu-item { display: block; padding: 12px 20px; color: #333; text-decoration: none; }
        .menu-item:hover { background: #f4f7f6; }
        .menu-item.active { background: #e8eaf6; color: #3f51b5; border-left: 4px solid #3f51b5; }
        
        /* Main */
        .main-content { flex: 1; display: flex; flex-direction: column; }
        .header { background: #fff; padding: 15px 20px; border-bottom: 1px solid #eee; text-align: right; }
        .container { padding: 20px; }
        .table-container { background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.05); }
        table { width: 100%; border-collapse: collapse; margin-top: 15px; }
        th, td { padding: 12px; border-bottom: 1px solid #eee; text-align: left; }
        
        /* Modal */
        .modal-overlay { display: none; position: fixed; top:0; left:0; width:100%; height:100%; background:rgba(0,0,0,0.5); z-index:1000; }
        .modal-content { background: white; width: 600px; margin: 50px auto; padding: 25px; border-radius: 8px; }
        .btn { padding: 8px 15px; border: none; border-radius: 4px; cursor: pointer; }
        .btn-info { background: #3498db; color: white; }
        .btn-duyet { background: #27ae60; color: white; }
        .btn-tuchoi { background: #e74c3c; color: white; }
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
             <a href="CongNoServlet" class="menu-item">Quản lý Công nợ</a>
              <a href="DuyetKiemKeServlet" class="menu-item  active">Duyệt Kiểm Kê</a>
        <% } %>
       
    </div>

    <div class="main-content">
        <div class="header">
            Trần Văn Tùng | Kế toán | <a href="Logout">Đăng xuất</a>
        </div>
        
        <div class="container">
            <div class="table-container">
                <h2>Danh Sách Phiếu Chờ Duyệt</h2>
                <table>
                    <thead>
                        <tr><th>Mã Phiếu</th><th>Ngày Kiểm</th><th>Người Lập</th><th>Thao Tác</th></tr>
                    </thead>
                    <tbody>
                        <% List<PhieuKiemKe> list = (List<PhieuKiemKe>) request.getAttribute("danhSachPhieu");
                           if (list != null) { for (PhieuKiemKe p : list) { %>
                        <tr>
                            <td><%= p.getMaKiemKe() %></td>
                            <td><%= p.getNgayKiem() %></td>
                            <td><%= p.getMaNV() %></td>
                            <td><button class="btn btn-info" onclick="openModal('<%= p.getMaKiemKe() %>')">🔍 Xem chi tiết</button></td>
                        </tr>
                        <% } } %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <div id="modalChiTiet" class="modal-overlay">
        <div class="modal-content">
            <h3>Chi tiết phiếu: <span id="maPhieuDisplay"></span></h3>
            <table border="1" style="width:100%; margin:15px 0; border-collapse:collapse;">
                <thead><tr><th>Tên hàng</th><th>Số lượng</th><th>Lý do</th></tr></thead>
                <tbody id="chiTietTableBody"></tbody>
            </table>
            <div style="text-align: right;">
                <button onclick="submitHanhDong('Đã duyệt')" class="btn btn-duyet">✔ Duyệt</button>
                <button onclick="submitHanhDong('Từ chối')" class="btn btn-tuchoi">✖ Từ chối</button>
                <button onclick="document.getElementById('modalChiTiet').style.display='none'" class="btn">Đóng</button>
            </div>
        </div>
    </div>

    <form id="actionForm" action="KiemKeDuyetServlet" method="POST" style="display:none">
        <input type="hidden" name="maKiemKe" id="inputMa">
        <input type="hidden" name="hanhDong" id="inputHanhDong">
    </form>

    <script>
        function openModal(ma) {
            document.getElementById('maPhieuDisplay').innerText = ma;
            fetch('GetChiTietPhieuServlet?maKiemKe=' + ma)
            .then(res => res.text())
            .then(html => {
                document.getElementById('chiTietTableBody').innerHTML = html;
                document.getElementById('modalChiTiet').style.display = 'block';
            });
        }
        function submitHanhDong(hanhDong) {
            document.getElementById('inputMa').value = document.getElementById('maPhieuDisplay').innerText;
            document.getElementById('inputHanhDong').value = hanhDong;
            document.getElementById('actionForm').submit();
        }
    </script>
    <%
        String thongBao = (String) session.getAttribute("thongBao");
        if (thongBao != null) {
    %>
        <script>
            // Hiện thông báo popup
            alert("<%= thongBao %>");
        </script>
    <%
            // Xóa thông báo đi để f5 không bị hiện lại
            session.removeAttribute("thongBao");
        }
    %>
</body>
</html>