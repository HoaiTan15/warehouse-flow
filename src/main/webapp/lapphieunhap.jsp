<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="model.taikhoan" %>
<%@ page import="model.HangHoa" %>
<%@ page import="model.NhaCungCap" %> 
<%
   // 1. KÉO THÔNG TIN TÀI KHOẢN TỪ SESSION
    taikhoan acc = (taikhoan) session.getAttribute("account");
    if(acc == null) { response.sendRedirect("login.jsp"); return; }
    if (request.getAttribute("danhSachHH") == null || request.getAttribute("danhSachNCC") == null
            || request.getAttribute("maPhieuAuto") == null) {
        request.getRequestDispatcher("LapPhieuNhapServlet").forward(request, response);
        return;
    }
     // 2. LOGIC PHÂN QUYỀN
    String role = acc.getVaiTro() != null ? acc.getVaiTro() : "";
  
    boolean showHangHoa      = role.equalsIgnoreCase("Thủ kho") || role.equalsIgnoreCase("Trưởng kho") || role.equalsIgnoreCase("Giám đốc");
    boolean showMenuNhapXuat = role.equalsIgnoreCase("Trưởng kho") || role.equalsIgnoreCase("Giám đốc");
    boolean showBtnLapPhieu  = role.equalsIgnoreCase("Thủ kho") || role.equalsIgnoreCase("Trưởng kho") || role.equalsIgnoreCase("Giám đốc");
    boolean showBaoCao       = role.equalsIgnoreCase("Thủ kho") || role.equalsIgnoreCase("Trưởng kho") || role.equalsIgnoreCase("Giám đốc") || role.equalsIgnoreCase("Admin");
    boolean showLichSu       = role.equalsIgnoreCase("Thủ kho") || role.equalsIgnoreCase("Trưởng kho") || role.equalsIgnoreCase("Giám đốc") || role.equalsIgnoreCase("Admin");
    boolean showTaiKhoan     = role.equalsIgnoreCase("Admin");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Lập Phiếu Nhập Kho - WMS</title>
    <style>
        /* CSS reset & Font */
        * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Segoe UI', Arial, sans-serif; }
        body { display: flex; background-color: #f5f6f8; height: 100vh; overflow: hidden; }
        
        /* SIDEBAR */
        .sidebar { width: 250px; min-width: 250px; flex: 0 0 250px; background-color: #ffffff; border-right: 1px solid #e0e0e0; padding: 20px 0; overflow-y: auto; }
        .sidebar-logo { font-size: 20px; font-weight: bold; color: #333; padding: 0 20px 20px; border-bottom: 1px solid #eee; margin-bottom: 10px; }
        .menu-title { font-size: 12px; color: #888; font-weight: bold; padding: 10px 20px; text-transform: uppercase; }
        .menu-item { padding: 12px 20px; color: #555; text-decoration: none; display: block; font-weight: 500; }
        .menu-item:hover { background-color: #f0f4ff; color: #0056b3; }
        .menu-item.active { background-color: #e6efff; color: #0056b3; border-right: 3px solid #0056b3; }

        /* MAIN CONTENT */
        .main-content { flex: 1; display: flex; flex-direction: column; overflow-y: auto; }
        .topbar { height: 60px; background-color: #ffffff; border-bottom: 1px solid #e0e0e0; display: flex; justify-content: space-between; align-items: center; padding: 0 30px; }
        .topbar .title { font-size: 18px; font-weight: bold; color: #333; }
        .topbar .user-info { font-size: 14px; color: #555; }
        .badge-role { background-color: #ffe4e4; color: #d93025; padding: 3px 8px; border-radius: 12px; font-size: 12px; font-weight: bold; margin-left: 10px; }

        /* FORM LẬP PHIẾU */
        .content-body { padding: 30px; }
        .card { background-color: #fff; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); padding: 25px; margin-bottom: 20px; }
        .card-header { font-size: 18px; font-weight: bold; color: #333; margin-bottom: 20px; padding-bottom: 10px; border-bottom: 1px solid #eee; }
        
        .form-row { display: flex; gap: 20px; margin-bottom: 15px; }
        .form-group { flex: 1; display: flex; flex-direction: column; }
        .form-group label { font-size: 13px; color: #666; margin-bottom: 5px; font-weight: bold; }
        .form-control { padding: 10px; border: 1px solid #ccc; border-radius: 5px; font-size: 14px; outline: none; transition: border-color 0.3s; }
        .form-control:focus { border-color: #0056b3; }
        
        /* BẢNG CHI TIẾT HÀNG HÓA */
        table { width: 100%; border-collapse: collapse; margin-top: 15px; }
        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #eee; }
        th { background-color: #f8f9fa; color: #555; font-size: 13px; text-transform: uppercase; }
        td { font-size: 14px; color: #333; }
        
        /* BUTTONS */
        .btn { padding: 10px 20px; border: none; border-radius: 5px; cursor: pointer; font-weight: bold; font-size: 14px; transition: 0.2s; }
        .btn-primary { background-color: #00b894; color: white; } 
        .btn-primary:hover { background-color: #00a383; }
        .btn-secondary { background-color: #f1f3f4; color: #333; border: 1px solid #ccc;}
        .btn-secondary:hover { background-color: #e2e6ea; }
        .btn-danger { background-color: #ff7675; color: white; padding: 6px 12px; font-size: 12px;}
        .action-footer { display: flex; justify-content: flex-end; gap: 15px; margin-top: 20px; padding-top: 20px; border-top: 1px solid #eee;}
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
            <a href="LichSuServlet" class="menu-item">Lịch sử tồn kho</a>
        <% } %>

      <% if(showTaiKhoan) { %>
         <div class="menu-title">HỆ THỐNG</div>
            <a href="QuanLyTaiKhoanServlet" class="menu-item active">Quản lý tài khoản</a>
        <% } %>
    </div>
    <div class="main-content">
        <div class="topbar">
            <div class="title">Hệ thống Quản lý Kho</div>
            <div class="user-info">
                <%= acc.getHoTen() != null ? acc.getHoTen() : acc.getTenDangNhap() %> 
                <span class="badge-role"><%= acc.getVaiTro() %></span>
                <a href="LogoutServlet" style="margin-left: 15px; color: #d93025; text-decoration: none;">[➜ Logout]</a>
            </div>
        </div>

        <div class="content-body">
            
            <% if(request.getAttribute("thongBaoLoi") != null) { %>
                <div style="background-color: #ffcccc; color: #cc0000; padding: 15px; border-radius: 5px; margin-bottom: 20px; font-weight: bold;">
                    ⚠️ LỖI: <%= request.getAttribute("thongBaoLoi") %>
                </div>
            <% } %>

            <form action="LuuPhieuNhapServlet" method="POST" id="formPhieuNhap">
                
                <div class="card">
                    <div class="card-header">Lập Phiếu Nhập Kho Mới</div>
                    <div class="form-row">
                        <div class="form-group">
                            <label>Mã Phiếu Nhập</label>
                            <input type="text" name="maPhieu" class="form-control" 
                                   value="<%= request.getAttribute("maPhieuAuto") != null ? request.getAttribute("maPhieuAuto") : "" %>" 
                                   readonly style="background-color: #e9ecef; font-weight: bold; color: #0056b3;">
                        </div>
                        <div class="form-group">
                            <label>Ngày Lập</label>
                           <input type="date" name="ngayLap" class="form-control" id="todayDate" readonly style="background-color: #e9ecef; font-weight: bold; pointer-events: none;">
                        </div>
                        <div class="form-group">
                            <label>Nhà Cung Cấp</label>
                            <select name="maNCC" class="form-control" required>
                                <option value="">-- Chọn Nhà cung cấp --</option>
                                <%
                                    List<NhaCungCap> listNCC = (List<NhaCungCap>) request.getAttribute("danhSachNCC");
                                    if(listNCC != null) {
                                        for(NhaCungCap ncc : listNCC) {
                                %>
                                <option value="<%= ncc.getMaNCC() %>"><%= ncc.getTenNCC() %></option>
                                <% 
                                        } 
                                    } 
                                %>
                            </select>
                        </div>
                    </div>
                    <div class="form-row">
                        <div class="form-group">
                            <label>Số Hóa Đơn (Tham chiếu)</label>
                            <input type="text" name="soHoaDon" class="form-control" placeholder="VD: HD_0123">
                        </div>
                        <div class="form-group" style="flex: 2;">
                            <label>Ghi Chú</label>
                            <input type="text" name="ghiChu" class="form-control" placeholder="Nhập ghi chú phiếu nhập...">
                        </div>
                    </div>
                </div>

                <div class="card">
                    <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
                        <span>Danh Sách Mặt Hàng Nhập</span>
                        <span style="font-size: 13px; color: #888; font-weight: normal;">* Thêm hàng hóa vào danh sách trước khi lưu phiếu</span>
                    </div>
                    
                    <div class="form-row" style="background-color: #f8f9fa; padding: 15px; border-radius: 5px;">
                        <div class="form-group" style="flex: 2;">
                            <label>Chọn Mặt Hàng</label>
                            <select id="maHangTam" class="form-control">
                                <option value="">-- Chọn mặt hàng cần nhập --</option>
                                <%
                                    List<HangHoa> listHH = (List<HangHoa>) request.getAttribute("danhSachHH");
                                 if(listHH != null) {
                                     for(HangHoa hh : listHH) {
                             %>
                                 <option value="<%= hh.getMaHang() %>" data-name="<%= hh.getTenHang() %>">
                                     <%= hh.getMaHang() %> - <%= hh.getTenHang() %> (Tồn: <%= hh.getSoLuongTonKho() %>)
                                 </option>
                             <%
                                     }
                                 }
                             %>
                            </select>
                        </div>
                        <div class="form-group">
                            <label>Số Lượng Nhập</label>
                            <input type="number" id="soLuongTam" class="form-control" min="1" value="1">
                        </div>
                        <div class="form-group">
                            <label>Đơn Giá Nhập (VNĐ)</label>
                            <input type="number" id="donGiaTam" class="form-control" placeholder="Ví dụ: 120000">
                        </div>
                        <div class="form-group" style="justify-content: flex-end;">
                            <button type="button" class="btn btn-secondary" id="btnThemHang" style="height: 40px;">➕ Thêm</button>
                        </div>
                    </div>

                    <table>
                        <thead>
                            <tr>
                                <th>Mã Hàng</th>
                                <th>Tên Mặt Hàng</th>
                                <th>Số Lượng</th>
                                <th>Đơn Giá</th>
                                <th>Thành Tiền</th>
                                <th>Hành Động</th>
                            </tr>
                        </thead>
                        <tbody id="bangChiTiet">
                            </tbody>
                    </table>
                </div>

                <div class="action-footer">
                    <a href="LoadDanhSachKhoServlet" class="btn btn-secondary">Hủy Bỏ</a>
                    <button type="button" onclick="kiemTraVaSubmit()" class="btn btn-primary">💾 Lưu Phiếu Nhập (Chờ Duyệt)</button>
                </div>

            </form>
        </div>
    </div>

    <script>
        // 1. TỰ ĐỘNG ĐIỀN NGÀY HÔM NAY VÀO Ô NGÀY LẬP
        window.onload = function() {
            var today = new Date();
            var dd = String(today.getDate()).padStart(2, '0');
            var mm = String(today.getMonth() + 1).padStart(2, '0'); // Tháng bắt đầu từ 0
            var yyyy = today.getFullYear();
            
            var todayStr = yyyy + '-' + mm + '-' + dd; 
            
            var inputNgay = document.getElementById('todayDate');
            if (inputNgay != null) {
                inputNgay.value = todayStr;
            }
        };

        // 2. Xử lý nút Thêm hàng hóa vào bảng
        document.getElementById('btnThemHang').addEventListener('click', function() {
            var selectBox = document.getElementById('maHangTam');
            var maHang = selectBox.value;
            
            if(!maHang) {
                alert("Vui lòng chọn một mặt hàng!");
                return;
            }

            var tenHang = selectBox.options[selectBox.selectedIndex].getAttribute('data-name');
            var soLuong = document.getElementById('soLuongTam').value;
            var donGia = document.getElementById('donGiaTam').value;

            if(soLuong <= 0 || donGia === "") {
                alert("Vui lòng nhập số lượng hợp lệ và đơn giá!");
                return;
            }

            var thanhTien = soLuong * donGia;
            
            var formatTien = new Intl.NumberFormat('vi-VN').format(thanhTien) + " đ";
            var formatDonGia = new Intl.NumberFormat('vi-VN').format(donGia) + " đ";

            // Tạo dòng <tr> chứa thông tin để submit đi
            var tr = document.createElement('tr');
            tr.innerHTML = `
                <td><input type="hidden" name="maHang[]" value="` + maHang + `">` + maHang + `</td>
                <td>` + tenHang + `</td>
                <td><input type="hidden" name="soLuong[]" value="` + soLuong + `">` + soLuong + `</td>
                <td><input type="hidden" name="donGia[]" value="` + donGia + `">` + formatDonGia + `</td>
                <td style="font-weight: bold; color: #00b894;">` + formatTien + `</td>
                <td><button type="button" class="btn btn-danger" onclick="xoaDong(this)">Xóa</button></td>
            `;

            document.getElementById('bangChiTiet').appendChild(tr);

            // Reset form nhỏ lại để chọn món khác
            selectBox.value = "";
            document.getElementById('soLuongTam').value = 1;
            document.getElementById('donGiaTam').value = "";
        });

        // 3. Hàm xóa dòng khi bấm nút Xóa
        function xoaDong(buttonElement) {
            var tr = buttonElement.parentNode.parentNode;
            tr.parentNode.removeChild(tr);
        }

        // 4. Kiểm tra xem đã có hàng trong bảng chưa trước khi bấm Lưu
        function kiemTraVaSubmit() {
            var soLuongHangTrongBang = document.getElementById('bangChiTiet').children.length;
            if(soLuongHangTrongBang === 0) {
                alert("Lỗi: Bạn chưa thêm mặt hàng nào vào danh sách nhập!");
            } else {
                document.getElementById('formPhieuNhap').submit();
            }
        }
    </script>
