# Quản lý kho hàng — Nhóm 19

Đồ án NoSQL, HUIT. Ứng dụng Java Web sử dụng SQL Server cho nghiệp vụ kho, MongoDB cho hồ sơ mở rộng và nhật ký, Redis để cache, Elasticsearch để tìm kiếm hàng hóa.

## Cấu trúc

- `src/`: mã nguồn Java, JSP, cấu hình và kiểm thử.
- `mongodb-init/`: dữ liệu khởi tạo MongoDB.
- `pom.xml`: thư viện và cấu hình build Maven.
- `docker-compose.yml`: MongoDB, Redis và Elasticsearch cho môi trường local.
- `quanlykhohang.sql`: script tạo SQL Server và dữ liệu mẫu.

## Chạy dự án

Yêu cầu JDK 11+, Maven, Docker Desktop và SQL Server.

1. Khởi tạo SQL Server bằng `quanlykhohang.sql`. **Script xóa database `QuanLyKhoHang` nếu đã tồn tại**, chỉ chạy trên database thử nghiệm.
2. Đặt biến môi trường `SQLSERVER_URL`, `SQLSERVER_USERNAME`, `SQLSERVER_PASSWORD` theo SQL Server của bạn. Hoặc tạo file riêng `config.properties` trong thư mục chạy máy chủ Java, với các khóa `sqlserver.url`, `sqlserver.username`, `sqlserver.password`. File này được Git bỏ qua. Có thể chỉ định đường dẫn tuyệt đối bằng JVM `-Dwarehouse.config=...`. Biến môi trường được ưu tiên hơn file cấu hình.
   Bản SQL công khai không chứa tài khoản đăng nhập mẫu; tạo tài khoản của bạn trong bảng `TaiKhoan` sau khi khởi tạo dữ liệu, liên kết với nhân viên và vai trò phù hợp.
3. Khởi động các dịch vụ và build:

```sh
docker compose up -d
docker compose ps
mvn clean package
```

MongoDB tự chạy script trong `mongodb-init/` khi volume được tạo lần đầu. Elasticsearch tự lấy dữ liệu từ SQL Server ở lần tìm kiếm đầu tiên.

Deploy `target/QuanLyKhoHang-1.0-SNAPSHOT.war` lên Tomcat 9, truy cập:

```text
http://localhost:8080/QuanLyKhoHang-1.0-SNAPSHOT/
```

## Cấu hình NoSQL

Thiết lập biến môi trường trước khi chạy máy chủ Java nếu cần đổi giá trị mặc định:

| Biến | Mặc định |
|---|---|
| `MONGODB_URI` | `mongodb://localhost:27017` |
| `MONGODB_DATABASE` | `quanlykhohang_nosql` |
| `REDIS_HOST` | `localhost` |
| `REDIS_PORT` | `6379` |
| `REDIS_PASSWORD` | Không có |
| `ELASTICSEARCH_URL` | `http://localhost:9200` |
| `ELASTICSEARCH_INDEX` | `quanlykhohang_hanghoa` |
| `ELASTICSEARCH_ENABLED` | `true` |
| `ELASTICSEARCH_TIMEOUT_MS` | `3000` |

Elasticsearch hỗ trợ thêm `ELASTICSEARCH_API_KEY` hoặc `ELASTICSEARCH_USERNAME` và `ELASTICSEARCH_PASSWORD`. Docker Compose hiện cấu hình cho phát triển local.

Tìm kiếm hỗ trợ tiếng Việt không dấu và lỗi gõ nhẹ. Sau khi thay đổi hàng hóa hoặc tồn kho, lần tìm tiếp theo sẽ đồng bộ lại chỉ mục; ngoài ra đồng bộ lại ở lần tìm sau mỗi 60 giây. Cơ chế snapshot hiện phù hợp dự án nhỏ chạy một instance Java. Khi Elasticsearch lỗi, tìm kiếm chuyển sang SQL/Redis.

## Kiểm thử

```sh
mvn test
# Kiểm thử tích hợp khi Elasticsearch đang chạy:
mvn -Delasticsearch.integration=true test
```

`target/` được Maven tạo lại khi build và không đưa lên Git.
