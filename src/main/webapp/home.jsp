<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.taikhoan" %>
<%@ page import="model.DailyWarehouseStats" %>
<%@ page import="model.TransactionFeedItem" %>
<%@ page import="nosql.redis.WarehouseRedisService" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.Locale" %>
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

    // 3. LẤY DỮ LIỆU THỐNG KÊ VÀ HOẠT ĐỘNG GẦN ĐÂY TỪ REDIS
    WarehouseRedisService redisService = new WarehouseRedisService();
    DailyWarehouseStats redisStats = redisService.getDailyStats(null);
    List<TransactionFeedItem> redisFeed = redisService.getRecentTransactions(8);
    NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
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
        .section-header { font-size: 22px; font-weight: bold; margin-bottom: 20px; color: #2c3e50; display: flex; align-items: center; justify-content: space-between; }
        .redis-tag { font-size: 11px; background-color: #d63031; color: white; padding: 3px 8px; border-radius: 12px; font-weight: normal; vertical-align: middle; margin-left: 8px; }

        .stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; margin-bottom: 30px; }
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
        .stat-info p { font-size: 24px; font-weight: bold; color: #2c3e50; }
        .stat-sub { font-size: 12px; color: #888; margin-top: 3px; }

        /* Bảng hoạt động Redis */
        .feed-container { background: white; padding: 20px; border-radius: 12px; border: 1px solid #e1e5eb; margin-bottom: 40px; box-shadow: 0 4px 10px rgba(0,0,0,0.03); }
        .feed-table { width: 100%; border-collapse: collapse; margin-top: 10px; font-size: 14px; }
        .feed-table th { background-color: #f8f9fa; color: #636e72; padding: 12px 15px; text-align: left; border-bottom: 2px solid #e1e5eb; font-weight: 600; }
        .feed-table td { padding: 12px 15px; border-bottom: 1px solid #f1f2f6; }
        .feed-table tr:hover { background-color: #fcfcfd; }
        .badge-nhap { background: #e8f8f0; color: #27ae60; padding: 3px 10px; border-radius: 12px; font-weight: bold; font-size: 11px; }
        .badge-xuat { background: #fef5e7; color: #e67e22; padding: 3px 10px; border-radius: 12px; font-weight: bold; font-size: 11px; }
        .empty-feed { text-align: center; color: #95a5a6; padding: 25px 0; font-style: italic; }

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
        
        <div class="section-header">
            <span>📊 Thống Kê Trong Ngày <span class="redis-tag">Redis Real-Time Hash</span></span>
            <span style="font-size: 13px; color: #7f8c8d; font-weight: normal;">Hôm nay: <%= redisStats.getDate() %></span>
        </div>
        <div class="stats-grid">
            <div class="stat-card green">
                <div class="stat-icon green-bg">📥</div>
                <div class="stat-info">
                    <h4>Nhập Kho Hôm Nay</h4>
                    <p><%= redisStats.getSoPhieuNhap() %> <span style="font-size: 14px; font-weight: normal; color: #7f8c8d;">phiếu</span></p>
                    <div class="stat-sub"><%= currencyFormat.format(redisStats.getTongTienNhap()) %> đ</div>
                </div>
            </div>
            <div class="stat-card orange">
                <div class="stat-icon orange-bg">📤</div>
                <div class="stat-info">
                    <h4>Xuất Kho Hôm Nay</h4>
                    <p><%= redisStats.getSoPhieuXuat() %> <span style="font-size: 14px; font-weight: normal; color: #7f8c8d;">phiếu</span></p>
                    <div class="stat-sub"><%= currencyFormat.format(redisStats.getTongTienXuat()) %> đ</div>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon blue-bg">⚡</div>
                <div class="stat-info">
                    <h4>Tổng Giao Dịch</h4>
                    <p><%= redisStats.getTongSoPhieu() %> <span style="font-size: 14px; font-weight: normal; color: #7f8c8d;">lượt</span></p>
                    <div class="stat-sub">Tự động cập nhật 0ms</div>
                </div>
            </div>
            <div class="stat-card red">
                <div class="stat-icon red-bg">💰</div>
                <div class="stat-info">
                    <h4>Tổng Giá Trị</h4>
                    <p style="font-size: 19px;"><%= currencyFormat.format(redisStats.getTongGiaTriGiaoDich()) %> đ</p>
                    <div class="stat-sub">Lưu in-memory TTL 3 ngày</div>
                </div>
            </div>
        </div>

        <%-- BẢNG FEED HOẠT ĐỘNG GẦN ĐÂY TỪ REDIS LIST --%>
        <div class="feed-container">
            <div class="section-header" style="margin-bottom: 10px;">
                <span>⚡ Hoạt Động Xuất - Nhập Kho Gần Đây <span class="redis-tag">Redis List LPUSH / LTRIM</span></span>
                <span style="font-size: 13px; color: #888; font-weight: normal;">Lưu tối đa 50 giao dịch mới nhất</span>
            </div>
            <% if (redisFeed == null || redisFeed.isEmpty()) { %>
                <div class="empty-feed">
                    Chưa có hoạt động xuất nhập mới trong cache Redis.<br>
                    <small>(Dữ liệu sẽ tự động xuất hiện tức thì mỗi khi bạn tạo phiếu Nhập hoặc Xuất kho)</small>
                </div>
            <% } else { %>
                <table class="feed-table">
                    <thead>
                        <tr>
                            <th>Thời Gian</th>
                            <th>Loại</th>
                            <th>Mã Phiếu</th>
                            <th>Người Lập</th>
                            <th>Số Mặt Hàng</th>
                            <th>Tổng Tiền</th>
                            <th>Nội Dung / Ghi Chú</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% for (TransactionFeedItem item : redisFeed) { 
                            boolean isNhap = "NHAP".equalsIgnoreCase(item.getLoaiPhieu());
                        %>
                        <tr>
                            <td style="color: #636e72; font-size: 13px;"><%= item.getThoiGian() %></td>
                            <td>
                                <span class="<%= isNhap ? "badge-nhap" : "badge-xuat" %>">
                                    <%= isNhap ? "📥 NHẬP KHO" : "📤 XUẤT KHO" %>
                                </span>
                            </td>
                            <td><b><%= item.getMaPhieu() %></b></td>
                            <td><%= item.getNguoiThucHien() != null ? item.getNguoiThucHien() : "—" %></td>
                            <td><%= item.getSoLuongMatHang() %> mặt hàng</td>
                            <td style="font-weight: 600; color: <%= isNhap ? "#27ae60" : "#d35400" %>;">
                                <%= currencyFormat.format(item.getTongTien()) %> đ
                            </td>
                            <td style="color: #555;"><%= item.getGhiChu() != null ? item.getGhiChu() : "" %></td>
                        </tr>
                        <% } %>
                    </tbody>
                </table>
            <% } %>
        </div>

        <h2 class="section-header" style="padding-top: 20px; border-top: 2px dashed #dcdde1;">
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