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

// Insert dữ liệu mẫu
db.ho_so_hang_hoa.insertMany([
    {
        maHang: "HH001",
        moTaChiTiet: "Laptop Dell Inspiron 15 3000 Series với màn hình 15.6 inch, CPU Intel Core i5 thế hệ 11, RAM 8GB, SSD 256GB",
        thuocTinhMoRong: {
            hang: "Dell",
            mauSac: "Xám bạc",
            kichThuoc: "15.6 inch",
            trongLuong: "1.85 kg",
            baoHanh: "12 tháng"
        },
        tags: ["laptop", "dell", "vp-phong"],
        ghiChu: "Sản phẩm dành cho văn phòng, học tập",
        ngayCapNhat: new Date()
    },
    {
        maHang: "HH002",
        moTaChiTiet: "Chuột không dây Logitech M280 với kết nối USB, pin AAA, tầm xa 10m",
        thuocTinhMoRong: {
            hang: "Logitech",
            mauSac: "Đen",
            ketNoi: "USB Wireless",
            pin: "1 x AAA"
        },
        tags: ["chuột", "logitech", "khong-day"],
        ghiChu: "Phụ kiện máy tính",
        ngayCapNhat: new Date()
    },
    {
        maHang: "HH003",
        moTaChiTiet: "Bàn phím cơ Gaming RGB với switch Blue, có đèn LED nhiều màu",
        thuocTinhMoRong: {
            hang: "Rapoo",
            loaiSwitch: "Blue",
            denLED: "RGB",
            ketNoi: "USB"
        },
        tags: ["ban-phim", "gaming", "co"],
        ghiChu: "Dành cho game thủ",
        ngayCapNhat: new Date()
    }
]);

print('Sample data inserted into ho_so_hang_hoa');

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

// Insert dữ liệu mẫu
db.nhat_ky_thao_tac_kho.insertMany([
    {
        loaiThaoTac: "THEM_HANG_HOA",
        maDoiTuong: "HH001",
        tenDangNhap: "admin",
        thoiGian: new Date(),
        ketQua: "THANH_CONG",
        chiTiet: "Thêm hàng hóa: Laptop Dell Inspiron 15"
    },
    {
        loaiThaoTac: "LAP_PHIEU_NHAP",
        maDoiTuong: "PN001",
        tenDangNhap: "thukho01",
        thoiGian: new Date(),
        ketQua: "THANH_CONG",
        chiTiet: "Lập phiếu nhập: PN001 - NCC: NCC001"
    },
    {
        loaiThaoTac: "DUYET_PHIEU_NHAP",
        maDoiTuong: "PN001",
        tenDangNhap: "truongkho",
        thoiGian: new Date(),
        ketQua: "THANH_CONG",
        chiTiet: "Duyệt phiếu nhập: PN001"
    }
]);

print('Sample data inserted into nhat_ky_thao_tac_kho');

print('MongoDB initialization completed successfully!');
