<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, java.util.Map, model.taikhoan, model.PhieuNhapDTO, model.PhieuXuatDTO, model.KiemKeDTO, model.SupplierImpactAnalysisDTO" %>
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
    boolean showLichSu       = role.equalsIgnoreCase("Thủ kho") || role.equalsIgnoreCase("Trưởng kho") || role.equalsIgnoreCase("Giám đốc") || role.equalsIgnoreCase("Admin") || role.equalsIgnoreCase("Kế toán");
    boolean showKeToan       = role.equalsIgnoreCase("Kế toán") || role.equalsIgnoreCase("Giám đốc");

    // 3. Lấy dữ liệu hiển thị
    String loai = (String) request.getAttribute("loaiHienTai");
    if(loai == null) loai = "nhapkho";
    String filter = (String) request.getAttribute("filterHienTai");
    if(filter == null) filter = "all";

    SupplierImpactAnalysisDTO supplierImpact = (SupplierImpactAnalysisDTO) request.getAttribute("supplierImpact");
    String supplierGraphJson = (String) request.getAttribute("supplierGraphJson");
    List<Map<String, String>> supplierList = (List<Map<String, String>>) request.getAttribute("supplierList");
    String selectedMaNCC = (String) request.getAttribute("selectedMaNCC");
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Trung Tâm Báo Cáo Thống Kê</title>
    <!-- Thư viện vis-network vẽ đồ thị Neo4j -->
    <script src="https://unpkg.com/vis-network/standalone/umd/vis-network.min.js"></script>
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
        
        /* CSS Giao diện Card & Table */
        .card { background: #fff; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); padding: 25px; margin: 20px 30px; border: 1px solid #eee; }
        .filter-container { display: flex; flex-wrap: wrap; gap: 15px; align-items: center; background: #f8f9fa; padding: 15px; border-radius: 5px; margin-bottom: 20px; border: 1px solid #eee; }
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
                <select name="loai" id="loaiSelect" onchange="this.form.submit()">
                    <option value="nhapkho" <%= "nhapkho".equals(loai) ? "selected" : "" %>>Báo Cáo Nhập Kho</option>
                    <option value="xuatkho" <%= "xuatkho".equals(loai) ? "selected" : "" %>>Báo Cáo Xuất Kho</option>
                    <option value="kiemke" <%= "kiemke".equals(loai) ? "selected" : "" %>>Báo Cáo Kiểm Kê</option>
                    <option value="ruiro_ncc" <%= "ruiro_ncc".equals(loai) ? "selected" : "" %>>⚠️ Phân Tích Rủi Ro NCC (Neo4j Graph)</option>
                </select>

                <span id="standardFilterGroup" style="<%= "ruiro_ncc".equals(loai) ? "display:none;" : "display:inline-flex; align-items:center; gap:10px;" %>">
                    <label><strong>Bộ Lọc:</strong></label>
                    <select name="filter" id="filterSelect"></select>
                </span>

                <span id="nccFilterGroup" style="<%= "ruiro_ncc".equals(loai) ? "display:inline-flex; align-items:center; gap:10px;" : "display:none;" %>">
                    <label><strong>Chọn Nhà Cung Cấp:</strong></label>
                    <select name="maNCC" id="nccSelect" onchange="this.form.submit()">
                        <% if (supplierList != null) {
                            for (Map<String, String> ncc : supplierList) {
                                String m = ncc.get("maNCC");
                                String t = ncc.get("tenNCC");
                                boolean sel = m != null && m.equalsIgnoreCase(selectedMaNCC);
                        %>
                            <option value="<%= m %>" <%= sel ? "selected" : "" %>><%= m %> - <%= t %></option>
                        <%  }
                        } %>
                    </select>
                </span>
                
                <button type="submit">Xem Báo Cáo</button>
            </form>

            <% if ("ruiro_ncc".equals(loai)) { %>
                <% if (supplierImpact != null) { 
                    String riskColor = "#27ae60";
                    String riskBg = "#d4edda";
                    if ("CAO".equalsIgnoreCase(supplierImpact.getMucDoRuiRo())) {
                        riskColor = "#c0392b";
                        riskBg = "#f8d7da";
                    } else if ("TRUNG BÌNH".equalsIgnoreCase(supplierImpact.getMucDoRuiRo())) {
                        riskColor = "#d35400";
                        riskBg = "#ffeaa7";
                    }

                    int tongSLTieuthu = 0;
                    for (SupplierImpactAnalysisDTO.DepartmentImpact bp : supplierImpact.getDanhSachBoPhanAnhHuong()) {
                        tongSLTieuthu += bp.getTongSoLuongDaNhan();
                    }
                %>
                <!-- KPI SUMMARY CARDS -->
                <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 15px; margin-bottom: 25px;">
                    <div style="background: <%= riskBg %>; border: 1px solid <%= riskColor %>; padding: 18px; border-radius: 8px; text-align: center;">
                        <div style="font-size: 13px; font-weight: bold; color: <%= riskColor %>; text-transform: uppercase;">Mức Độ Rủi Ro</div>
                        <div style="font-size: 26px; font-weight: 800; color: <%= riskColor %>; margin-top: 5px;"><%= supplierImpact.getMucDoRuiRo() %></div>
                        <div style="font-size: 11px; color: #555; margin-top: 5px;">Dựa trên phân tích phụ thuộc Neo4j</div>
                    </div>

                    <div style="background: #eef2ff; border: 1px solid #c7d2fe; padding: 18px; border-radius: 8px; text-align: center;">
                        <div style="font-size: 13px; font-weight: bold; color: #3730a3; text-transform: uppercase;">Số Mặt Hàng Cung Cấp</div>
                        <div style="font-size: 26px; font-weight: 800; color: #4338ca; margin-top: 5px;"><%= supplierImpact.getDanhSachHangCungCap().size() %> mặt hàng</div>
                        <div style="font-size: 11px; color: #6b7280; margin-top: 5px;">Đang phân phối trong kho</div>
                    </div>

                    <div style="background: #fdf2f8; border: 1px solid #fbcfe8; padding: 18px; border-radius: 8px; text-align: center;">
                        <div style="font-size: 13px; font-weight: bold; color: #9d174d; text-transform: uppercase;">Phòng Ban / Đơn Vị Ảnh Hưởng</div>
                        <div style="font-size: 26px; font-weight: 800; color: #be185d; margin-top: 5px;"><%= supplierImpact.getDanhSachBoPhanAnhHuong().size() %> bộ phận</div>
                        <div style="font-size: 11px; color: #6b7280; margin-top: 5px;">Bị gián đoạn nếu NCC đứt gãy</div>
                    </div>

                    <div style="background: #ecfdf5; border: 1px solid #a7f3d0; padding: 18px; border-radius: 8px; text-align: center;">
                        <div style="font-size: 13px; font-weight: bold; color: #065f46; text-transform: uppercase;">Tổng SL Đã Xuất Cung Ứng</div>
                        <div style="font-size: 26px; font-weight: 800; color: #047857; margin-top: 5px;"><%= tongSLTieuthu %> sản phẩm</div>
                        <div style="font-size: 11px; color: #6b7280; margin-top: 5px;">Phục vụ hoạt động các đơn vị</div>
                    </div>
                </div>

                <!-- NEO4J INTERACTIVE RISK GRAPH CANVAS -->
                <div style="background: #ffffff; border: 1px solid #e0e6ed; border-radius: 8px; padding: 20px; margin-bottom: 25px; box-shadow: 0 1px 3px rgba(0,0,0,0.05);">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px;">
                        <div style="font-size: 16px; font-weight: bold; color: #2c3e50; display: flex; align-items: center; gap: 8px;">
                            <span>🌐 Sơ Đồ Mạng Lưới Lan Truyền Rủi Ro (Supplier Risk Propagation Graph)</span>
                            <span style="font-size: 11px; font-weight: normal; background: #e0f2fe; color: #0369a1; padding: 2px 8px; border-radius: 12px;">Powered by Neo4j</span>
                        </div>
                        <div style="font-size: 12px; color: #666;">
                            💡 <i>Kéo thả các node, cuộn chuột để phóng to/thu nhỏ đồ thị.</i>
                        </div>
                    </div>
                    <div id="supplierGraphCanvas" style="height: 420px; width: 100%; border: 1px dashed #cbd5e1; border-radius: 6px; background: #fafafa;"></div>
                    <div style="display: flex; gap: 20px; margin-top: 12px; font-size: 12px; color: #555; justify-content: center;">
                        <span><b style="color: <%= riskColor %>;">■</b> Nhà Cung Cấp (<%= supplierImpact.getTenNCC() %>)</span>
                        <span><b style="color: #f39c12;">■</b> Hàng Hóa Cung Cấp</span>
                        <span><b style="color: #8e44ad;">■</b> Phòng Ban / Bộ Phận Tiếp Nhận</span>
                    </div>
                </div>

                <!-- IMPACT MATRIX TABLE -->
                <div style="background: #ffffff; border: 1px solid #e0e6ed; border-radius: 8px; padding: 20px;">
                    <h4 style="margin-top: 0; color: #2c3e50; margin-bottom: 15px;">📊 Ma Trận Mức Độ Phụ Thuộc & Ảnh Hưởng Chi Tiết Theo Phòng Ban</h4>
                    <table>
                        <thead>
                            <tr>
                                <th style="width: 25%;">Phòng Ban / Đơn Vị Tiếp Nhận</th>
                                <th style="width: 35%;">Các Mặt Hàng Phụ Thuộc Vào NCC Này</th>
                                <th style="width: 15%; text-align: center;">Tổng SL Đã Nhận</th>
                                <th style="width: 12%; text-align: center;">Số Đơn Hàng</th>
                                <th style="width: 13%; text-align: center;">Đánh Giá Mức Độ</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% if (!supplierImpact.getDanhSachBoPhanAnhHuong().isEmpty()) {
                                for (SupplierImpactAnalysisDTO.DepartmentImpact bp : supplierImpact.getDanhSachBoPhanAnhHuong()) {
                                    int sl = bp.getTongSoLuongDaNhan();
                                    String tagClass = "badge-green";
                                    String tagText = "Thấp";
                                    if (sl >= 30) {
                                        tagClass = "badge-red";
                                        tagText = "Rất Cao";
                                    } else if (sl >= 10) {
                                        tagClass = "badge-yellow";
                                        tagText = "Trung Bình";
                                    }
                            %>
                                <tr>
                                    <td><b>🏢 <%= bp.getTenBoPhan() %></b></td>
                                    <td>
                                        <% for (String h : bp.getDanhSachHangPhuThuoc()) { %>
                                            <span style="display: inline-block; background: #f1f5f9; border: 1px solid #cbd5e1; color: #334155; padding: 3px 8px; border-radius: 12px; font-size: 12px; margin: 2px;">
                                                📦 <%= h %>
                                            </span>
                                        <% } %>
                                    </td>
                                    <td style="text-align: center; font-weight: bold; color: #1e293b;"><%= sl %></td>
                                    <td style="text-align: center;"><%= bp.getSoDonHangLienQuan() %> đơn</td>
                                    <td style="text-align: center;"><span class="<%= tagClass %>"><%= tagText %></span></td>
                                </tr>
                            <%  }
                            } else { %>
                                <tr>
                                    <td colspan="5" style="text-align: center; color: #888; padding: 25px;">
                                        <i>Nhà cung cấp này chưa có hàng hóa được phân phối xuất kho đến các phòng ban.</i>
                                    </td>
                                </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>

                <% } else { %>
                    <div style="text-align: center; padding: 40px; background: #fff; border-radius: 8px; border: 1px dashed #cbd5e1;">
                        <p style="color: #64748b; font-size: 15px;">Không tìm thấy dữ liệu liên kết đồ thị cho Nhà Cung Cấp này trong Graph Database.</p>
                    </div>
                <% } %>

            <% } else { %>
                <!-- Bảng báo cáo truyền thống (Nhập kho, Xuất kho, Kiểm kê) -->
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
            <% } %>
        </div>
    </div>

    <script>
        function updateFilterOptions() {
            var loai = document.getElementById("loaiSelect").value;
            var standardGroup = document.getElementById("standardFilterGroup");
            var nccGroup = document.getElementById("nccFilterGroup");

            if (loai === "ruiro_ncc") {
                if (standardGroup) standardGroup.style.display = "none";
                if (nccGroup) nccGroup.style.display = "inline-flex";
                return;
            } else {
                if (standardGroup) standardGroup.style.display = "inline-flex";
                if (nccGroup) nccGroup.style.display = "none";
            }

            var filterSelect = document.getElementById("filterSelect");
            var currentFilterValue = filterSelect.value;
            
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
            
            if (filterSelect) {
                for(var i = 0; i < filterSelect.options.length; i++) {
                    if(filterSelect.options[i].value === serverFilter) {
                        filterSelect.selectedIndex = i;
                        break;
                    }
                }
            }

            <% if ("ruiro_ncc".equals(loai) && supplierGraphJson != null && !supplierGraphJson.trim().isEmpty()) { %>
            try {
                var graphDataRaw = <%= supplierGraphJson %>;
                var container = document.getElementById('supplierGraphCanvas');
                if (container && graphDataRaw && graphDataRaw.nodes && graphDataRaw.nodes.length > 0) {
                    var data = {
                        nodes: new vis.DataSet(graphDataRaw.nodes),
                        edges: new vis.DataSet(graphDataRaw.edges)
                    };
                    var options = {
                        physics: {
                            stabilization: true,
                            barnesHut: {
                                springLength: 140,
                                avoidOverlap: 0.3
                            }
                        },
                        nodes: {
                            margin: 10,
                            shadow: true,
                            font: { face: 'Segoe UI', size: 12 }
                        },
                        edges: {
                            smooth: { type: 'cubicBezier', roundness: 0.4 },
                            font: { size: 11, align: 'middle' },
                            arrows: { to: { enabled: true, scaleFactor: 0.8 } }
                        },
                        interaction: {
                            hover: true,
                            navigationButtons: true,
                            keyboard: true
                        }
                    };
                    new vis.Network(container, data, options);
                }
            } catch(e) {
                console.error("Lỗi khởi tạo sơ đồ mạng lưới Neo4j:", e);
            }
            <% } %>
        };
    </script>
</body>
</html>
