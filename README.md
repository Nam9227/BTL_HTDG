# Hệ thống Đấu giá Trực tuyến (Bidding System) 🔨

Hệ thống Đấu giá trực tuyến (Bidding System) được xây dựng theo mô hình **Client-Server** qua giao thức TCP/IP (Java Sockets). Ứng dụng cung cấp một nền tảng thời gian thực cho phép người dùng tham gia đấu giá các sản phẩm, quản lý ví điện tử (nạp/rút tiền), đăng bán sản phẩm và nhận thông báo. Bên cạnh đó, hệ thống cung cấp một phân hệ Quản trị (Admin) toàn diện để kiểm duyệt và giám sát.

---

## 1. Mô tả bài toán và phạm vi hệ thống
- **Bài toán:** Giải quyết nhu cầu mua bán và đấu giá tài sản/sản phẩm trên môi trường mạng một cách công bằng, minh bạch và theo thời gian thực (Real-time).
- **Phạm vi (Scope):**
  - **Người dùng (Bidder/Seller):** Có thể mở gian hàng đăng bán sản phẩm, nạp tiền vào ví, đặt giá (bid) đua top với người khác và nhận sản phẩm khi thắng cuộc. Dòng tiền được tính toán và luân chuyển tự động.
  - **Quản trị viên (Admin):** Quản lý toàn bộ vòng đời của ứng dụng bao gồm kiểm duyệt người dùng, duyệt sản phẩm tải lên, duyệt các yêu cầu nạp/rút tiền thật và giám sát luồng đấu giá.

## 2. Công nghệ sử dụng, môi trường chạy & Yêu cầu cài đặt
### Công nghệ & Thư viện
- **Ngôn ngữ:** Java (JDK 17 trở lên, tương thích tốt với JDK 21/25).
- **Kiến trúc mạng:** TCP/IP Sockets (Giao tiếp luồng đối tượng - `ObjectOutputStream` / `ObjectInputStream`).
- **Giao diện (GUI):** JavaFX, FXML, CSS.
- **Cơ sở dữ liệu:** MySQL 8.0+ (Tương tác qua JDBC).
- **Quản lý dự án:** Maven (Multi-module Architecture).
- **Ghi log:** SLF4J, Logback (Quản lý log chuẩn doanh nghiệp).

### Yêu cầu cài đặt
Để chạy được hệ thống, máy tính của bạn cần được cài đặt sẵn:
1. **JDK 17+** (Cần thiết lập biến môi trường `JAVA_HOME`).
2. **Apache Maven 3.8+** (Thiết lập biến môi trường `M2_HOME` hoặc có sẵn lệnh `mvn` trong Terminal).
3. **MySQL Server** (Khởi tạo Database bằng file `mydb.sql` đính kèm trong thư mục gốc).

---

## 3. Cấu trúc thư mục (Multi-module Maven)
Dự án được chia thành 3 module chính nhằm tách biệt logic và tái sử dụng code:
- 📁 **`bidding-common`**: Chứa các Model dữ liệu (User, AuctionItem, Transaction, Notification...) và các đối tượng gói tin (Requests / Responses) dùng để trao đổi giữa Client và Server.
- 📁 **`bidding-server`**: Bộ não của hệ thống. Chứa các Service xử lý đa luồng, kết nối Database (DAO), phân luồng luân chuyển tiền tệ, và bộ lập lịch (Scheduler) đóng/mở phiên đấu giá tự động.
- 📁 **`bidding-client`**: Phân hệ giao diện đồ họa JavaFX. Tương tác với người dùng, kết nối đến Server thông qua `ClientSocket` và cập nhật giao diện theo thời gian thực.

---

## 4. Hướng dẫn cài đặt & Câu lệnh khởi chạy
Dưới đây là các câu lệnh có thể chạy đa nền tảng (**Windows / Linux / MacOS**) thông qua Terminal (hoặc PowerShell/Command Prompt).

### Bước 1: Build toàn bộ dự án
Mở Terminal tại thư mục gốc của dự án (nơi chứa file `pom.xml` tổng) và chạy lệnh:
```bash
mvn clean install -DskipTests
```

### Bước 2: Khởi chạy Server
Phải **chạy Server trước** để mở cổng lắng nghe (Port: `27915`).
```bash
mvn exec:java -pl bidding-server -Dexec.mainClass="com.uet.server.network.ServerMain"
```
*Lưu ý: Đảm bảo cấu hình Database (URL, username, password) trong file `DBConnection.java` của `bidding-server` trùng khớp với MySQL của máy bạn.*

### Bước 3: Khởi chạy Client
Mở một cửa sổ Terminal mới (giữ nguyên cửa sổ Server đang chạy), thực thi lệnh sau để mở giao diện:
```bash
mvn javafx:run -pl bidding-client
```
*(Bạn có thể mở nhiều cửa sổ Client cùng lúc để test tính năng đấu giá cạnh tranh)*

---

## 5. Danh sách chức năng đã hoàn thành
✅ **Phân hệ Client / Người dùng (User)**
- [x] Đăng nhập, Đăng ký, Quên mật khẩu.
- [x] Chỉnh sửa hồ sơ cá nhân, tải lên ảnh đại diện (Avatar).
- [x] Quản lý Ví (Wallet): Gửi yêu cầu Nạp tiền / Rút tiền.
- [x] Đăng bán sản phẩm (Đính kèm ảnh, mô tả, danh mục, giá khởi điểm).
- [x] Đấu giá thời gian thực: Đặt giá (Bid), tự động trừ/cộng tiền khi kết thúc.
- [x] Nhận thông báo (Notifications) theo thời gian thực khi có sự kiện (Thắng đấu giá, giao dịch thành công...).

✅ **Phân hệ Quản trị viên (Admin)**
- [x] Dashboard thống kê (Tổng người dùng, tổng sản phẩm, số phiên đang chạy).
- [x] Quản lý Người dùng: Khóa/Mở khóa tài khoản, Xóa tài khoản, Xem chi tiết lịch sử.
- [x] Xét duyệt Sản phẩm: Duyệt để lên sàn hoặc Từ chối sản phẩm vi phạm.
- [x] Quản lý Giao dịch: Duyệt các yêu cầu Nạp/Rút tiền từ người dùng.
- [x] Quản lý Đấu giá: Xem tiến trình đấu giá, ép kết thúc sớm (Force End) một phiên bất kỳ.

---

## 6. Báo cáo và Video Demo
- 📄 **Báo cáo chi tiết (PDF):** [Tải xuống / Xem Báo Cáo Tại Đây](#) *(Thêm link tại đây)*
- 🎥 **Video Demo Hệ thống:** [Xem Video Demo Trên YouTube/Drive](#) *(Thêm link tại đây)*

---
*Dự án Bài Tập Lớn - Lập trình Mạng / Hệ thống đa tầng.*
