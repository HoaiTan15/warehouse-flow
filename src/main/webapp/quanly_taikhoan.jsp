<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="model.TaiKhoanDTO" %>
<%@ page import="model.taikhoan" %>
<%
    taikhoan acc = (taikhoan) session.getAttribute("account");
    if(acc == null || !acc.getVaiTro().equalsIgnoreCase("Admin")) { response.sendRedirect("login.jsp"); return; }
    
    String role = acc.getVaiTro() != null ? acc.getVaiTro() : "";
    boolean showTaiKhoan     = role.equalsIgnoreCase("Admin");
    boolean showHangHoa      = role.equalsIgnoreCase("Thủ kho") || role.equalsIgnoreCase("Trưởng kho") || role.equalsIgnoreCase("Giám đốc");
    boolean showMenuNhapXuat = role.equalsIgnoreCase("Trưởng kho") || role.equalsIgnoreCase("Giám đốc");
    boolean showBaoCao       = role.equalsIgnoreCase("Thủ kho") || role.equalsIgnoreCase("Trưởng kho") || role.equalsIgnoreCase("Giám đốc") || role.equalsIgnoreCase("Admin");
    boolean showLichSu       = role.equalsIgnoreCase("Thủ kho") || role.equalsIgnoreCase("Trưởng kho") || role.equalsIgnoreCase("Giám đốc") || role.equalsIgnoreCase("Admin");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Quản Lý Tài Khoản - WMS Admin</title>
    <style>
        /* CSS GIAO DIỆN CHUNG NHƯ CŨ */
        * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Segoe UI', Arial, sans-serif; }
        body { display: flex; background-color: #f5f6f8; height: 100vh; overflow: hidden; }
       .sidebar { width: 250px; min-width: 250px; flex: 0 0 250px; background-color: #ffffff; border-right: 1px solid #e0e0e0; padding: 20px 0; overflow-y: auto; }
        .sidebar-logo { font-size: 20px; font-weight: bold; color: #2c3e50; padding: 0 20px 20px; border-bottom: 1px solid #eee; margin-bottom: 10px; display: flex; align-items: center; gap: 10px;}
        .menu-title { font-size: 11px; color: #888; font-weight: bold; padding: 10px 20px; text-transform: uppercase; }
        .menu-item { padding: 12px 20px; color: #555; text-decoration: none; display: flex; align-items: center; font-weight: 500; font-size: 14px; transition: 0.2s;}
        .menu-item:hover, .menu-item.active { background-color: #e6efff; color: #4b49ac; }
        .main-content { flex: 1; display: flex; flex-direction: column; overflow-y: auto; }
        .topbar { height: 60px; background-color: #ffffff; border-bottom: 1px solid #e0e0e0; display: flex; justify-content: space-between; align-items: center; padding: 0 30px; }
        .content-body { padding: 30px; }
        .card { background-color: #fff; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.02); padding: 25px; border: 1px solid #eee; }
        .page-title { font-size: 22px; font-weight: bold; color: #2c3e50; margin-bottom: 20px; border-bottom: 2px solid #f1f2f6; padding-bottom: 10px; display: flex; justify-content: space-between;}
        
        table { width: 100%; border-collapse: collapse; margin-top: 15px;}
        th, td { padding: 15px; text-align: left; border-bottom: 1px solid #f1f2f6; font-size: 14px; }
        th { background-color: #fcfcfd; color: #333; font-weight: bold; }
        
        .badge-active { background-color: #d4edda; color: #155724; padding: 5px 10px; border-radius: 12px; font-weight: bold; font-size: 12px;}
        .badge-locked { background-color: #f8d7da; color: #721c24; padding: 5px 10px; border-radius: 12px; font-weight: bold; font-size: 12px;}
        
        .btn-sm { padding: 6px 12px; border: none; border-radius: 4px; cursor: pointer; font-weight: bold; font-size: 12px; color: white;}
        .btn-add { background-color: #2ecc71; padding: 10px 20px; font-size: 14px;}
        .btn-edit { background-color: #f39c12; margin-right: 5px;}
        .btn-delete { background-color: #e74c3c; }
        .search-box { padding: 10px; width: 300px; border: 1px solid #ccc; border-radius: 4px; font-size: 14px; }

        /* MODAL */
        .modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); display: flex; justify-content: center; align-items: center; z-index: 1000; }
        .modal-content { background: #fff; width: 450px; border-radius: 8px; overflow: hidden; animation: fadeIn 0.2s; }
        .modal-header { background: #4b49ac; color: white; padding: 15px 20px; display: flex; justify-content: space-between; }
        .modal-body { padding: 20px; }
        .modal-body label { font-weight: bold; font-size: 13px; color: #555; display: block; margin-top: 10px;}
        .modal-body input, .modal-body select { width: 100%; padding: 10px; margin-top: 5px; border: 1px solid #ccc; border-radius: 4px; font-size: 14px;}
        .modal-body input[readonly] { background-color: #eee; cursor: not-allowed; }
        .modal-footer { padding: 15px 20px; background: #f8f9fa; text-align: right; border-top: 1px solid #eee; }
        .close-btn { cursor: pointer; font-size: 20px; font-weight: bold; }
        @keyframes fadeIn { from { opacity: 0; transform: translateY(-20px); } to { opacity: 1; transform: translateY(0); } }
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
        
        <%-- ĐÃ SỬA LỖI ĐÓNG NGOẶC TẠI ĐÂY VÀ THÊM MENU HỆ THỐNG --%>
        <% if(showTaiKhoan) { %>
            <div class="menu-title">HỆ THỐNG</div>
            <a href="QuanLyTaiKhoanServlet" class="menu-item active">Quản lý tài khoản</a>
        <% } %>
    </div>

    <div class="main-content">
        <div class="topbar">
            <div style="font-weight:bold;">Hệ thống Quản lý Kho</div>
            <div style="font-size: 13px;"><b><%= acc.getHoTen() %></b> | <%= acc.getVaiTro() %> 
                <a href="LogoutServlet" style="color: #e74c3c; text-decoration: none; margin-left:10px;">[Đăng xuất]</a>
            </div>
        </div>

        <div class="content-body">
            <% if(request.getParameter("error") != null) { %>
                <div style="color: red; background: #f8d7da; padding: 10px; border-radius: 4px; margin-bottom: 15px;">⚠️ <%= request.getParameter("error") %></div>
            <% } %>

            <div class="card">
                <div class="page-title">
                    <span>Quản Lý Quyền Truy Cập Hệ Thống</span>
                    <button class="btn-sm btn-add" onclick="moModalThem()">+ Cấp Tài Khoản Mới</button>
                </div>
                
                <input type="text" id="searchInput" class="search-box" onkeyup="timKiemLIVE()" placeholder="🔍 Gõ tên đăng nhập hoặc tên NV...">

                <table id="tkTable">
                    <thead>
                        <tr>
                            <th>Tên Đăng Nhập</th>
                            <th>Họ Tên Nhân Viên</th>
                            <th>Vai Trò</th>
                            <th>Trạng Thái</th>
                            <th style="text-align: center;">Thao Tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% 
                            List<TaiKhoanDTO> list = (List<TaiKhoanDTO>) request.getAttribute("listTK");
                            if(list != null && !list.isEmpty()) {
                                for(TaiKhoanDTO tk : list) {
                                    boolean isActive = "Hoạt động".equalsIgnoreCase(tk.getTrangThai());
                        %>
                        <tr>
                            <td class="tim-kiem-td"><strong><%= tk.getTenDangNhap() %></strong></td>
                            <td class="tim-kiem-td"><%= tk.getHoTenNV() != null ? tk.getHoTenNV() : "Admin Hệ Thống" %></td>
                            <td style="color: #4b49ac; font-weight: bold;"><%= tk.getVaiTro() %></td>
                            <td>
                                <% if(isActive) { %> <span class="badge-active">✔ Hoạt động</span>
                                <% } else { %> <span class="badge-locked">✖ Bị khóa</span> <% } %>
                            </td>
                            <td style="text-align: center;">
                                <button class="btn-sm btn-edit" 
                                        onclick="moModalSua('<%= tk.getTenDangNhap() %>', '<%= tk.getMatKhau() %>', '<%= tk.getVaiTro() %>', '<%= tk.getTrangThai() %>')">
                                    ⚙ Thiết lập
                                </button>
                                <% if(!tk.getTenDangNhap().equals(acc.getTenDangNhap())) { %> <button class="btn-sm btn-delete" onclick="xacNhanXoa('<%= tk.getTenDangNhap() %>')">✖ Xóa</button>
                                <% } %>
                            </td>
                        </tr>
                        <% } } %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <div id="tkModal" class="modal-overlay" style="display: none;">
        <div class="modal-content">
            <div class="modal-header">
                <h3 id="modalTitle">Cấp Tài Khoản Mới</h3>
                <span class="close-btn" onclick="dongModal()">&times;</span>
            </div>
            <form action="QuanLyTaiKhoanServlet" method="POST">
                <div class="modal-body">
                    <input type="hidden" name="action" id="formAction" value="add">
                    
                    <label>Tên đăng nhập (*):</label>
                    <input type="text" name="tenDangNhap" id="txtTenDN" required placeholder="Ví dụ: nva_kho">

                    <label>Mật khẩu (*):</label>
                    <input type="text" name="matKhau" id="txtMatKhau" required placeholder="Nhập mật khẩu...">

                    <div id="divNhanVien">
                        <label>Gắn với Nhân Viên:</label>
                        <select name="maNV" id="selMaNV">
                            <% 
                                Map<String, String> mapNV = (Map<String, String>) request.getAttribute("mapNV");
                                if(mapNV != null) {
                                    for(Map.Entry<String, String> entry : mapNV.entrySet()) {
                            %>
                                <option value="<%= entry.getKey() %>"><%= entry.getValue() %> (Mã: <%= entry.getKey() %>)</option>
                            <% } } %>
                        </select>
                    </div>

                    <label>Vai Trò:</label>
                    <select name="vaiTro" id="selVaiTro">
                        <option value="Thủ kho">Thủ kho (Chỉ tạo phiếu)</option>
                        <option value="Trưởng kho">Trưởng kho (Duyệt phiếu)</option>
                        <option value="Giám đốc">Giám đốc (Xem báo cáo)</option>
                        <option value="Admin">Admin (Quản trị hệ thống)</option>
                    </select>

                    <div id="divTrangThai" style="display:none;">
                        <label>Trạng Thái:</label>
                        <select name="trangThai" id="selTrangThai">
                            <option value="Hoạt động">✔ Hoạt động (Được phép đăng nhập)</option>
                            <option value="Bị khóa">✖ Bị khóa (Cấm đăng nhập)</option>
                        </select>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn-sm" style="background:#aaa;" onclick="dongModal()">Hủy</button>
                    <button type="submit" class="btn-sm btn-add" id="btnSubmit">Lưu Tài Khoản</button>
                </div>
            </form>
        </div>
    </div>

    <script>
        function timKiemLIVE() {
            let filter = document.getElementById('searchInput').value.toUpperCase();
            let tr = document.getElementById('tkTable').getElementsByTagName('tr');
            for (let i = 1; i < tr.length; i++) {
                let textContent = tr[i].innerText;
                tr[i].style.display = textContent.toUpperCase().indexOf(filter) > -1 ? "" : "none";
            }
        }

        function moModalThem() {
            document.getElementById('modalTitle').innerText = "Cấp Tài Khoản Mới";
            document.getElementById('formAction').value = "add";
            document.getElementById('btnSubmit').innerText = "+ Tạo Mới";
            
            document.getElementById('txtTenDN').value = "";
            document.getElementById('txtTenDN').readOnly = false; // Cho phép nhập User
            document.getElementById('txtMatKhau').value = "";
            
            document.getElementById('divNhanVien').style.display = "block";
            document.getElementById('divTrangThai').style.display = "none";

            document.getElementById('tkModal').style.display = "flex";
        }

        function moModalSua(tenDN, matKhau, vaiTro, trangThai) {
            document.getElementById('modalTitle').innerText = "Thiết Lập Tài Khoản";
            document.getElementById('formAction').value = "edit";
            document.getElementById('btnSubmit').innerText = "✔ Cập Nhật";

            document.getElementById('txtTenDN').value = tenDN;
            document.getElementById('txtTenDN').readOnly = true; // Không cho sửa Username
            document.getElementById('txtMatKhau').value = matKhau; // Có thể reset pass
            document.getElementById('selVaiTro').value = vaiTro;
            document.getElementById('selTrangThai').value = trangThai;

            document.getElementById('divNhanVien').style.display = "none"; // Sửa thì không cho đổi Nhân viên
            document.getElementById('divTrangThai').style.display = "block"; // Hiển thị để chọn Khóa/Mở Khóa

            document.getElementById('tkModal').style.display = "flex";
        }

        function xacNhanXoa(tenDN) {
            if(confirm("Xác nhận XÓA VĨNH VIỄN tài khoản '" + tenDN + "'?")) {
                window.location.href = "QuanLyTaiKhoanServlet?action=delete&id=" + tenDN;
            }
        }

        function dongModal() { document.getElementById('tkModal').style.display = "none"; }
    </script>
</body>
</html>