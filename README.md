# Hệ Thống Đấu Giá Trực Tuyến (Online Bidding System)

## 1. Mô tả ngắn gọn bài toán và phạm vi hệ thống
Hệ thống là một ứng dụng client-server cho phép người dùng tham gia đấu giá các sản phẩm trực tuyến theo thời gian thực. 
- **Người dùng (Bidder/Seller):** Có thể đăng ký bán sản phẩm, nạp/rút tiền vào ví ảo, tham gia đặt giá (bid) cho các sản phẩm đang mở bán, và nhận thông báo kết quả.
- **Quản trị viên (Admin):** Quản lý người dùng, phê duyệt các yêu cầu nạp/rút tiền, duyệt các phiên đấu giá mới và có quyền ép đóng phiên đấu giá nếu có vi phạm.

## 2. Công nghệ sử dụng, môi trường chạy và yêu cầu cài đặt
- **Ngôn ngữ:** Java 17+
- **Giao diện Client:** JavaFX
- **Giao tiếp mạng:** TCP Socket (Real-time hai chiều)
- **Cơ sở dữ liệu:** MySQL 8.0+ & JDBC
- **Công cụ Build:** Maven
- **Môi trường:** Đa nền tảng (Windows, Linux, macOS).

**Yêu cầu cài đặt:**
1. Cài đặt JDK 17 trở lên.
2. Cài đặt Maven.
3. Cài đặt MySQL Server, tạo database `auction_system` và chạy script SQL (nếu có) để tạo bảng.
4. Cập nhật thông tin kết nối CSDL trong file `bidding-server/config/db.properties`.

## 3. Cấu trúc thư mục / module chính
Hệ thống được chia thành 3 module Maven chính:
- `bidding-common`: Chứa các model, DTO, và các class cấu trúc gói tin (Request/Response) dùng chung cho cả Client và Server.
- `bidding-server`: Chứa logic xử lý nghiệp vụ, quản lý database (DAO), quản lý Socket connection (ClientManager, ClientHandler), và các luồng lập lịch tự động (Scheduler).
- `bidding-client`: Chứa giao diện người dùng JavaFX và logic kết nối socket tới server.

## 4. Hướng dẫn chạy chương trình (Dòng lệnh)

### Trên Windows:
```cmd
# Build toàn bộ project
mvn clean install

# Chạy Server
cd bidding-server
mvn exec:java -Dexec.mainClass="com.uet.server.ServerApp"

# Mở một terminal khác, chạy Client
cd bidding-client
mvn javafx:run
```

### Trên Linux / macOS:
```bash
# Build toàn bộ project
mvn clean install

# Chạy Server
cd bidding-server
mvn exec:java -Dexec.mainClass="com.uet.server.ServerApp"

# Mở một terminal khác, chạy Client
cd bidding-client
mvn javafx:run
```

## 5. Danh sách chức năng đã hoàn thành
- [x] Đăng ký, Đăng nhập và xác thực phân quyền (Admin, Bidder, Seller).
- [x] Giao tiếp Client-Server qua TCP Socket (đa luồng).
- [x] Quản lý thông tin tài khoản và đổi mật khẩu.
- [x] Quản lý ví tiền (Nạp/Rút tiền) có sự phê duyệt của Admin.
- [x] Đăng bán sản phẩm đấu giá (Seller).
- [x] Quản lý và phê duyệt sản phẩm đấu giá (Admin).
- [x] Hệ thống lập lịch tự động: Mở phiên khi đến giờ, đóng phiên và tự động thanh toán khi hết giờ.
- [x] Đặt giá (Bid) realtime: Trực tiếp cập nhật giá lên UI của mọi client đang xem bằng cơ chế Broadcast.
- [x] Lịch sử đặt giá và Lịch sử giao dịch dòng tiền.
- [x] Hệ thống Thông báo (Notification) realtime đẩy về cho Client.
- [x] Dashboard thống kê cho Admin.

## 6. Link báo cáo PDF và Video Demo
- **Báo cáo PDF:** [Link Google Drive / đính kèm]
- **Video Demo:** [Link Youtube / đính kèm]
