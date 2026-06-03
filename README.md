# Hệ Thống Đấu Giá Trực Tuyến (Bidding System)

## 1. Mô tả bài toán và phạm vi hệ thống
Dự án **Hệ Thống Đấu Giá Trực Tuyến (BTL_HTDG)** là một ứng dụng theo kiến trúc **Client-Server** mô phỏng một sàn giao dịch đấu giá thời gian thực.
Hệ thống cho phép người dùng đăng ký tài khoản với các vai trò khác nhau (Người đấu giá - Bidder, Người bán - Seller, Quản trị viên - Admin) để tham gia vào quá trình đăng bán sản phẩm, theo dõi và đặt giá (bidding) trực tiếp với độ trễ tối thiểu thông qua kết nối Socket.

## 2. Công nghệ sử dụng và Yêu cầu môi trường
*   **Ngôn ngữ lập trình:** Java (Yêu cầu **JDK 25** trở lên).
*   **Giao diện đồ họa (GUI):** JavaFX.
*   **Giao tiếp mạng:** TCP Socket (Real-time).
*   **Cơ sở dữ liệu:** MySQL 8.0+.
*   **Công cụ quản lý dự án & Build:** Apache Maven.
*   **Môi trường chạy:** Hỗ trợ đa nền tảng (Windows, Linux, macOS).

## 3. Cấu trúc thư mục (Modules chính)
Hệ thống được chia thành 3 module Maven riêng biệt để đảm bảo tính đóng gói và dễ bảo trì:
*   `bidding-common/`: Chứa các cấu trúc dữ liệu dùng chung (Models: *User, AuctionItem, BidRecord*), các định dạng gói tin mạng (Request/Response) hỗ trợ việc serialize/deserialize giữa Client và Server.
*   `bidding-server/`: Chứa logic nghiệp vụ máy chủ, quản lý đa luồng (Multi-threading) xử lý kết nối từ nhiều Client, tương tác với cơ sở dữ liệu MySQL (qua DAO pattern), và phân phối dữ liệu (Broadcast) tới các Client theo thời gian thực.
*   `bidding-client/`: Ứng dụng Desktop dành cho người dùng cuối sử dụng JavaFX, thực hiện gửi/nhận gói tin mạng từ Server và render giao diện cập nhật trạng thái đấu giá.

## 4. Hướng dẫn khởi chạy hệ thống (Đa nền tảng)

> **Lưu ý quan trọng:** Hệ thống bao gồm Server và Cơ sở dữ liệu đã được triển khai sẵn trên máy chủ ảo (VPS). Do đó, bạn **không cần** phải thiết lập hay khởi chạy Server. Chỉ cần biên dịch và chạy ứng dụng Client theo hướng dẫn dưới đây để kết nối vào hệ thống.

### Bước 4.1. Biên dịch dự án
Mở Terminal/Command Prompt tại thư mục gốc của project (nơi chứa file `pom.xml` tổng) và chạy lệnh sau để tải các thư viện và biên dịch mã nguồn:
```bash
# Trên Windows
mvnw.cmd clean compile

# Trên Linux/macOS
chmod +x mvnw
./mvnw clean compile
```

### Bước 4.2. Chạy Client
Sau khi lệnh biên dịch chạy xong và báo SUCCESS, chạy tiếp lệnh sau để mở giao diện người dùng:
```bash
# Trên Windows
mvnw.cmd exec:java -pl bidding-client "-Dexec.mainClass=com.uet.client.Launcher"

# Trên Linux/macOS
./mvnw exec:java -pl bidding-client -Dexec.mainClass="com.uet.client.Launcher"
```
*(Bạn có thể mở thêm nhiều Terminal và chạy lại lệnh này để mở nhiều Client cùng lúc, phục vụ việc kiểm thử đấu giá với nhiều tài khoản).*

## 5. Danh sách chức năng đã hoàn thành
- [x] Đăng nhập, đăng ký, cấp quyền người dùng (Admin, Seller, Bidder).
- [x] Nạp tiền, rút tiền, quản lý số dư ví cá nhân.
- [x] Đăng bán sản phẩm (upload hình ảnh, set giá khởi điểm, thời gian kết thúc).
- [x] Quản lý duyệt/từ chối sản phẩm đấu giá (Admin).
- [x] Tham gia phòng đấu giá thời gian thực (Cập nhật giá và lịch sử tức thời).
- [x] Đặt giá thủ công & Đặt giá tự động (Auto-bid).
- [x] Hệ thống thông báo đẩy (Notifications) khi bị vượt giá hoặc chiến thắng.
- [x] Bảng xếp hạng, lịch sử đặt giá minh bạch.

## 6. Tài liệu tham khảo & Demo
*   📄 **[Link Báo Cáo Chi Tiết (PDF)](#)** *(Cập nhật link báo cáo tại đây)*
*   🎥 **[Link Video Demo Hệ Thống](#)** *(Cập nhật link video YouTube/Drive tại đây)*
