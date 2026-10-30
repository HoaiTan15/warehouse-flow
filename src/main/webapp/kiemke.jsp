<%@ page contentType="text/html;charset=UTF-8" language="java" %>
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
    <title>Kiểm Kê Kho - WMS</title>
    <style>
        /* 1. KHUNG BỐ CỤC CHUẨN */
        * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Segoe UI', Arial, sans-serif; }
        body { display: flex; background-color: #f5f6f8; height: 100vh; overflow: hidden; }
        
       .sidebar { width: 250px; min-width: 250px; flex: 0 0 250px; background-color: #ffffff; border-right: 1px solid #e0e0e0; padding: 20px 0; overflow-y: auto; }
        .sidebar-logo { font-size: 20px; font-weight: bold; color: #2c3e50; padding: 0 20px 20px; border-bottom: 1px solid #eee; margin-bottom: 10px; display: flex; align-items: center; gap: 10px;}
        .menu-title { font-size: 11px; color: #888; font-weight: bold; padding: 10px 20px; text-transform: uppercase; }
        .menu-item { padding: 12px 20px; color: #555; text-decoration: none; display: flex; align-items: center; font-weight: 500; font-size: 14px; transition: 0.2s;}
        .menu-item:hover, .menu-item.active { background-color: #e6efff; color: #4b49ac; border-right: 3px solid #4b49ac; font-weight: bold;}
        
        /* 3. MAIN CONTENT BÊN PHẢI */
        .main-content { flex: 1; display: flex; flex-direction: column; overflow-y: auto; }
        .topbar { height: 60px; min-height: 60px; background-color: #ffffff; border-bottom: 1px solid #e0e0e0; display: flex; justify-content: space-between; align-items: center; padding: 0 30px; }
        .content-body { padding: 30px; }

        /* 4. CSS DÀNH RIÊNG CHO FORM KIỂM KÊ CỦA BẠN */
        .card { background-color: #fff; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.02); padding: 25px; border: 1px solid #eee; margin-bottom: 20px;}
        .card-title { font-size: 18px; font-weight: bold; color: #2c3e50; margin-bottom: 20px; border-bottom: 2px solid #f1f2f6; padding-bottom: 10px;}
        
        .row { display: flex; gap: 20px; margin-bottom: 15px; align-items: flex-end;}
        .col { flex: 1; display: flex; flex-direction: column;}
        
        label { font-size: 13px; font-weight: bold; color: #555; margin-bottom: 8px; }
        .form-control { width: 100%; padding: 10px; border: 1px solid #ccc; border-radius: 4px; font-size: 14px; }
        .form-control[readonly] { background-color: #e9ecef; cursor: not-allowed; color: #2c3e50; font-weight: 500;}
        
        .box-highlight { background-color: #fffdf5; padding: 15px; border-radius: 8px; border: 1px solid #ffeaa7; }
        
        .btn { padding: 10px 20px; border: none; border-radius: 4px; cursor: pointer; font-weight: bold; font-size: 14px; transition: 0.2s;}
        .btn-add { background-color: #f39c12; color: white; }
        .btn-add:hover { background-color: #e67e22; }
        .btn-save { background-color: #2ecc71; color: white; margin-left: 10px;}
        .btn-save:hover { background-color: #27ae60; }
        .btn-cancel { background-color: #ecf0f1; color: #7f8c8d; }
        .btn-cancel:hover { background-color: #bdc3c7; }
        
        table { width: 100%; border-collapse: collapse; margin-top: 20px; }
        th, td { padding: 15px; text-align: center; border-bottom: 1px solid #f1f2f6; font-size: 14px; }
        th { background-color: #fcfcfd; font-weight: bold; color: #333;}
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
            <a href="KiemKeServlet" class="menu-item active">Kiểm kê</a>
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
            <div style="font-size: 13px;">
                <b><%= acc.getHoTen() %></b> | <%= acc.getVaiTro() %> 
                <a href="LogoutServlet" style="color: #e74c3c; text-decoration: none; margin-left:10px;">[Đăng xuất]</a>
            </div>
        </div>

        <div class="content-body">
            
            <div class="card">
                <div class="card-title">Tạo Phiếu Kiểm Kê Mới</div>
                <div class="row">
                    <div class="col">
                        <label>Mã Phiếu Kiểm Kê (Tạo tự động)</label>
                        <input type="text" class="form-control" value="PKK_260613_095536" readonly style="color: #2980b9;">
                    </div>
                    <div class="col">
                        <label>Ngày Kiểm Kê</label>
                        <input type="text" class="form-control" value="<%= new java.text.SimpleDateFormat("dd-MM-yyyy").format(new java.util.Date()) %>" readonly>
                    </div>
                </div>
            </div>

            <div class="card">
                <div class="card-title">Đối Chiếu Hàng Hóa Thực Tế Tại Kho</div>
                
                <div class="row box-highlight">
                    <div class="col" style="flex: 2;">
                        <label>Chọn Mặt Hàng Cần Kiểm Kê</label>
                        <select class="form-control">
                            <option>-- Chọn hàng hóa --</option>
                            </select>
                    </div>
                    <div class="col">
                        <label>Tồn Kho (Lý thuyết)</label>
                        <input type="text" class="form-control" readonly>
                    </div>
                    <div class="col">
                        <label>Đếm Thực Tế Tại Kệ</label>
                        <input type="number" class="form-control" placeholder="Nhập số...">
                    </div>
                    <div class="col">
                        <label>Độ Lệch</label>
                        <input type="text" class="form-control" readonly>
                    </div>
                </div>

                <div class="row">
                    <div class="col" style="flex: 3;">
                        <label>Nguyên Nhân (BẮT BUỘC nếu Độ lệch khác 0)</label>
                        <input type="text" class="form-control" placeholder="Ghi chú nguyên nhân lệch kho (hư hỏng, mất mát, đếm sai lần trước...)">
                    </div>
                    <div class="col" style="flex: 1;">
                        <button class="btn btn-add" style="width: 100%; margin-top: 25px;">➕ Ghi Nhận Xuống Bảng</button>
                    </div>
                </div>

                <table>
                    <thead>
                        <tr>
                            <th>MÃ HÀNG</th>
                            <th>TÊN MẶT HÀNG</th>
                            <th>LÝ THUYẾT</th>
                            <th>THỰC TẾ</th>
                            <th>CHÊNH LỆCH</th>
                            <th>NGUYÊN NHÂN</th>
                            <th>THAO TÁC</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td colspan="7" style="color: #999; font-style: italic;">Chưa có mặt hàng nào được ghi nhận.</td>
                        </tr>
                    </tbody>
                </table>
                
                <div style="text-align: right; margin-top: 30px; border-top: 1px solid #eee; padding-top: 20px;">
                    <button class="btn btn-cancel">Hủy Bỏ</button>
                    <button class="btn btn-save">💾 Hoàn Tất & Cập Nhật Số Dư Kho</button>
                </div>
            </div>

        </div>
    </div>
</body> 
</html>