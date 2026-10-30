-- =============================================
-- SCRIPT KHỞI TẠO DATABASE QUANLYKHO
-- Tự động tạo database mới không cần chỉ định đường dẫn MDF/LDF
-- =============================================

USE [master]
GO

-- Drop database nếu đã tồn tại (CHÚ Ý: sẽ XÓA toàn bộ dữ liệu)
IF EXISTS (SELECT name FROM sys.databases WHERE name = N'QuanLyKhoHang')
BEGIN
    ALTER DATABASE [QuanLyKhoHang] SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE [QuanLyKhoHang];
    PRINT N'Đã xóa database QuanLyKhoHang cũ';
END
GO

-- Tạo database mới (SQL Server sẽ tự động tạo file ở thư mục DATA mặc định)
CREATE DATABASE [QuanLyKhoHang];
GO

PRINT N'Database QuanLyKhoHang đã được tạo';
GO

USE [QuanLyKhoHang];
GO

-- =============================================
-- TẠO CÁC BẢNG
-- =============================================

-- Bảng BoPhan
CREATE TABLE [dbo].[BoPhan](
	[MaBoPhan] [varchar](20) NOT NULL PRIMARY KEY,
	[TenBoPhan] [nvarchar](100) NOT NULL,
	[MoTa] [nvarchar](255) NULL
);
GO

-- Bảng LoaiHangHoa
CREATE TABLE [dbo].[LoaiHangHoa](
	[MaLoai] [varchar](20) NOT NULL PRIMARY KEY,
	[TenLoai] [nvarchar](100) NOT NULL
);
GO

-- Bảng ViTriKho
CREATE TABLE [dbo].[ViTriKho](
	[MaViTri] [varchar](20) NOT NULL PRIMARY KEY,
	[TenViTri] [nvarchar](100) NOT NULL,
	[TrangThai] [nvarchar](50) NULL
);
GO

-- Bảng NhaCungCap
CREATE TABLE [dbo].[NhaCungCap](
	[MaNCC] [varchar](20) NOT NULL PRIMARY KEY,
	[TenNCC] [nvarchar](150) NOT NULL,
	[DiaChi] [nvarchar](255) NULL,
	[SDT] [varchar](15) NULL,
	[Email] [varchar](100) NULL
);
GO

-- Bảng NhanVien
CREATE TABLE [dbo].[NhanVien](
	[MaNV] [varchar](20) NOT NULL PRIMARY KEY,
	[HoTen] [nvarchar](100) NOT NULL,
	[SDT] [varchar](15) NULL,
	[DiaChi] [nvarchar](255) NULL,
	[NgaySinh] [date] NULL,
	[MaBoPhan] [varchar](20) NULL,
	FOREIGN KEY([MaBoPhan]) REFERENCES [dbo].[BoPhan] ([MaBoPhan])
);
GO

-- Bảng HangHoa
CREATE TABLE [dbo].[HangHoa](
	[MaHang] [varchar](20) NOT NULL PRIMARY KEY,
	[TenHang] [nvarchar](150) NOT NULL,
	[DVT] [nvarchar](50) NULL,
	[SoLuongTonKho] [int] NULL DEFAULT ((0)),
	[QuyCach] [nvarchar](100) NULL,
	[HanMucTonToiThieu] [int] NULL,
	[MaLoai] [varchar](20) NULL,
	[MaViTri] [varchar](20) NULL,
	FOREIGN KEY([MaLoai]) REFERENCES [dbo].[LoaiHangHoa] ([MaLoai]),
	FOREIGN KEY([MaViTri]) REFERENCES [dbo].[ViTriKho] ([MaViTri])
);
GO

-- Bảng TaiKhoan
CREATE TABLE [dbo].[TaiKhoan](
	[TenDangNhap] [varchar](50) NOT NULL PRIMARY KEY,
	[MatKhau] [varchar](255) NOT NULL,
	[VaiTro] [nvarchar](50) NULL,
	[TrangThai] [nvarchar](50) NULL,
	[MaNV] [varchar](20) NULL UNIQUE,
	FOREIGN KEY([MaNV]) REFERENCES [dbo].[NhanVien] ([MaNV])
);
GO

-- Bảng PhieuDeNghiXuat
CREATE TABLE [dbo].[PhieuDeNghiXuat](
	[MaDeNghi] [varchar](20) NOT NULL PRIMARY KEY,
	[NgayDeNghi] [date] NULL,
	[LyDo] [nvarchar](255) NULL,
	[TrangThai] [nvarchar](50) NULL
);
GO

-- Bảng PhieuNhapKho
CREATE TABLE [dbo].[PhieuNhapKho](
	[MaPhieu] [varchar](20) NOT NULL PRIMARY KEY,
	[NgayLap] [date] NULL,
	[MaNV] [varchar](20) NULL,
	[MaNCC] [varchar](20) NULL,
	[SoHoaDon] [varchar](50) NULL,
	[GhiChu] [nvarchar](255) NULL,
	[MaNguoiDuyet] [varchar](20) NULL,
	FOREIGN KEY([MaNCC]) REFERENCES [dbo].[NhaCungCap] ([MaNCC]),
	FOREIGN KEY([MaNguoiDuyet]) REFERENCES [dbo].[NhanVien] ([MaNV]),
	FOREIGN KEY([MaNV]) REFERENCES [dbo].[NhanVien] ([MaNV])
);
GO

CREATE NONCLUSTERED INDEX [IDX_PhieuNhapKho_NgayLap] ON [dbo].[PhieuNhapKho]([NgayLap] ASC);
GO

-- Bảng PhieuXuatKho
CREATE TABLE [dbo].[PhieuXuatKho](
	[MaPhieu] [varchar](20) NOT NULL PRIMARY KEY,
	[NgayLap] [date] NULL,
	[MaNV] [varchar](20) NULL,
	[MaDeNghi] [varchar](20) NULL,
	[BoPhanNhan] [nvarchar](100) NULL,
	[MucDich] [nvarchar](255) NULL,
	[MaNguoiDuyet] [varchar](20) NULL,
	FOREIGN KEY([MaDeNghi]) REFERENCES [dbo].[PhieuDeNghiXuat] ([MaDeNghi]),
	FOREIGN KEY([MaNguoiDuyet]) REFERENCES [dbo].[NhanVien] ([MaNV]),
	FOREIGN KEY([MaNV]) REFERENCES [dbo].[NhanVien] ([MaNV])
);
GO

CREATE NONCLUSTERED INDEX [IDX_PhieuXuatKho_NgayLap] ON [dbo].[PhieuXuatKho]([NgayLap] ASC);
GO

-- Bảng PhieuKiemKeHangHoa
CREATE TABLE [dbo].[PhieuKiemKeHangHoa](
	[MaKiemKe] [varchar](20) NOT NULL PRIMARY KEY,
	[NgayKiem] [date] NULL,
	[MaNV] [varchar](20) NULL,
	[TrangThai] [nvarchar](50) NULL DEFAULT (N'Chờ duyệt'),
	[LyDo] [nvarchar](255) NULL,
	FOREIGN KEY([MaNV]) REFERENCES [dbo].[NhanVien] ([MaNV])
);
GO

-- Bảng ChiTietPhieuDeNghiXuat
CREATE TABLE [dbo].[ChiTietPhieuDeNghiXuat](
	[MaDeNghi] [varchar](20) NOT NULL,
	[MaHang] [varchar](20) NOT NULL,
	[SoLuongYeuCau] [int] NULL,
	PRIMARY KEY ([MaDeNghi], [MaHang]),
	FOREIGN KEY([MaDeNghi]) REFERENCES [dbo].[PhieuDeNghiXuat] ([MaDeNghi]),
	FOREIGN KEY([MaHang]) REFERENCES [dbo].[HangHoa] ([MaHang])
);
GO

-- Bảng ChiTietPhieuKiemKe
CREATE TABLE [dbo].[ChiTietPhieuKiemKe](
	[MaKiemKe] [varchar](20) NOT NULL,
	[MaHang] [varchar](20) NOT NULL,
	[SoLuongThucTe] [int] NULL,
	[ChenhLech] [int] NULL,
	[NguyenNhan] [nvarchar](255) NULL,
	PRIMARY KEY ([MaKiemKe], [MaHang]),
	FOREIGN KEY([MaHang]) REFERENCES [dbo].[HangHoa] ([MaHang]),
	FOREIGN KEY([MaKiemKe]) REFERENCES [dbo].[PhieuKiemKeHangHoa] ([MaKiemKe])
);
GO

-- Bảng ChiTietPhieuNhapKho
CREATE TABLE [dbo].[ChiTietPhieuNhapKho](
	[MaPhieu] [varchar](20) NOT NULL,
	[MaHang] [varchar](20) NOT NULL,
	[SoLuong] [int] NULL,
	[DonGia] [decimal](18, 2) NULL,
	PRIMARY KEY ([MaPhieu], [MaHang]),
	FOREIGN KEY([MaHang]) REFERENCES [dbo].[HangHoa] ([MaHang]),
	FOREIGN KEY([MaPhieu]) REFERENCES [dbo].[PhieuNhapKho] ([MaPhieu])
);
GO

CREATE NONCLUSTERED INDEX [IDX_CTNhap_MaHang] ON [dbo].[ChiTietPhieuNhapKho]([MaHang] ASC);
GO

-- Bảng ChiTietPhieuXuatKho
CREATE TABLE [dbo].[ChiTietPhieuXuatKho](
	[MaPhieu] [varchar](20) NOT NULL,
	[MaHang] [varchar](20) NOT NULL,
	[SoLuong] [int] NULL,
	[DonGia] [decimal](18, 2) NULL,
	PRIMARY KEY ([MaPhieu], [MaHang]),
	FOREIGN KEY([MaHang]) REFERENCES [dbo].[HangHoa] ([MaHang]),
	FOREIGN KEY([MaPhieu]) REFERENCES [dbo].[PhieuXuatKho] ([MaPhieu])
);
GO

CREATE NONCLUSTERED INDEX [IDX_CTXuat_MaHang] ON [dbo].[ChiTietPhieuXuatKho]([MaHang] ASC);
GO

-- Bảng CongNo
CREATE TABLE [dbo].[CongNo](
	[MaCongNo] [varchar](20) NOT NULL PRIMARY KEY,
	[MaPhieu] [varchar](20) NULL,
	[LoaiPhieu] [nvarchar](20) NULL,
	[SoTienPhaiTra] [decimal](18, 2) NULL,
	[SoTienDaTra] [decimal](18, 2) NULL DEFAULT ((0)),
	[TrangThai] [nvarchar](50) NULL DEFAULT (N'Chưa thanh toán')
);
GO

-- Bảng KetQuaDuBao
CREATE TABLE [dbo].[KetQuaDuBao](
	[MaDuBao] [varchar](20) NOT NULL PRIMARY KEY,
	[DoChinhXac] [decimal](5, 2) NULL,
	[NgayDuBao] [date] NULL,
	[SoLuongDuBao] [int] NULL,
	[MaHang] [varchar](20) NULL,
	FOREIGN KEY([MaHang]) REFERENCES [dbo].[HangHoa] ([MaHang])
);
GO

-- Bảng LichSuTonKho
CREATE TABLE [dbo].[LichSuTonKho](
	[MaHang] [varchar](20) NULL,
	[NgayGhiNhan] [datetime] NULL,
	[LoaiBienDong] [nvarchar](50) NULL,
	[SoLuongThayDoi] [int] NULL,
	[MaChungTu] [varchar](20) NULL,
	[GhiChu] [nvarchar](255) NULL,
	FOREIGN KEY([MaHang]) REFERENCES [dbo].[HangHoa] ([MaHang])
);
GO

CREATE NONCLUSTERED INDEX [IDX_LichSuTonKho_MaHang_Ngay] ON [dbo].[LichSuTonKho]([MaHang] ASC, [NgayGhiNhan] ASC);
GO

PRINT N'Đã tạo xong tất cả các bảng';
GO

-- =============================================
-- THÊM DỮ LIỆU MẪU
-- =============================================

-- Insert BoPhan
INSERT [dbo].[BoPhan] VALUES (N'BP01', N'Phòng Quản lý Kho', N'Quản lý nhập xuất tồn');
INSERT [dbo].[BoPhan] VALUES (N'BP02', N'Phòng Mua Hàng', N'Liên hệ nhà cung cấp');
INSERT [dbo].[BoPhan] VALUES (N'BP03', N'Phòng Kinh Doanh', N'Bán hàng và phân phối');
INSERT [dbo].[BoPhan] VALUES (N'BP04', N'Phòng Kế Toán', N'Thống kê và báo cáo');
GO

-- Insert LoaiHangHoa
INSERT [dbo].[LoaiHangHoa] VALUES (N'LH01', N'Chăm sóc cá nhân');
INSERT [dbo].[LoaiHangHoa] VALUES (N'LH02', N'Thực phẩm dinh dưỡng');
INSERT [dbo].[LoaiHangHoa] VALUES (N'LH03', N'Gia vị - Thực phẩm');
INSERT [dbo].[LoaiHangHoa] VALUES (N'LH04', N'Chất tẩy rửa');
GO

-- Insert ViTriKho
INSERT [dbo].[ViTriKho] VALUES (N'K1-T1', N'Kệ 1 - Tầng 1 (Khu A)', N'Đang sử dụng');
INSERT [dbo].[ViTriKho] VALUES (N'K1-T2', N'Kệ 1 - Tầng 2 (Khu A)', N'Đang sử dụng');
INSERT [dbo].[ViTriKho] VALUES (N'K2-T1', N'Kệ 2 - Tầng 1 (Khu B)', N'Đang sử dụng');
GO

-- Insert NhaCungCap
INSERT [dbo].[NhaCungCap] VALUES (N'NCC01', N'Unilever Việt Nam', N'Quận 7, TP.HCM', N'02838123456', N'contact@unilever.com');
INSERT [dbo].[NhaCungCap] VALUES (N'NCC02', N'Masan Consumer', N'Quận 1, TP.HCM', N'02838123457', N'sales@masan.com');
INSERT [dbo].[NhaCungCap] VALUES (N'NCC03', N'Abbott Việt Nam', N'Quận 1, TP.HCM', N'02838123458', N'info@abbott.com');
GO

-- Insert NhanVien
INSERT [dbo].[NhanVien] VALUES (N'NV01', N'Nguyễn Văn Thủ', N'0901234567', N'Bình Thạnh, TP.HCM', CAST(N'1990-05-15' AS Date), N'BP01');
INSERT [dbo].[NhanVien] VALUES (N'NV02', N'Trần Thị Trưởng', N'0901234568', N'Gò Vấp, TP.HCM', CAST(N'1985-08-20' AS Date), N'BP01');
INSERT [dbo].[NhanVien] VALUES (N'NV03', N'Lê Mua Hàng', N'0901234569', N'Quận 2, TP.HCM', CAST(N'1992-10-10' AS Date), N'BP02');
INSERT [dbo].[NhanVien] VALUES (N'NV04', N'Phạm Kinh Doanh', N'0901234570', N'Quận 3, TP.HCM', CAST(N'1995-01-01' AS Date), N'BP03');
INSERT [dbo].[NhanVien] VALUES (N'NV05', N'AdMin', N'0901234571', N'Quận 3, TP.HCM', CAST(N'1995-01-01' AS Date), N'BP03');
INSERT [dbo].[NhanVien] VALUES (N'NV06', N'Trần Văn Tùng', N'0901234572', N'Bình Thạnh, TP.HCM', CAST(N'1990-05-15' AS Date), N'BP04');
INSERT [dbo].[NhanVien] VALUES (N'NV07', N'Trần Văn Bê', N'0901234573', N'Bình Thạnh, TP.HCM', CAST(N'1990-05-15' AS Date), N'BP04');
GO

-- Insert HangHoa
INSERT [dbo].[HangHoa] VALUES (N'SP01', N'Dầu gội Clear Men', N'Chai', 150, N'650g/chai', 50, N'LH01', N'K1-T1');
INSERT [dbo].[HangHoa] VALUES (N'SP02', N'Nước mắm Nam Ngư', N'Chai', 80, N'500ml/chai', 100, N'LH03', N'K1-T2');
INSERT [dbo].[HangHoa] VALUES (N'SP03', N'Sữa bột Ensure Gold', N'Hộp', 310, N'850g/hộp', 30, N'LH02', N'K2-T1');
INSERT [dbo].[HangHoa] VALUES (N'SP04', N'Bột giặt OMO', N'Bịch', 500, N'4.5kg/bịch', 150, N'LH04', N'K2-T1');
GO

-- Insert TaiKhoan
GO

-- Insert PhieuDeNghiXuat
INSERT [dbo].[PhieuDeNghiXuat] VALUES (N'DNX001', CAST(N'2026-05-20' AS Date), N'Xuất hàng giao đại lý Quận Bình Thạnh', N'Đã duyệt');
INSERT [dbo].[PhieuDeNghiXuat] VALUES (N'DNX002', CAST(N'2026-05-22' AS Date), N'Xuất hàng khuyến mãi siêu thị', N'Chờ duyệt');
GO

-- Insert PhieuNhapKho
INSERT [dbo].[PhieuNhapKho] VALUES (N'PN001', CAST(N'2026-05-01' AS Date), N'NV01', N'NCC01', N'HD_UNI_001', N'Nhập đủ hàng theo hóa đơn', N'NV02');
INSERT [dbo].[PhieuNhapKho] VALUES (N'PN002', CAST(N'2026-05-05' AS Date), N'NV01', N'NCC02', N'HD_MASAN_02', N'Nhập nước mắm', N'NV02');
GO

-- Insert ChiTietPhieuNhapKho
INSERT [dbo].[ChiTietPhieuNhapKho] VALUES (N'PN001', N'SP01', 100, CAST(120000.00 AS Decimal(18, 2)));
INSERT [dbo].[ChiTietPhieuNhapKho] VALUES (N'PN001', N'SP04', 300, CAST(150000.00 AS Decimal(18, 2)));
INSERT [dbo].[ChiTietPhieuNhapKho] VALUES (N'PN002', N'SP02', 80, CAST(25000.00 AS Decimal(18, 2)));
GO

-- Insert PhieuXuatKho
INSERT [dbo].[PhieuXuatKho] VALUES (N'PX001', CAST(N'2026-05-21' AS Date), N'NV01', N'DNX001', N'Phòng Kinh Doanh', N'Giao đại lý Bình Thạnh', N'NV02');
GO

-- Insert ChiTietPhieuXuatKho
INSERT [dbo].[ChiTietPhieuXuatKho] VALUES (N'PX001', N'SP01', 20, CAST(150000.00 AS Decimal(18, 2)));
INSERT [dbo].[ChiTietPhieuXuatKho] VALUES (N'PX001', N'SP04', 50, CAST(180000.00 AS Decimal(18, 2)));
GO

-- Insert CongNo
INSERT [dbo].[CongNo] VALUES (N'CN_PN001', N'PN001', N'Nhập', CAST(57000000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), N'Chưa thanh toán');
INSERT [dbo].[CongNo] VALUES (N'CN_PN002', N'PN002', N'Nhập', CAST(2000000.00 AS Decimal(18, 2)), CAST(1000000.00 AS Decimal(18, 2)), N'Thanh toán một phần');
INSERT [dbo].[CongNo] VALUES (N'CN_PX001', N'PX001', N'Xuất', CAST(12000000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), N'Chưa thanh toán');
GO

PRINT N'Đã thêm xong dữ liệu mẫu';
GO

-- =============================================
-- TẠO STORED PROCEDURES
-- =============================================

-- SP: Báo cáo hàng tồn dưới mức tối thiểu
CREATE PROCEDURE [dbo].[SP_BaoCaoHangTonDuoiMucToiThieu]
AS
BEGIN
    SELECT
        h.MaHang AS 'Mã Hàng',
        h.TenHang AS 'Tên Hàng Hóa',
        h.DVT AS 'Đơn Vị Tính',
        h.HanMucTonToiThieu AS 'Hạn Mức Tối Thiểu',
        h.SoLuongTonKho AS 'Tồn Kho Hiện Tại',
        (h.HanMucTonToiThieu - h.SoLuongTonKho) AS 'Số Lượng Cần Bổ Sung'
    FROM HangHoa h
    WHERE h.SoLuongTonKho <= h.HanMucTonToiThieu
    ORDER BY [Số Lượng Cần Bổ Sung] DESC;
END;
GO

-- SP: Báo cáo Nhập - Xuất - Tồn theo kỳ
CREATE PROCEDURE [dbo].[SP_BaoCaoNhapXuatTonTheoKy]
    @TuNgay DATE,
    @DenNgay DATE
AS
BEGIN
    SELECT
        h.MaHang,
        h.TenHang,
        h.SoLuongTonKho AS 'Tồn Kho Cuối Kỳ',
        ISNULL(SUM(ctn.SoLuong), 0) AS 'Tổng Nhập Trong Kỳ',
        ISNULL(SUM(ctx.SoLuong), 0) AS 'Tổng Xuất Trong Kỳ'
    FROM HangHoa h
    LEFT JOIN ChiTietPhieuNhapKho ctn ON h.MaHang = ctn.MaHang
    LEFT JOIN PhieuNhapKho pn ON ctn.MaPhieu = pn.MaPhieu AND pn.NgayLap BETWEEN @TuNgay AND @DenNgay
    LEFT JOIN ChiTietPhieuXuatKho ctx ON h.MaHang = ctx.MaHang
    LEFT JOIN PhieuXuatKho px ON ctx.MaPhieu = px.MaPhieu AND px.NgayLap BETWEEN @TuNgay AND @DenNgay
    GROUP BY h.MaHang, h.TenHang, h.SoLuongTonKho;
END;
GO

-- SP: Duyệt phiếu nhập kho
CREATE PROCEDURE [dbo].[SP_DuyetPhieuNhapKho]
    @MaPhieu VARCHAR(20),
    @MaTruongKho VARCHAR(20)
AS
BEGIN
    UPDATE PhieuNhapKho
    SET MaNguoiDuyet = @MaTruongKho
    WHERE MaPhieu = @MaPhieu;

    IF NOT EXISTS (SELECT 1 FROM CongNo WHERE MaPhieu = @MaPhieu)
    BEGIN
        INSERT INTO CongNo (MaCongNo, MaPhieu, LoaiPhieu, SoTienPhaiTra)
        SELECT 'CN_' + @MaPhieu, @MaPhieu, N'Nhập', SUM(SoLuong * DonGia)
        FROM ChiTietPhieuNhapKho WHERE MaPhieu = @MaPhieu
        GROUP BY MaPhieu;
    END
END;
GO

-- SP: Duyệt phiếu xuất kho
CREATE PROCEDURE [dbo].[SP_DuyetPhieuXuatKho]
    @MaPhieu VARCHAR(20),
    @MaTruongKho VARCHAR(20)
AS
BEGIN
    UPDATE PhieuXuatKho
    SET MaNguoiDuyet = @MaTruongKho
    WHERE MaPhieu = @MaPhieu;

    UPDATE PhieuDeNghiXuat
    SET TrangThai = N'Đã hoàn tất'
    WHERE MaDeNghi = (SELECT MaDeNghi FROM PhieuXuatKho WHERE MaPhieu = @MaPhieu);

    IF NOT EXISTS (SELECT 1 FROM CongNo WHERE MaPhieu = @MaPhieu)
    BEGIN
        INSERT INTO CongNo (MaCongNo, MaPhieu, LoaiPhieu, SoTienPhaiTra)
        SELECT 'CN_' + @MaPhieu, @MaPhieu, N'Xuất', SUM(SoLuong * DonGia)
        FROM ChiTietPhieuXuatKho WHERE MaPhieu = @MaPhieu
        GROUP BY MaPhieu;
    END
END;
GO

PRINT N'Đã tạo xong stored procedures';
GO

PRINT N'========================================';
PRINT N'KHỞI TẠO DATABASE THÀNH CÔNG!';
PRINT N'Database: QuanLyKhoHang';
PRINT N'========================================';
GO
