<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="model.HangHoa" %>
<%@ page import="model.taikhoan" %>
<%
    taikhoan acc = (taikhoan) session.getAttribute("account");
    if(acc == null) { response.sendRedirect("login.jsp"); return; }
    
    String role = acc.getVaiTro() != null ? acc.getVaiTro() : "";
    boolean showMenuNhapXuat = role.equalsIgnoreCase("Trưởng kho") || role.equalsIgnoreCase("Giám đốc");
    boolean isManager = role.equalsIgnoreCase("Trưởng kho") || role.equalsIgnoreCase("Giám đốc");
    boolean showKeToan = role.equalsIgnoreCase("Giám đốc");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Quản Lý Hàng Hóa - WMS</title>
    <style>
        /* CSS GIAO DIỆN CHUNG (Giống hình bạn gửi) */
        * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Segoe UI', Arial, sans-serif; }
        body { display: flex; background-color: #f5f6f8; height: 100vh; overflow: hidden; }
        .sidebar { width: 250px; min-width: 250px; flex: 0 0 250px; background-color: #ffffff; border-right: 1px solid #e0e0e0; padding: 20px 0; overflow-y: auto; }
        .sidebar-logo { font-size: 20px; font-weight: bold; color: #333; padding: 0 20px 20px; border-bottom: 1px solid #eee; margin-bottom: 10px; display: flex; align-items: center; gap: 10px;}
        .menu-title { font-size: 11px; color: #888; font-weight: bold; padding: 10px 20px; text-transform: uppercase; }
        .menu-item { padding: 12px 20px; color: #555; text-decoration: none; display: block; font-weight: 500; font-size: 14px; transition: 0.2s;}
        .menu-item:hover, .menu-item.active { background-color: #e6efff; color: #4b49ac; border-right: 3px solid #4b49ac; font-weight: bold;}
        
        .main-content { flex: 1; display: flex; flex-direction: column; overflow-y: auto; }
        .topbar { height: 60px; min-height: 60px; background-color: #ffffff; border-bottom: 1px solid #e0e0e0; display: flex; justify-content: space-between; align-items: center; padding: 0 30px; }
        .content-body { padding: 30px; }
        
        .card { background-color: #fff; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); padding: 25px; margin-bottom: 20px; }
        .card-title { font-size: 20px; font-weight: bold; color: #2c3e50; margin-bottom: 20px;}
        
        /* THANH CÔNG CỤ TÌM KIẾM & NÚT BẤM */
        .toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
        .search-group { display: flex; gap: 10px; }
        .search-input { padding: 10px 15px; width: 300px; border: 1px solid #ccc; border-radius: 4px; outline: none; }
        .btn { padding: 10px 15px; border: none; border-radius: 4px; cursor: pointer; font-weight: bold; font-size: 13px; color: white; text-decoration: none; display: inline-block; text-align: center;}
        .btn-search { background-color: #3498db; }
        .btn-reload { background-color: #95a5a6; color: #333;}
        .btn-add-product { background-color: #2ecc71; } /* Nút Thêm Sản Phẩm Mới */
        .btn-import { background-color: #27ae60; }
        .btn-export { background-color: #f39c12; }
        .btn-edit { background-color: #f1c40f; color: #333; padding: 6px 12px; margin: 0 2px;}
        .btn-delete { background-color: #e74c3c; padding: 6px 12px; margin: 0 2px;}
        .btn-inventory { background-color: #fff; color: #3498db; border: 1px solid #3498db; padding: 5px 12px; }

        /* BẢNG DỮ LIỆU */
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 15px 10px; text-align: center; border-bottom: 1px solid #f1f2f6; font-size: 14px; }
        th { background-color: #fcfcfd; color: #555; font-weight: bold; }
        
        /* SỐ LƯỢNG < 10 SẼ ĐỎ */
        .qty-low { color: #e74c3c; font-weight: bold; font-size: 16px; }
        .qty-normal { color: #2c3e50; font-weight: bold; font-size: 15px; }

        /* MODAL */
        .modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); display: flex; justify-content: center; align-items: center; z-index: 1000; }
        .modal-content { background: #fff; width: 500px; border-radius: 8px; overflow: hidden; animation: fadeIn 0.2s; }
        .modal-header { background: #4b49ac; color: white; padding: 15px 20px; display: flex; justify-content: space-between; }
        .modal-body { padding: 20px; max-height: 70vh; overflow-y: auto;}
        .modal-body label { font-weight: bold; font-size: 13px; color: #555; display: block; margin-top: 10px;}
        .modal-body input, .modal-body select { width: 100%; padding: 10px; margin-top: 5px; border: 1px solid #ccc; border-radius: 4px; font-size: 14px;}
        .modal-body input[readonly] { background-color: #eee; cursor: not-allowed; }
        .modal-footer { padding: 15px 20px; background: #f8f9fa; text-align: right; border-top: 1px solid #eee; }
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
        <a href="LoadDanhSachKhoServlet" class="menu-item active">Quản lý hàng hóa</a>
        <a href="KiemKeServlet" class="menu-item">Kiểm kê</a>
        <a href="BaoCaoServlet" class="menu-item">Báo cáo</a>
        <a href="LichSuServlet" class="menu-item">Lịch sử tồn kho</a>
         <% if(showKeToan) { %>
             <a href="CongNoServlet" class="menu-item">Quản lý Công nợ</a>
              <a href="KiemKeDuyetServlet" class="menu-item">Duyệt Kiểm Kê</a>
        <% } %>
    </div>

    <div class="main-content">
        <div class="topbar">
            <div style="font-weight:bold; font-size: 16px;">Hệ thống Quản lý Kho</div>
            <div style="font-size: 13px;"><b><%= acc.getHoTen() %></b> | <%= acc.getVaiTro() %> 
                <a href="LogoutServlet" style="color: #e74c3c; text-decoration: none; margin-left:10px;">[Đăng xuất]</a>
            </div>
        </div>

        <div class="content-body">
            <% if(request.getParameter("error") != null) { %>
                <div style="color: white; background: #e74c3c; padding: 15px; border-radius: 4px; margin-bottom: 15px; font-weight: bold;">
                    ⚠️ <%= request.getParameter("error") %>
                </div>
            <% } %>

            <div class="card">
                <div class="card-title">Danh Sách Hàng Hóa Trong Kho</div>
                
                <div class="toolbar">
                    <form class="search-group" action="TimKiemHangHoaServlet" method="get">
                        <input type="text" id="searchInput" name="tuKhoa" class="search-input" placeholder="Tên, mã, loại hàng hoặc vị trí...">
                        <button type="submit" class="btn btn-search">🔍 Tìm kiếm</button>
                        <a href="LoadDanhSachKhoServlet" class="btn btn-reload">🔄 Tải lại</a>
                    </form>
                    <div style="display: flex; gap: 10px;">
                        <% if (isManager) { %>
                        <button class="btn btn-add-product" onclick="moModalThem()">+ Thêm Sản Phẩm</button>
                        <% } %>
                        <a href="lapphieunhap.jsp" class="btn btn-import">+ Lập Phiếu Nhập</a>
                        <a href="lapphieuxuat.jsp" class="btn btn-export">- Lập Phiếu Xuất</a>
                    </div>
                </div>

                <table id="hhTable">
                    <thead>
                        <tr>
                            <th>Mã Hàng Hóa</th>
                            <th style="text-align: left;">Tên Hàng Hóa</th>
                            <th>Danh Mục</th>
                            <th>Số Lượng Tồn</th>
                            <th>Đơn Vị Tính</th>
                            <th>Thao Tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% 
                            List<HangHoa> list = (List<HangHoa>) request.getAttribute("listHH");
                            if(list != null && !list.isEmpty()) {
                                for(HangHoa hh : list) {
                                    // LOGIC: SỐ LƯỢNG < 10 HIỆN MÀU ĐỎ
                                    boolean isLow = hh.getSoLuongTonKho() < 10;
                                    String qtyClass = isLow ? "qty-low" : "qty-normal";
                        %>
                        <tr>
                            <td><strong><%= hh.getMaHang() %></strong></td>
                            <td class="ten-hang" style="text-align: left;"><%= hh.getTenHang() %></td>
                            <td><%= hh.getTenLoai() %></td>
                            <td class="<%= qtyClass %>"><%= hh.getSoLuongTonKho() %></td>
                            <td><%= hh.getDvt() %></td>
                            <td>
                                <% if (isManager) { %>
                                <button class="btn btn-edit" onclick="moModalSua('<%= hh.getMaHang() %>', '<%= hh.getTenHang() %>', '<%= hh.getDvt() %>', '<%= hh.getSoLuongTonKho() %>', '<%= hh.getQuyCach() %>', '<%= hh.getHanMucTonToiThieu() %>', '<%= hh.getMaLoai() %>', '<%= hh.getMaViTri() %>')">Sửa</button>
                                <button class="btn btn-delete" onclick="xacNhanXoa('<%= hh.getMaHang() %>', '<%= hh.getTenHang() %>', <%= hh.getSoLuongTonKho() %>)">Xóa</button>
                                <% } %>
                                <a href="KiemKeServlet?maHang=<%= hh.getMaHang() %>" class="btn btn-inventory">Kiểm Kê</a>
                            </td>
                        </tr>
                        <% } } else { out.print("<tr><td colspan='6'>Chưa có dữ liệu hàng hóa.</td></tr>"); } %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <div id="hhModal" class="modal-overlay" style="display: none;">
        <div class="modal-content">
            <div class="modal-header">
                <h3 id="modalTitle">Thêm Sản Phẩm Mới</h3>
                <span class="close-btn" style="cursor:pointer;" onclick="dongModal()">&times;</span>
            </div>
            <form action="QuanLyHangHoaServlet" method="POST">
                <div class="modal-body">
                    <input type="hidden" name="action" id="formAction" value="add">
                    
                    <label>Mã Hàng (Hệ thống tự cấp):</label>
                    <input type="text" name="maHang" id="txtMaHang" readonly placeholder="Mã tự sinh khi lưu (Không được sửa)">

                    <label>Tên Sản Phẩm (*):</label>
                    <input type="text" name="tenHang" id="txtTenHang" required placeholder="Nhập tên sản phẩm...">

                    <label>Thuộc Danh Mục (Bắt buộc) (*):</label>
                    <select name="maLoai" id="selMaLoai" required>
                        <% 
                            Map<String, String> mapLoai = (Map<String, String>) request.getAttribute("mapLoai");
                            if(mapLoai != null) {
                                for(Map.Entry<String, String> entry : mapLoai.entrySet()) {
                        %>
                            <option value="<%= entry.getKey() %>"><%= entry.getValue() %></option>
                        <% } } %>
                    </select>

                    <label>Đơn Vị Tính (*):</label>
                    <input type="text" name="dvt" id="txtDVT" required placeholder="Ví dụ: Chai, Lon, Bịch...">

                    <label>Số Lượng Tồn Kho Ban Đầu:</label>
                    <input type="number" name="soLuongTonKho" id="txtSoLuong" value="0" min="0">

                    <label>Quy Cách:</label>
                    <input type="text" name="quyCach" id="txtQuyCach" placeholder="Ví dụ: 500ml/chai...">

                    <label>Mức Tồn Cảnh Báo (Tối thiểu):</label>
                    <input type="number" name="hanMucTonToiThieu" id="txtHanMuc" required value="10" min="0">

                    <label>Vị Trí Cất Giữ:</label>
                    <select name="maViTri" id="selMaViTri">
                        <% 
                            Map<String, String> mapViTri = (Map<String, String>) request.getAttribute("mapViTri");
                            if(mapViTri != null) {
                                for(Map.Entry<String, String> entry : mapViTri.entrySet()) {
                        %>
                            <option value="<%= entry.getKey() %>"><%= entry.getValue() %></option>
                        <% } } %>
                    </select>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn" style="background:#aaa;" onclick="dongModal()">Hủy Bỏ</button>
                    <button type="submit" class="btn btn-add-product" id="btnSubmit">Lưu Sản Phẩm</button>
                </div>
            </form>
        </div>
    </div>

    <script>
        document.getElementById('searchInput').value = new URLSearchParams(window.location.search).get('tuKhoa') || '';

        function moModalThem() {
            document.getElementById('modalTitle').innerText = "Thêm Sản Phẩm Mới";
            document.getElementById('formAction').value = "add";
            document.getElementById('btnSubmit').innerText = "+ Thêm Sản Phẩm";
            
            document.getElementById('txtMaHang').value = "";
            document.getElementById('txtTenHang').value = "";
            document.getElementById('txtDVT').value = "";
            document.getElementById('txtSoLuong').value = "0";
            document.getElementById('txtSoLuong').readOnly = false; 
            document.getElementById('txtQuyCach').value = "";
            document.getElementById('txtHanMuc').value = "10";
            document.getElementById('hhModal').style.display = "flex";
        }

        function moModalSua(ma, ten, dvt, soluong, quycach, hanmuc, maloai, mavitri) {
            document.getElementById('modalTitle').innerText = "Cập Nhật Sản Phẩm";
            document.getElementById('formAction').value = "edit";
            document.getElementById('btnSubmit').innerText = "✔ Lưu Cập Nhật";

            document.getElementById('txtMaHang').value = ma; // Khóa chết bằng thuộc tính readonly bên trên HTML
            document.getElementById('txtTenHang').value = ten;
            document.getElementById('txtDVT').value = dvt;
            document.getElementById('txtSoLuong').value = soluong;
            document.getElementById('txtSoLuong').readOnly = true; // Sửa thì ko được sửa Số lượng
            document.getElementById('txtQuyCach').value = quycach;
            document.getElementById('txtHanMuc').value = hanmuc;
            document.getElementById('selMaLoai').value = maloai;
            document.getElementById('selMaViTri').value = mavitri;

            document.getElementById('hhModal').style.display = "flex";
        }

        // LOGIC CHẶN XÓA TỪ PHÍA GIAO DIỆN NẾU TỒN KHO >= 10
        function xacNhanXoa(ma, ten, soLuong) {
            if (soLuong >= 10) {
                alert("⛔ KHÔNG THỂ XÓA!\nChỉ được phép xóa các sản phẩm có số lượng tồn kho dưới 10.");
                return;
            }
            if(confirm("Bạn có chắc chắn muốn xóa sản phẩm '" + ten + "' (Tồn kho: " + soLuong + ") không?")) {
                window.location.href = "QuanLyHangHoaServlet?action=delete&id=" + ma + "&sl=" + soLuong;
            }
        }

        function dongModal() { document.getElementById('hhModal').style.display = "none"; }
    </script>
</body>
</html>
