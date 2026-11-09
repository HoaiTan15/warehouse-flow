// =============================================================================
// SCRIPT KHỞI TẠO ĐỒ THỊ KHO HÀNG TRÊN NEO4J (WAREHOUSE GRAPH)
// Phục vụ: 
// 1. Truy vết chuỗi cung ứng (Supply Chain Traceability)
// 2. Gợi ý mặt hàng xuất cùng nhau (Recommendation)
// 3. Phân tích rủi ro & phụ thuộc nhà cung cấp (Risk Analysis)
// =============================================================================

// 1. RÀNG BUỘC DUY NHẤT (CONSTRAINTS)
CREATE CONSTRAINT c_hanghoa IF NOT EXISTS FOR (h:HangHoa) REQUIRE h.maHang IS UNIQUE;
CREATE CONSTRAINT c_ncc IF NOT EXISTS FOR (ncc:NhaCungCap) REQUIRE ncc.maNCC IS UNIQUE;
CREATE CONSTRAINT c_vitri IF NOT EXISTS FOR (vt:ViTriKho) REQUIRE vt.maViTri IS UNIQUE;
CREATE CONSTRAINT c_bophan IF NOT EXISTS FOR (bp:BoPhan) REQUIRE bp.tenBoPhan IS UNIQUE;
CREATE CONSTRAINT c_phieuxuat IF NOT EXISTS FOR (px:PhieuXuat) REQUIRE px.maPhieu IS UNIQUE;

// 2. TẠO CÁC NÚT NHÀ CUNG CẤP
MERGE (ncc1:NhaCungCap {maNCC: 'NCC01'})
SET ncc1.tenNCC = 'Unilever Việt Nam', ncc1.diaChi = 'Quận 7, TP.HCM', ncc1.sdt = '02838123456', ncc1.email = 'contact@unilever.com';

MERGE (ncc2:NhaCungCap {maNCC: 'NCC02'})
SET ncc2.tenNCC = 'Masan Consumer', ncc2.diaChi = 'Quận 1, TP.HCM', ncc2.sdt = '02838123457', ncc2.email = 'sales@masan.com';

MERGE (ncc3:NhaCungCap {maNCC: 'NCC03'})
SET ncc3.tenNCC = 'Abbott Việt Nam', ncc3.diaChi = 'Quận 1, TP.HCM', ncc3.sdt = '02838123458', ncc3.email = 'info@abbott.com';

// 3. TẠO CÁC NÚT VỊ TRÍ KỆ KHO
MERGE (vt1:ViTriKho {maViTri: 'K1-T1'}) SET vt1.tenViTri = 'Kệ 1 - Tầng 1 (Khu A)';
MERGE (vt2:ViTriKho {maViTri: 'K1-T2'}) SET vt2.tenViTri = 'Kệ 1 - Tầng 2 (Khu A)';
MERGE (vt3:ViTriKho {maViTri: 'K2-T1'}) SET vt3.tenViTri = 'Kệ 2 - Tầng 1 (Khu B)';

// 4. TẠO CÁC NÚT HÀNG HÓA VÀ LIÊN KẾT LƯU TRỮ TẠI KỆ
MERGE (h1:HangHoa {maHang: 'SP01'})
SET h1.tenHang = 'Dầu gội Clear Men', h1.dvt = 'Chai', h1.quyCach = '650g/chai';

MERGE (h2:HangHoa {maHang: 'SP02'})
SET h2.tenHang = 'Nước mắm Nam Ngư', h2.dvt = 'Chai', h2.quyCach = '500ml/chai';

MERGE (h3:HangHoa {maHang: 'SP03'})
SET h3.tenHang = 'Sữa bột Ensure Gold', h3.dvt = 'Hộp', h3.quyCach = '850g/hộp';

MERGE (h4:HangHoa {maHang: 'SP04'})
SET h4.tenHang = 'Bột giặt OMO', h4.dvt = 'Bịch', h4.quyCach = '4.5kg/bịch';

// Liên kết Vị trí kho
MERGE (h1)-[:LUU_TAI]->(vt1);
MERGE (h2)-[:LUU_TAI]->(vt2);
MERGE (h3)-[:LUU_TAI]->(vt3);
MERGE (h4)-[:LUU_TAI]->(vt3);

// 5. LIÊN KẾT NHÀ CUNG CẤP VỚI HÀNG HÓA (CUNG_CAP)
MERGE (ncc1)-[:CUNG_CAP]->(h1);
MERGE (ncc1)-[:CUNG_CAP]->(h4);
MERGE (ncc2)-[:CUNG_CAP]->(h2);
MERGE (ncc3)-[:CUNG_CAP]->(h3);

// 6. TẠO BỘ PHẬN NHẬN VÀ PHIẾU XUẤT HÀNG
MERGE (bp1:BoPhan {tenBoPhan: 'Phòng Kinh Doanh'});
MERGE (bp2:BoPhan {tenBoPhan: 'Phân Xưởng Sản Xuất'});
MERGE (bp3:BoPhan {tenBoPhan: 'Đại Lý Phân Phối'});

// Phiếu xuất PX001 (Xuất Dầu gội SP01 và Bột giặt SP04 cho Phòng Kinh Doanh)
MERGE (px1:PhieuXuat {maPhieu: 'PX001'})
SET px1.ngayLap = '2026-05-21', px1.mucDich = 'Giao đại lý Bình Thạnh';
MERGE (px1)-[:CAP_CHO]->(bp1);
MERGE (px1)-[:XUAT_HANG {soLuong: 20, donGia: 150000.0}]->(h1);
MERGE (px1)-[:XUAT_HANG {soLuong: 50, donGia: 180000.0}]->(h4);

// Phiếu xuất PX002 (Xuất Dầu gội SP01 và Bột giặt SP04 cho Đại Lý)
MERGE (px2:PhieuXuat {maPhieu: 'PX002'})
SET px2.ngayLap = '2026-06-10', px2.mucDich = 'Cung cấp hàng định kỳ';
MERGE (px2)-[:CAP_CHO]->(bp3);
MERGE (px2)-[:XUAT_HANG {soLuong: 15, donGia: 150000.0}]->(h1);
MERGE (px2)-[:XUAT_HANG {soLuong: 30, donGia: 180000.0}]->(h4);

// Phiếu xuất PX003 (Xuất Nước mắm SP02 cho Phân Xưởng)
MERGE (px3:PhieuXuat {maPhieu: 'PX003'})
SET px3.ngayLap = '2026-06-15', px3.mucDich = 'Bếp ăn công nghiệp';
MERGE (px3)-[:CAP_CHO]->(bp2);
MERGE (px3)-[:XUAT_HANG {soLuong: 10, donGia: 25000.0}]->(h2);

