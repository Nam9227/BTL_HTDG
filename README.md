# Hệ Thống Đấu Giá Trực Tuyến (BTL_HTDG)

BTL_HTDG là hệ thống đấu giá trực tuyến được xây dựng theo mô hình Client-Server. Dự án gồm backend xử lý bằng Java TCP Socket đa luồng, client desktop JavaFX, cơ chế đấu giá realtime qua Socket và cơ sở dữ liệu MySQL.

## Mục lục
- [1. Giới thiệu bài toán](#1-giới-thiệu-bài-toán)
- [2. Phạm vi hệ thống](#2-phạm-vi-hệ-thống)
- [3. Nhóm người dùng chính](#3-nhóm-người-dùng-chính)
- [4. Công nghệ sử dụng](#4-công-nghệ-sử-dụng)
- [5. Yêu cầu môi trường](#5-yêu-cầu-môi-trường)
- [6. Cấu trúc thư mục dự án](#6-cấu-trúc-thư-mục-dự-án)
- [7. Hướng dẫn chạy chương trình](#7-hướng-dẫn-chạy-chương-trình)
- [8. Tài khoản demo](#8-tài-khoản-demo)
- [9. Luồng nghiệp vụ chính](#9-luồng-nghiệp-vụ-chính)
- [10. Danh sách chức năng đã hoàn thành](#10-danh-sách-chức-năng-đã-hoàn-thành)
- [11. Báo cáo và video demo](#11-báo-cáo-và-video-demo)

---

## 1. Giới thiệu bài toán
BTL_HTDG là hệ thống đấu giá trực tuyến cho phép người bán đăng sản phẩm, quản trị viên duyệt sản phẩm và tạo phiên đấu giá, còn người mua tham gia đặt giá theo thời gian thực.

Mục tiêu của hệ thống là mô phỏng đầy đủ một quy trình đấu giá cơ bản, bao gồm:
- Đăng ký và đăng nhập người dùng.
- Phân quyền theo vai trò (Admin, Seller, Bidder).
- Tạo và quản lý sản phẩm đấu giá.
- Duyệt sản phẩm trước khi đưa vào phiên đấu giá.
- Đặt giá thủ công và tự động (Auto-bid).
- Cập nhật giá realtime bằng kết nối TCP Socket.
- Quản lý ví cá nhân (nạp/rút tiền).
- Theo dõi người thắng cuộc sau khi phiên kết thúc.

## 2. Phạm vi hệ thống

### 2.1. Phạm vi hiện tại
Hệ thống hiện tập trung vào các nghiệp vụ cốt lõi của một nền tảng đấu giá:
- Xác thực người dùng và phân quyền theo vai trò.
- Quản lý listing sản phẩm của seller.
- Đặt giá và theo dõi lịch sử bid.
- Đấu giá realtime với độ trễ thấp thông qua Socket.
- Quản lý ví, số dư và lịch sử giao dịch.
- Bảng xếp hạng, lịch sử đặt giá minh bạch.

### 2.2. Những phần chưa nằm trong phạm vi hiện tại
- Thanh toán qua bên thứ ba (VNPay, Momo, Stripe).
- Quy trình vận chuyển sau khi đấu giá thành công.
- Ứng dụng web/mobile riêng biệt.

## 3. Nhóm người dùng chính
Hệ thống hỗ trợ 3 nhóm người dùng chính: Bidder, Seller và Admin.

### 3.1. Bidder
Bidder là người tham gia đấu giá. Các chức năng chính gồm:
- Xem danh sách phiên đấu giá.
- Tìm kiếm và xem chi tiết sản phẩm.
- Đặt giá thủ công hoặc bật auto-bid.
- Tham gia phòng đấu giá thời gian thực.
- Theo dõi lịch sử đấu giá cá nhân.
- Quản lý số dư ví cá nhân (Nạp tiền, rút tiền).

### 3.2. Seller
Seller là người bán sản phẩm. Các chức năng chính gồm:
- Tạo listing đăng bán sản phẩm.
- Upload hình ảnh, thiết lập giá khởi điểm và thời gian kết thúc.
- Theo dõi trạng thái duyệt sản phẩm (Chờ duyệt, đã duyệt, bị từ chối).

*Lưu ý: Tài khoản seller không được seed sẵn. Muốn test vai trò seller, bạn cần đăng ký mới từ màn hình Register.*

### 3.3. Admin
Admin là quản trị viên hệ thống. Các chức năng chính gồm:
- Duyệt hoặc từ chối listing sản phẩm do seller gửi lên.
- Quản lý vòng đời và trạng thái các phiên đấu giá.
- Quản lý dòng tiền và các giao dịch trên hệ thống.

## 4. Công nghệ sử dụng
| Thành phần | Công nghệ |
| --- | --- |
| **Ngôn ngữ** | Java (Yêu cầu JDK 25) |
| **Giao tiếp mạng (Realtime)** | TCP Socket truyền thống đa luồng |
| **Desktop Client** | JavaFX 21+ |
| **Cơ sở dữ liệu** | MySQL 8.0+ |
| **Công cụ Build** | Apache Maven |
| **Thiết kế** | Mô hình Client-Server / Truyền dữ liệu Object (Serializable) |

## 5. Yêu cầu môi trường
Trước khi chạy dự án, cần đảm bảo:
- **JDK 25** đã được cài đặt và thiết lập biến môi trường `JAVA_HOME`.
- **Apache Maven** (Dự án đã tích hợp sẵn Maven Wrapper nên không bắt buộc cài system-wide).
- Hệ điều hành Windows, Linux hoặc macOS có hỗ trợ giao diện đồ họa.

## 6. Cấu trúc thư mục dự án
Hệ thống được chia thành 3 module riêng biệt để đảm bảo tính đóng gói:
```text
BTL_HTDG/
|-- pom.xml                         # Cấu hình Maven tổng của toàn dự án
|-- bidding-common/                 # Module dùng chung
|   `-- src/main/java/com/uet/common/
|       |-- models/                 # Các thực thể: User, AuctionItem, BidRecord...
|       `-- network/                # Định dạng Request/Response giao tiếp mạng
|-- bidding-server/                 # Module Backend (Máy chủ)
|   `-- src/main/java/com/uet/server/
|       |-- network/                # Xử lý ServerSocket, đa luồng cho Client
|       |-- database/               # Kết nối CSDL MySQL (DAO pattern)
|       `-- service/                # Logic xử lý phiên đấu giá, Broadcast dữ liệu
`-- bidding-client/                 # Module Frontend (Ứng dụng người dùng)
    `-- src/main/java/com/uet/client/
        |-- ui/                     # JavaFX Controllers (Login, Dashboard, Auction...)
        |-- network/                # Gửi/nhận gói tin với Server
        `-- config/                 # Cấu hình IP/Port kết nối (client.properties)
```

## 7. Hướng dẫn chạy chương trình

> **LƯU Ý QUAN TRỌNG:** Hệ thống bao gồm **Server và Cơ sở dữ liệu MySQL đã được triển khai chạy 24/7 trên máy chủ ảo (VPS)**. Do đó, người dùng hoặc giảng viên kiểm thử **KHÔNG CẦN** khởi chạy Server hay thiết lập Database.
>
> Ứng dụng Client đã được cấu hình IP trỏ thẳng tới VPS. Bạn chỉ cần biên dịch và chạy **Client** theo các bước sau.

### Bước 1: Biên dịch dự án
Mở Terminal/Command Prompt tại thư mục gốc của project (nơi chứa file `pom.xml` tổng) và chạy lệnh:

**Trên Windows:**
```bash
mvnw.cmd clean compile
```

**Trên Linux / macOS:**
```bash
chmod +x mvnw
./mvnw clean compile
```

### Bước 2: Chạy Client Desktop
Sau khi lệnh biên dịch chạy xong và báo `BUILD SUCCESS`, chạy tiếp lệnh sau để mở giao diện:

**Trên Windows:**
```bash
mvnw.cmd exec:java -pl bidding-client "-Dexec.mainClass=com.uet.client.Launcher"
```

**Trên Linux / macOS:**
```bash
./mvnw exec:java -pl bidding-client -Dexec.mainClass="com.uet.client.Launcher"
```
*(Bạn có thể mở thêm nhiều cửa sổ Terminal và chạy lại lệnh này để mở nhiều Client cùng lúc, phục vụ việc kiểm thử đấu giá realtime với nhiều tài khoản).*

## 8. Tài khoản demo
Hệ thống seed sẵn một số tài khoản demo để bạn test nhanh:

| Vai trò | Username | Password |
| --- | --- | --- |
| **Admin** | `admin` | `123456` |
| **Bidder** | `bidder` | `123456` |

*Lưu ý:*
- Tài khoản Seller không được seed sẵn. Muốn test luồng của Seller, hãy dùng chức năng **Đăng ký (Register)** trên giao diện Client.
- Khi đăng nhập, hãy đảm bảo chọn đúng quyền tương ứng của tài khoản.

## 9. Luồng nghiệp vụ chính

### 9.1. Luồng đăng ký và đăng nhập
1. Người dùng mở ứng dụng Client.
2. Chọn đăng ký tài khoản mới (cấp quyền Seller/Bidder) hoặc đăng nhập bằng tài khoản có sẵn.
3. Client gửi request qua TCP Socket tới Server.
4. Server xác thực với MySQL, trả về phản hồi hợp lệ. Client điều hướng sang màn hình Dashboard.

### 9.2. Luồng Seller đăng sản phẩm
1. Seller đăng nhập và chọn tính năng tạo Listing.
2. Nhập thông tin, tải lên hình ảnh, đặt giá khởi điểm.
3. Sản phẩm được đưa vào trạng thái **Chờ duyệt** (Pending).

### 9.3. Luồng Admin kiểm duyệt
1. Admin đăng nhập, xem danh sách sản phẩm chờ duyệt.
2. Kiểm tra thông tin và ấn **Duyệt**.
3. Sản phẩm chính thức trở thành một Phiên đấu giá.

### 9.4. Luồng Bidder tham gia đấu giá (Realtime)
1. Bidder chọn một phiên đấu giá đang diễn ra và tham gia phòng.
2. Màn hình hiển thị giá hiện tại và lịch sử đấu giá.
3. Bidder nhập số tiền muốn đặt (hoặc chọn Auto-bid).
4. Server kiểm tra tính hợp lệ (tiền trong ví, mức giá tối thiểu).
5. Nếu hợp lệ, Server lưu dữ liệu và lập tức **Broadcast** mức giá mới tới tất cả Client đang ở trong phòng.
6. Giao diện các Client tự động cập nhật ngay lập tức (Realtime).

### 9.5. Luồng kết thúc phiên đấu giá
1. Thời gian đếm ngược kết thúc, phiên đấu giá đóng lại.
2. Hệ thống xác định Bidder trả giá cao nhất.
3. Trừ tiền ở ví Bidder, cộng tiền vào ví Seller, gửi thông báo chiến thắng.

## 10. Danh sách chức năng đã hoàn thành
- [x] Đăng nhập, đăng ký, cấp quyền người dùng (Admin, Seller, Bidder).
- [x] Quản lý ví cá nhân: Nạp tiền, rút tiền, theo dõi biến động số dư.
- [x] Đăng bán sản phẩm (upload hình ảnh, đặt giá khởi điểm, set thời gian).
- [x] Quản lý kiểm duyệt sản phẩm đấu giá (Admin).
- [x] Tham gia phòng đấu giá thời gian thực (Realtime updates qua TCP Socket).
- [x] Hỗ trợ đặt giá thủ công & Đặt giá tự động (Auto-bid).
- [x] Hệ thống thông báo (Notifications) khi bị vượt giá hoặc chiến thắng.
- [x] Bảng xếp hạng & lịch sử đặt giá minh bạch.

## 11. Báo cáo và video demo
*   📄 **[Link Báo Cáo Chi Tiết (PDF)](#)** *(Cập nhật link báo cáo tại đây)*
*   🎥 **[Link Video Demo Hệ Thống](#)** *(Cập nhật link video YouTube/Drive tại đây)*
