// Script khởi tạo dữ liệu MongoDB cho ứng dụng Quản Lý Kho Hàng

// Sử dụng database
db = db.getSiblingDB('quanlykhohang_nosql');

// ============================================================================
// COLLECTION 1: ho_so_hang_hoa
// Lưu hồ sơ mở rộng của hàng hóa
// ============================================================================

print('Creating collection: ho_so_hang_hoa');
db.createCollection('ho_so_hang_hoa');

// Tạo indexes
db.ho_so_hang_hoa.createIndex({ "maHang": 1 }, { unique: true });
db.ho_so_hang_hoa.createIndex({ "tags": 1 });
db.ho_so_hang_hoa.createIndex({ "ngayCapNhat": -1 });

// Hồ sơ hàng hóa thật được ứng dụng đồng bộ từ SQL Server.

// ============================================================================
// COLLECTION 2: nhat_ky_thao_tac_kho
// Lưu nhật ký các thao tác trên hệ thống
// ============================================================================

print('Creating collection: nhat_ky_thao_tac_kho');
db.createCollection('nhat_ky_thao_tac_kho');

// Tạo indexes
db.nhat_ky_thao_tac_kho.createIndex({ "thoiGian": -1 });
db.nhat_ky_thao_tac_kho.createIndex({ "loaiThaoTac": 1 });
db.nhat_ky_thao_tac_kho.createIndex({ "maDoiTuong": 1 });
db.nhat_ky_thao_tac_kho.createIndex({ "tenDangNhap": 1 });

// Nhật ký thật được ứng dụng ghi khi người dùng thao tác.

print('MongoDB initialization completed successfully!');
