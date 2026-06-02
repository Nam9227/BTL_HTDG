# BÁO CÁO BÀI TẬP LỚN: HỆ THỐNG ĐẤU GIÁ TRỰC TUYẾN (BTL_HTDG)

## 1. Giới thiệu mục tiêu và phạm vi thực hiện

### 1.1. Mục tiêu đề tài
Đề tài hướng tới việc thiết kế và xây dựng một nền tảng **Hệ thống đấu giá trực tuyến (Online Bidding System)** dựa trên kiến trúc Client-Server. Mục tiêu cốt lõi của hệ thống bao gồm:
*   **Trải nghiệm thời gian thực (Real-time):** Đảm bảo việc cập nhật giá thầu, trạng thái người đấu giá và lịch sử giao dịch diễn ra tức thời thông qua kết nối TCP Socket, không có độ trễ lớn.
*   **Tính minh bạch và Công bằng:** Ghi nhận chính xác thứ tự đặt giá, người ra giá cao nhất và tự động xác định người chiến thắng khi phiên đấu giá kết thúc.
*   **Giao diện trực quan:** Cung cấp ứng dụng Desktop thân thiện bằng công nghệ JavaFX, giúp người dùng dễ dàng theo dõi sản phẩm, quản lý tài chính và thao tác đấu giá.
*   **Hiệu năng và Đa luồng:** Phía Server được thiết kế để chịu tải tốt, xử lý đồng thời hàng loạt yêu cầu từ nhiều Client khác nhau thông qua cơ chế Multi-threading và đồng bộ hóa (Synchronization) an toàn.

### 1.2. Phạm vi thực hiện
Hệ thống được phát triển hoàn chỉnh từ giao diện người dùng đến logic nghiệp vụ backend và cơ sở dữ liệu, phân tách thành các module rõ ràng:

**A. Phân hệ Máy khách (Client - JavaFX)**
*   Quản lý tài khoản (Đăng ký, Đăng nhập, Phân quyền người dùng: Quản trị viên, Người bán, Người mua).
*   Quản lý ví điện tử (Nạp tiền, kiểm tra số dư).
*   Chức năng Người bán: Đăng bán sản phẩm mới (kèm ảnh minh họa, giá khởi điểm, thời gian kết thúc).
*   Chức năng Người mua: Tìm kiếm, tham gia phòng đấu giá, đặt giá thủ công hoặc sử dụng tính năng Đặt giá tự động (Auto-bid).
*   Hệ thống thông báo đẩy (Push Notifications) khi bị vượt giá hoặc khi kết thúc phiên đấu giá.

**B. Phân hệ Máy chủ (Server - Java Socket & MySQL)**
*   Lắng nghe và duy trì kết nối liên tục với nhiều Client.
*   Xử lý nghiệp vụ logic: Duyệt sản phẩm, đối soát số dư ví, xác minh tính hợp lệ của lệnh đặt giá thầu.
*   Dịch vụ phát sóng (Broadcast Service): Đẩy dữ liệu giá mới nhất và lịch sử đấu giá tới tất cả người dùng trong phòng ngay lập tức.
*   Hệ thống lên lịch (Scheduler): Liên tục kiểm tra thời gian thực để tự động đóng các phiên đấu giá đã hết hạn và thông báo người chiến thắng.
*   Tương tác Cơ sở dữ liệu: Lưu trữ thông tin người dùng, sản phẩm, lịch sử giao dịch bằng MySQL (thông qua DAO pattern).

**C. Phân hệ Dùng chung (Common)**
*   Định nghĩa các Entity/Model (User, AuctionItem, BidRecord,...).
*   Đóng gói cấu trúc gói tin giao tiếp mạng (Request/Response) hỗ trợ Serialization để đảm bảo hai bên Client-Server hiểu nhau một cách chính xác.
