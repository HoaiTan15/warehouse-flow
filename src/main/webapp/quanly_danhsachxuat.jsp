<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="model.PhieuXuatDTO" %>
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
    <title>Quản Lý Phiếu Xuất - WMS Admin</title>
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Segoe UI', Arial, sans-serif; }
        body { display: flex; background-color: #f5f6f8; height: 100vh; overflow: hidden; }
      .sidebar { width: 250px; min-width: 250px; flex: 0 0 250px; background-color: #ffffff; border-right: 1px solid #e0e0e0; padding: 20px 0; overflow-y: auto; }
        .sidebar-logo { font-size: 20px; font-weight: bold; color: #2c3e50; padding: 0 20px 20px; border-bottom: 1px solid #eee; margin-bottom: 10px; display: flex; align-items: center; gap: 10px;}
        .menu-title { font-size: 11px; color: #888; font-weight: bold; padding: 10px 20px; text-transform: uppercase; }
        .menu-item { padding: 12px 20px; color: #555; text-decoration: none; display: flex; align-items: center; font-weight: 500; font-size: 14px; transition: 0.2s;}
        .menu-item:hover { background-color: #f0f4ff; color: #4b49ac; }
        .menu-item.active { background-color: #e6efff; color: #4b49ac; border-right: 3px solid #4b49ac; font-weight: bold;}
        .main-content { flex: 1; display: flex; flex-direction: column; overflow-y: auto; }
        .topbar { height: 60px; background-color: #ffffff; border-bottom: 1px solid #e0e0e0; display: flex; justify-content: space-between; align-items: center; padding: 0 30px; }
        .content-body { padding: 30px; }
        .card { background-color: #fff; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.02); padding: 25px; border: 1px solid #eee; }
        .page-title { font-size: 22px; font-weight: bold; color: #2c3e50; margin-bottom: 20px; border-bottom: 2px solid #f1f2f6; padding-bottom: 10px;}
        
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 15px; text-align: center; border-bottom: 1px solid #f1f2f6; font-size: 14px; }
        th { background-color: #fcfcfd; color: #333; font-weight: bold; }
        
        .badge-pending { background-color: #fff3cd; color: #856404; padding: 5px 10px; border-radius: 12px; font-weight: bold; font-size: 12px;}
        .badge-approved { background-color: #d4edda; color: #155724; padding: 5px 10px; border-radius: 12px; font-weight: bold; font-size: 12px;}
        .badge-rejected { background-color: #f8d7da; color: #721c24; padding: 5px 10px; border-radius: 12px; font-weight: bold; font-size: 12px;}
        
        .btn-sm { padding: 6px 12px; border: none; border-radius: 4px; cursor: pointer; font-weight: bold; font-size: 12px; color: white;}
        .btn-approve { background-color: #2ecc71; margin-right: 5px;}
        .btn-approve:hover { background-color: #27ae60; }
        .btn-reject { background-color: #e74c3c; }
        .btn-reject:hover { background-color: #c0392b; }
        .btn-view { background-color: #3498db; }
        .btn-view:hover { background-color: #2980b9; }

        /* MODAL */
        .modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); display: flex; justify-content: center; align-items: center; z-index: 1000; }
        .modal-content { background: #fff; width: 500px; border-radius: 8px; box-shadow: 0 5px 15px rgba(0,0,0,0.3); overflow: hidden; animation: fadeIn 0.3s; }
        .modal-header { background: #f39c12; color: white; padding: 15px 20px; display: flex; justify-content: space-between; align-items: center; }
        .modal-header h3 { margin: 0; font-size: 18px; }
        .close-btn { font-size: 24px; cursor: pointer; font-weight: bold; }
        .close-btn:hover { color: #f8d7da; }
        .modal-body { padding: 25px; font-size: 15px; line-height: 1.8; color: #333;}
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
            <a href="QuanLyDanhSachXuatServlet" class="menu-item  active">Quản lý Xuất kho</a>
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
              <a href="KiemKeDuyetServlet" class="menu-item">Duyệt Kiểm Kê</a>
        <% } %>
       
    </div>

    <div class="main-content">
        <div class="topbar">
            <div style="font-weight:bold;">Hệ thống Quản lý Kho</div>
            <div style="font-size: 13px;">
                <b><%= acc.getHoTen() %></b> | <%= acc.getVaiTro() %> 
                <a href="LogoutServlet" style="color: #e74c3c; text-decoration: none; margin-left:10px;">[Đăng xuất]</a>
            </div>
        </div>

        <div class="content-body">
            <div class="card">
                <div class="page-title">Danh Sách Đề Nghị Xuất Kho</div>
                
                <table>
                    <thead>
                        <tr>
                            <th>Mã Phiếu</th>
                            <th>Ngày Lập</th>
                            <th>Trạng Thái</th>
                            <th>Người Duyệt</th>
                            <th>Mục Đích / Lý do</th>
                            <th>Thao Tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% 
                            List<PhieuXuatDTO> danhSachPX = (List<PhieuXuatDTO>) request.getAttribute("danhSachPhieu");
                            if(danhSachPX != null && !danhSachPX.isEmpty()) {
                                for(PhieuXuatDTO px : danhSachPX) {
                                    boolean isPending = (px.getMaNguoiDuyet() == null);
                                    boolean isRejected = (px.getMucDich() != null && px.getMucDich().startsWith("Từ chối:"));
                        %>
                        <tr>
                            <td><strong><%= px.getMaPhieu() %></strong></td>
                            <td><%= px.getNgayLap() %></td>
                            <td>
                                <% if(isPending) { %> <span class="badge-pending">⏳ Chờ duyệt</span>
                                <% } else if(isRejected) { %> <span class="badge-rejected">❌ Bị từ chối</span>
                                <% } else { %> <span class="badge-approved">✅ Đã duyệt</span> <% } %>
                            </td>
                            <td><%= isPending ? "-" : px.getMaNguoiDuyet() %></td>
                            <td style="color: #666; font-style: italic; max-width: 200px;"><%= px.getMucDich() != null ? px.getMucDich() : "" %></td>
                            
                            <td>
                                <% if(isPending) { %>
                                    <button onclick="xacNhanDuyet('<%= px.getMaPhieu() %>')" class="btn-sm btn-approve">✔ Duyệt</button>
                                    <button onclick="xacNhanTuChoi('<%= px.getMaPhieu() %>')" class="btn-sm btn-reject">✖ Từ chối</button>
                                <% } else { %>
                                    <button class="btn-sm btn-view btn-chi-tiet" 
                                            data-maphieu="<%= px.getMaPhieu() %>"
                                            data-ngaylap="<%= px.getNgayLap() %>"
                                            data-bophan="<%= px.getBoPhanNhan() %>"
                                            data-tenhang="<%= px.getTenHang() %>"
                                            data-soluong="<%= px.getSoLuong() %>"
                                            data-dongia="<%= px.getDonGia() %>"
                                            data-mucdich="<%= px.getMucDich() != null ? px.getMucDich() : "" %>">
                                        👁 Xem chi tiết
                                    </button>
                                <% } %>
                            </td>
                        </tr>
                        <% 
                                }
                            } else { out.print("<tr><td colspan='6'>Không có phiếu xuất nào.</td></tr>"); }
                        %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <div id="chiTietModal" class="modal-overlay" style="display: none;">
        <div class="modal-content">
            <div class="modal-header">
                <h3>📤 Chi Tiết Phiếu Xuất Kho</h3>
                <span class="close-btn" onclick="dongModal()">&times;</span>
            </div>
            <div class="modal-body">
                <p><strong>Mã Phiếu:</strong> <span id="md-maPhieu" style="color: #f39c12; font-weight: bold;"></span></p>
                <p><strong>Ngày Đề Nghị:</strong> <span id="md-ngayLap"></span></p>
                <p><strong>Bộ Phận Nhận:</strong> <span id="md-boPhan" style="font-weight: bold;"></span></p>
                <hr style="border-top: 1px dashed #ccc; margin: 15px 0;">
                <p><strong>📦 Sản Phẩm:</strong> <span id="md-tenHang"></span></p>
                <p><strong>Số Lượng Xuất:</strong> <span id="md-soLuong"></span></p>
                <p><strong>Đơn Giá Xuất:</strong> <span id="md-donGia"></span></p>
                <p><strong>Thành Tiền:</strong> <span id="md-thanhTien" style="color: #e74c3c; font-weight: bold;"></span></p>
                <hr style="border-top: 1px dashed #ccc; margin: 15px 0;">
                <p><strong>Mục đích / Ghi chú:</strong> <span id="md-mucDich" style="font-style: italic; color: #555;"></span></p>
            </div>
            <div class="modal-footer">
                <button class="btn-sm btn-view" style="padding: 8px 15px;" onclick="dongModal()">Đóng Cửa Sổ</button>
            </div>
        </div>
    </div>

    <script>
        function xacNhanDuyet(maPhieu) {
            if(confirm("Bạn có chắc chắn muốn DUYỆT phiếu xuất " + maPhieu + " này không?")) {
                window.location.href = "XuLyDuyetPhieuServlet?action=duyet&loai=xuat&maPhieu=" + maPhieu;
            }
        }

        function xacNhanTuChoi(maPhieu) {
            let lyDo = prompt("Vui lòng nhập lý do TỪ CHỐI phiếu xuất " + maPhieu + ":");
            if(lyDo != null && lyDo.trim() !== "") {
                window.location.href = "XuLyDuyetPhieuServlet?action=tuchoi&loai=xuat&maPhieu=" + maPhieu + "&lyDo=" + encodeURIComponent(lyDo.trim());
            } else if(lyDo !== null) {
                alert("Bạn phải nhập lý do thì mới được từ chối!");
            }
        }

        document.querySelectorAll('.btn-chi-tiet').forEach(button => {
            button.addEventListener('click', function() {
                document.getElementById('md-maPhieu').innerText = this.getAttribute('data-maphieu');
                document.getElementById('md-ngayLap').innerText = this.getAttribute('data-ngaylap');
                document.getElementById('md-boPhan').innerText = this.getAttribute('data-bophan');
                document.getElementById('md-tenHang').innerText = this.getAttribute('data-tenhang');
                
                let soLuong = parseFloat(this.getAttribute('data-soluong'));
                let donGia = parseFloat(this.getAttribute('data-dongia'));
                
                document.getElementById('md-soLuong').innerText = soLuong;
                document.getElementById('md-donGia').innerText = new Intl.NumberFormat('vi-VN').format(donGia) + " VNĐ";
                document.getElementById('md-thanhTien').innerText = new Intl.NumberFormat('vi-VN').format(soLuong * donGia) + " VNĐ";
                
                let mucDich = this.getAttribute('data-mucdich');
                document.getElementById('md-mucDich').innerText = mucDich ? mucDich : "Không có ghi chú.";
                
                document.getElementById('chiTietModal').style.display = 'flex';
            });
        });

        function dongModal() {
            document.getElementById('chiTietModal').style.display = 'none';
        }
    </script>
</body>
</html>