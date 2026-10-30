<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="model.NhaCungCap" %>
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
    <title>Quản Lý Nhà Cung Cấp - WMS Admin</title>
    <style>
        /* GIỮ NGUYÊN CSS KHUNG GIAO DIỆN CHUNG */
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
        
        /* CÁC NÚT BẤM */
        .btn-sm { padding: 6px 12px; border: none; border-radius: 4px; cursor: pointer; font-weight: bold; font-size: 12px; color: white;}
        .btn-add { background-color: #2ecc71; padding: 10px 20px; font-size: 14px;}
        .btn-edit { background-color: #f39c12; margin-right: 5px;}
        .btn-delete { background-color: #e74c3c; }
        
        /* Ô TÌM KIẾM */
        .search-box { padding: 10px; width: 300px; border: 1px solid #ccc; border-radius: 4px; font-size: 14px; }

        /* MODAL THÊM/SỬA */
        .modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); display: flex; justify-content: center; align-items: center; z-index: 1000; }
        .modal-content { background: #fff; width: 450px; border-radius: 8px; overflow: hidden; animation: fadeIn 0.2s; }
        .modal-header { background: #4b49ac; color: white; padding: 15px 20px; display: flex; justify-content: space-between; }
        .modal-body { padding: 20px; }
        .modal-body label { font-weight: bold; font-size: 13px; color: #555; display: block; margin-top: 10px;}
        .modal-body input { width: 100%; padding: 10px; margin-top: 5px; border: 1px solid #ccc; border-radius: 4px; }
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
             <a href="QuanLyNhaCungCapServlet" class="menu-item  active">Nhà cung cấp</a>
        <% } %>

        <% if(showHangHoa) { %>
            <a href="LoadDanhSachKhoServlet" class="menu-item">Quản lý hàng hóa</a>
            <a href="KiemKeServlet" class="menu-item">Kiểm kê</a>
        <% } %>
        
        <% if(showBaoCao) { %>
            <a href="BaoCaoServlet" class="menu-item ">Báo cáo</a>
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
            <div style="font-weight:bold;">Hệ thống Quản lý Kho</div>
            <div style="font-size: 13px;"><b><%= acc.getHoTen() %></b> | <%= acc.getVaiTro() %></div>
        </div>

        <div class="content-body">
            <% if(request.getParameter("error") != null) { %>
                <div style="color: red; background: #f8d7da; padding: 10px; border-radius: 4px; margin-bottom: 15px;">
                    ⚠️ <%= request.getParameter("error") %>
                </div>
            <% } %>

            <div class="card">
                <div class="page-title">
                    <span>Danh Sách Đối Tác Cung Ứng</span>
                    <button class="btn-sm btn-add" onclick="moModalThem()">+ Thêm Nhà Cung Cấp Mới</button>
                </div>
                
                <input type="text" id="searchInput" class="search-box" onkeyup="timKiemLIVE()" placeholder="🔍 Gõ tên nhà cung cấp để tìm...">

                <table id="nccTable">
                    <thead>
                        <tr>
                            <th>Mã NCC</th>
                            <th>Tên Nhà Cung Cấp</th>
                            <th>Số Điện Thoại</th>
                            <th>Email</th>
                            <th>Địa Chỉ</th>
                            <th style="text-align: center;">Thao Tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% 
                            List<NhaCungCap> list = (List<NhaCungCap>) request.getAttribute("listNCC");
                            if(list != null && !list.isEmpty()) {
                                for(NhaCungCap ncc : list) {
                        %>
                        <tr>
                            <td><strong><%= ncc.getMaNCC() %></strong></td>
                            <td class="ten-ncc"><%= ncc.getTenNCC() %></td>
                            <td><%= ncc.getSdt() %></td>
                            <td><%= ncc.getEmail() %></td>
                            <td><%= ncc.getDiaChi() %></td>
                            <td style="text-align: center;">
                                <button class="btn-sm btn-edit" 
                                        onclick="moModalSua('<%= ncc.getMaNCC() %>', '<%= ncc.getTenNCC() %>', '<%= ncc.getSdt() %>', '<%= ncc.getEmail() %>', '<%= ncc.getDiaChi() %>')">
                                    ✎ Sửa
                                </button>
                                <button class="btn-sm btn-delete" onclick="xacNhanXoa('<%= ncc.getMaNCC() %>', '<%= ncc.getTenNCC() %>')">✖ Xóa</button>
                            </td>
                        </tr>
                        <% } } %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <div id="nccModal" class="modal-overlay" style="display: none;">
        <div class="modal-content">
            <div class="modal-header">
                <h3 id="modalTitle">Thêm Nhà Cung Cấp</h3>
                <span class="close-btn" onclick="dongModal()">&times;</span>
            </div>
            <form action="QuanLyNhaCungCapServlet" method="POST">
                <div class="modal-body">
                    <input type="hidden" name="action" id="formAction" value="add">
                    
                    <label>Mã NCC (Hệ thống tự cấp):</label>
                    <input type="text" name="maNCC" id="txtMaNCC" readonly placeholder="Tự động sinh khi thêm mới...">

                    <label>Tên Nhà Cung Cấp (*):</label>
                    <input type="text" name="tenNCC" id="txtTenNCC" required placeholder="Nhập tên đối tác...">

                    <label>Số Điện Thoại:</label>
                    <input type="text" name="sdt" id="txtSDT" placeholder="Ví dụ: 0901234567">

                    <label>Email:</label>
                    <input type="email" name="email" id="txtEmail" placeholder="Ví dụ: contact@congty.com">

                    <label>Địa Chỉ:</label>
                    <input type="text" name="diaChi" id="txtDiaChi" placeholder="Địa chỉ trụ sở...">
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn-sm" style="background:#aaa;" onclick="dongModal()">Hủy Bỏ</button>
                    <button type="submit" class="btn-sm btn-add" id="btnSubmit">Lưu Dữ Liệu</button>
                </div>
            </form>
        </div>
    </div>

    <script>
        // 1. CHỨC NĂNG TÌM KIẾM REAL-TIME
        function timKiemLIVE() {
            let filter = document.getElementById('searchInput').value.toUpperCase();
            let tr = document.getElementById('nccTable').getElementsByTagName('tr');

            for (let i = 1; i < tr.length; i++) { // Bỏ qua dòng thead (i=0)
                let td = tr[i].getElementsByClassName('ten-ncc')[0]; // Lấy ô Tên NCC
                if (td) {
                    let txtValue = td.textContent || td.innerText;
                    if (txtValue.toUpperCase().indexOf(filter) > -1) {
                        tr[i].style.display = ""; // Hiện dòng
                    } else {
                        tr[i].style.display = "none"; // Ẩn dòng
                    }
                }       
            }
        }

        // 2. CHỨC NĂNG BẬT MODAL THÊM
        function moModalThem() {
            document.getElementById('modalTitle').innerText = "Thêm Nhà Cung Cấp Mới";
            document.getElementById('formAction').value = "add";
            document.getElementById('btnSubmit').innerText = "+ Thêm Mới";
            
            // Xóa rỗng các ô
            document.getElementById('txtMaNCC').value = "";
            document.getElementById('txtTenNCC').value = "";
            document.getElementById('txtSDT').value = "";
            document.getElementById('txtEmail').value = "";
            document.getElementById('txtDiaChi').value = "";

            document.getElementById('nccModal').style.display = "flex";
        }

        // 3. CHỨC NĂNG BẬT MODAL SỬA
        function moModalSua(ma, ten, sdt, email, diachi) {
            document.getElementById('modalTitle').innerText = "Cập Nhật Thông Tin";
            document.getElementById('formAction').value = "edit";
            document.getElementById('btnSubmit').innerText = "✔ Lưu Thay Đổi";

            // Đổ dữ liệu cũ vào các ô
            document.getElementById('txtMaNCC').value = ma;
            document.getElementById('txtTenNCC').value = ten;
            document.getElementById('txtSDT').value = sdt;
            document.getElementById('txtEmail').value = email;
            document.getElementById('txtDiaChi').value = diachi;

            document.getElementById('nccModal').style.display = "flex";
        }

        // 4. CHỨC NĂNG XÓA
        function xacNhanXoa(ma, ten) {
            if(confirm("Bạn có chắc chắn muốn xóa đối tác '" + ten + "' không? Hành động này không thể hoàn tác!")) {
                window.location.href = "QuanLyNhaCungCapServlet?action=delete&id=" + ma;
            }
        }

        function dongModal() {
            document.getElementById('nccModal').style.display = "none";
        }
    </script>
</body>
</html>