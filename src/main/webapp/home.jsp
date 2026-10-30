<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.taikhoan" %>
<%
    // 1. LẤY THÔNG TIN TÀI KHOẢN TỪ SESSION
    taikhoan acc = (taikhoan) session.getAttribute("account");
    if(acc == null) { response.sendRedirect("login.jsp"); return; }
    
    // 2. SETUP LOGIC PHÂN QUYỀN
    String role = acc.getVaiTro() != null ? acc.getVaiTro() : "";
    
    boolean showHangHoa    = role.equalsIgnoreCase("Thủ kho") || role.equalsIgnoreCase("Trưởng kho") || role.equalsIgnoreCase("Giám đốc");
    boolean showNhapXuat   = role.equalsIgnoreCase("Trưởng kho") || role.equalsIgnoreCase("Giám đốc");
    boolean showBaoCao     = role.equalsIgnoreCase("Thủ kho") || role.equalsIgnoreCase("Trưởng kho") || role.equalsIgnoreCase("Giám đốc") || role.equalsIgnoreCase("Admin") || role.equalsIgnoreCase("Kế toán");
    boolean showCongNo     = role.equalsIgnoreCase("Kế toán") || role.equalsIgnoreCase("Giám đốc") || role.equalsIgnoreCase("Admin"); // MỚI
    boolean showTaiKhoan   = role.equalsIgnoreCase("Admin");
    boolean showDuyetKiemKe = role.equalsIgnoreCase("Kế toán") || role.equalsIgnoreCase("Giám đốc");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Bảng Điều Khiển - WMS</title>
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Arial, sans-serif; }
        body { background-color: #f4f7fa; color: #333; }
        .top-navbar { background-color: #2c3e50; color: white; padding: 15px 30px; display: flex; justify-content: space-between; align-items: center; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }
        .nav-title { font-size: 20px; font-weight: bold; letter-spacing: 1px; }
        .user-panel { display: flex; align-items: center; gap: 15px; font-size: 14px; }
        .badge-role { background-color: #f1c40f; color: #2c3e50; padding: 4px 10px; border-radius: 20px; font-weight: bold; font-size: 12px; text-transform: uppercase; }
        .btn-logout { background-color: #e74c3c; color: white; padding: 6px 15px; text-decoration: none; border-radius: 4px; font-weight: bold; transition: 0.2s; }
        .btn-logout:hover { background-color: #c0392b; }

        .main-container { max-width: 1200px; margin: 30px auto; padding: 0 20px; }
        .section-header { font-size: 22px; font-weight: bold; margin-bottom: 20px; color: #2c3e50; }

        .stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; margin-bottom: 40px; }
        .stat-card { background: white; padding: 20px; border-radius: 10px; box-shadow: 0 4px 6px rgba(0,0,0,0.05); display: flex; align-items: center; border: 1px solid #e1e5eb; border-left: 5px solid #3498db; }
        .stat-card.green { border-left-color: #2ecc71; }
        .stat-card.orange { border-left-color: #f39c12; }
        .stat-card.red { border-left-color: #e74c3c; }
        .stat-icon { font-size: 28px; width: 60px; height: 60px; border-radius: 12px; display: flex; justify-content: center; align-items: center; margin-right: 15px; }
        .stat-icon.blue-bg { background-color: #eaf4fc; color: #3498db; }
        .stat-icon.green-bg { background-color: #ebf9f1; color: #2ecc71; }
        .stat-icon.orange-bg { background-color: #fdf5e7; color: #f39c12; }
        .stat-icon.red-bg { background-color: #fdedec; color: #e74c3c; }
        .stat-info h4 { font-size: 13px; color: #7f8c8d; text-transform: uppercase; margin-bottom: 5px; }
        .stat-info p { font-size: 26px; font-weight: bold; color: #2c3e50; }

        .feature-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 25px; }
        .feature-card { background: white; padding: 40px 20px; border-radius: 12px; text-align: center; text-decoration: none; color: #333; box-shadow: 0 4px 10px rgba(0,0,0,0.03); border: 1px solid #e1e5eb; transition: all 0.3s ease; }
        .feature-card:hover { transform: translateY(-5px); box-shadow: 0 10px 20px rgba(0,0,0,0.08); border-color: #3498db; }
        .feature-icon { font-size: 50px; margin-bottom: 20px; display: inline-block; }
        .feature-title { font-size: 18px; font-weight: bold; color: #2c3e50; }
        .feature-desc { font-size: 13px; color: #7f8c8d; margin-top: 10px; }
    </style>
</head>
<body>

    <div class="top-navbar">
        <div class="nav-title">📦 Bảng Điều Khiển Kho Hàng</div>
        <div class="user-panel">
            <span>Xin chào, <b><%= acc.getHoTen() != null ? acc.getHoTen() : acc.getTenDangNhap() %></b></span>
            <span class="badge-role"><%= acc.getVaiTro() %></span>
            <a href="LogoutServlet" class="btn-logout">Đăng xuất</a>
        </div>
    </div>

    <div class="main-container">
        
        <h2 class="section-header">Tổng quan hệ thống</h2>
        <div class="stats-grid">
            <div class="stat-card">
                <div class="stat-icon blue-bg">📦</div>
                <div class="stat-info"><h4>Tổng Hàng Hóa</h4><p>15</p></div>
            </div>
            <div class="stat-card green">
                <div class="stat-icon green-bg">📥</div>
                <div class="stat-info"><h4>Phiếu Nhập Kho</h4><p>83</p></div>
            </div>
            <div class="stat-card orange">
                <div class="stat-icon orange-bg">📤</div>
                <div class="stat-info"><h4>Phiếu Xuất Kho</h4><p>98</p></div>
            </div>
            <div class="stat-card red">
                <div class="stat-icon red-bg">⚠️</div>
                <div class="stat-info"><h4>Cần Tạo Đơn Nhập</h4><p>3</p></div>
            </div>
        </div>

        <h2 class="section-header" style="margin-top: 50px; padding-top: 20px; border-top: 2px dashed #dcdde1;">
            Khu Vực Làm Việc (<%= role %>)
        </h2>

       <div class="feature-grid">
            
            <%-- 1. QUẢN LÝ HÀNG HÓA --%>
            <% if(showHangHoa) { %>
            <a href="LoadDanhSachKhoServlet" class="feature-card">
                <div class="feature-icon">🗄️</div>
                <div class="feature-title">Quản Lý Hàng Hóa</div>
            </a>
            <% } %>

            <%-- 2. NHẬP/XUẤT (Trưởng kho & Giám đốc) --%>
            <% if(showNhapXuat) { %>
            <a href="QuanLyDanhSachNhapServlet" class="feature-card">
                <div class="feature-icon">📥</div>
                <div class="feature-title">Quản Lý Nhập Kho</div>
            </a>
            <a href="QuanLyDanhSachXuatServlet" class="feature-card">
                <div class="feature-icon">📤</div>
                <div class="feature-title">Quản Lý Xuất Kho</div>
            </a>
             <a href="QuanLyNhaCungCapServlet" class="feature-card">
                <div class="feature-icon">📤</div>
                <div class="feature-title">Quản Lý Nhà Cung Cấp</div>
            </a>
            <% } %>

            <%-- 3. BÁO CÁO & LỊCH SỬ (Cho phép Kế toán xem) --%>
            <% if(showBaoCao) { %>
            <a href="BaoCaoServlet" class="feature-card">
                <div class="feature-icon">📊</div>
                <div class="feature-title">Báo Cáo Tổng Hợp</div>
            </a>
            <a href="LichSuServlet" class="feature-card">
                <div class="feature-icon">🕒</div>
                <div class="feature-title">Lịch Sử Tồn Kho</div>
            </a>
            <% } %>

            <%-- 4. CHỨC NĂNG KẾ TOÁN  --%>
            <% if(showCongNo) { %>
            <a href="CongNoServlet" class="feature-card" style="border-color: #27ae60;">
                <div class="feature-icon">💰</div>
                <div class="feature-title" style="color: #27ae60;">Quản Lý Công Nợ</div>
                <div class="feature-desc">Theo dõi công nợ Nhập - Xuất.</div>
            </a>
            <% } %>
            <%-- CHỨC NĂNG DUYỆT KIỂM KÊ (Cho Kế toán) --%>
            <% if(showDuyetKiemKe) { %>
            <a href="LoadKiemKeDuyetServlet" class="feature-card" style="border-color: #e67e22;">
                <div class="feature-icon">✅</div>
                <div class="feature-title" style="color: #e67e22;">Duyệt Kiểm Kê</div>
                <div class="feature-desc">Phê duyệt chênh lệch kho.</div>
            </a>
            <% } %>

            <%-- 5. QUẢN LÝ TÀI KHOẢN --%>
            <% if(showTaiKhoan) { %>
            <a href="QuanLyTaiKhoanServlet" class="feature-card">
                <div class="feature-icon">👥</div>
                <div class="feature-title">Quản Lý Tài Khoản</div>
            </a>
            <% } %>
        </div>
    </div>
</body>
</html>