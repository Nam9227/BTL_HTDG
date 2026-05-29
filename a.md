# Chi tiết Ghi chú (Kèm Code Context)

Dưới đây là các ghi chú kèm theo đoạn code xung quanh (1 dòng trước và 2 dòng sau) để bạn dễ hình dung.

### bidding-client\src\main\java\com\uet\client\ClientApp.java
**Dòng 21**:
```java
// Hiệu ứng mượt mà khi mở ứng dụng
            com.uet.client.util.TransitionUtils.applyFadeIn(root);
```

**Dòng 37**:
```java
public static void main(String[] args) {
        // Thiết lập múi giờ mặc định của Client JVM sang Asia/Ho_Chi_Minh (GMT+7)
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        launch(args);
```

---
### bidding-client\src\main\java\com\uet\client\network\ClientSocket.java
**Dòng 42**:
```java
// 🌟 THÊM DÒNG NÀY: Ép mạng gửi gói tin đi ngay, không chờ đệm
            socket.setTcpNoDelay(true);
```

**Dòng 62**:
```java
// 🌟 THÊM DÒNG NÀY: Xóa bộ đệm đối tượng cũ, tránh lag dữ liệu
        out.reset();
    }
```

**Dòng 67**:
```java
private void startListening() {
        // Nếu luồng cũ đang chạy thì không tạo luồng mới trùng lặp
        if (listenerThread != null && listenerThread.isAlive()) {
            return;
```

**Dòng 83**:
```java
} catch (java.io.EOFException | java.net.SocketException e) {
                    // 🌟 MẸO KHỬ LỖI ĐỎ: Nếu ta chủ động gọi close(), biến listening sẽ bằng false.
                    // Khi đó, việc dính EOFException là hoàn toàn bình thường, ta cho luồng chết êm ái, không in lỗi ra.
                    if (!listening) {
```

**Dòng 84**:
```java
// 🌟 MẸO KHỬ LỖI ĐỎ: Nếu ta chủ động gọi close(), biến listening sẽ bằng false.
                    // Khi đó, việc dính EOFException là hoàn toàn bình thường, ta cho luồng chết êm ái, không in lỗi ra.
                    if (!listening) {
                        logger.info("ClientSocket: Luồng nghe ngầm đã dừng an toàn sau khi Đăng xuất.");
```

**Dòng 91**:
```java
}
                    break; // Thoát hẳn vòng lặp while để hủy Thread ngầm
                } catch (Exception e) {
                    if (listening) {
```

**Dòng 139**:
```java
try {
            // Hạ cờ hiệu nghe xuống trước để vòng lặp while nhận biết hành vi chủ động đóng
            stopListening();
```

**Dòng 142**:
```java
// Đóng tuần tự từ Stream ra đến Socket vật lý
            if (out != null) {
                out.close();
```

**Dòng 156**:
```java
} finally {
            // 🌟 QUAN TRỌNG NHẤT: Xóa trắng toàn bộ Object cũ về null
            // Để lần sau khi quay lại màn Login bấm nút Đăng nhập, hàm connect() check (socket == null) sẽ tự tạo luồng mới tinh.
            this.socket = null;
```

**Dòng 157**:
```java
// 🌟 QUAN TRỌNG NHẤT: Xóa trắng toàn bộ Object cũ về null
            // Để lần sau khi quay lại màn Login bấm nút Đăng nhập, hàm connect() check (socket == null) sẽ tự tạo luồng mới tinh.
            this.socket = null;
            this.in = null;
```

---
### bidding-client\src\main\java\com\uet\client\ui\AuctionDetailController.java
**Dòng 5**:
```java
import com.uet.common.model.auction.AuctionItem;
import com.uet.common.model.auction.BidRecord; // 🌟 Đã import đối tượng lịch sử từ Common
import com.uet.common.model.user.User;
import com.uet.common.network.AuctionUpdateResponse;
```

**Dòng 11**:
```java
import com.uet.common.network.LeaveAuctionRequest;
import com.uet.common.network.GetBidHistoryRequest; // 🌟 Import gói tin xin lịch sử
import com.uet.common.network.Response;
import javafx.animation.KeyFrame;
```

**Dòng 57**:
```java
// 🌟 ĐÃ KHAI BÁO BẢNG ĐÚNG FX:ID VÀ KIỂU DỮ LIỆU BID_RECORD
    @FXML private TableView<BidRecord> bidHistoryTable;
    @FXML private TableColumn<BidRecord, String> bidderColumn;
```

**Dòng 78**:
```java
// 🌟 BƯỚC CHÍ MẠNG: KẾT NỐI BIẾN CỦA BID_RECORD VÀO CỘT TRÊN GIAO DIỆN ĐỂ HIỆN CHỮ
        bidderColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
```

**Dòng 81**:
```java
// Định dạng số double thành chuỗi tiền tệ #,###đ hiển thị lên bảng
        amountColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatMoney(cellData.getValue().getBidAmount()))
```

**Dòng 86**:
```java
// Định dạng hiển thị Giờ:Phút:Giây cho cột thời gian đặt
        timeColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getBidTime() != null) {
```

**Dòng 154**:
```java
// Logic vẽ biểu đồ gốc ban đầu của Nam giữ nguyên 100%
        addBidHistory("Giá hiện tại", currentPrice);
```

**Dòng 159**:
```java
// 🌟 LẤY LỊCH SỬ TỪ SERVER ĐỂ ĐỔ VÀO CHO BẢNG HIỂN THỊ LÊN LẦN ĐẦU
        requestBidHistoryFromServer(item.getAuctionId());
```

**Dòng 180**:
```java
Platform.runLater(() -> {
                            // 1. Cập nhật thông tin chữ nghĩa giá cả và vẽ biểu đồ cục bộ
                            updateAuctionUI(updatedItem);
```

**Dòng 183**:
```java
// 2. 🌟 GỘP CHUNG REALTIME: Bốc luôn danh sách lịch sử đi kèm đổ thẳng vào bảng!
                            if (updateResponse.getBidHistory() != null) {
                                bidHistoryTable.getItems().clear();
```

**Dòng 189**:
```java
// 3. Hiển thị dòng chữ thông báo xanh/vàng nếu có
                            if (updateResponse.getMessage() != null && !updateResponse.getMessage().isBlank()) {
                                showMessage(updateResponse.getMessage(), true);
```

**Dòng 193**:
```java
} else {
                                messageLabel.setText(""); // Xóa sạch chữ báo lỗi cũ của lượt trước
                            }
                        });
```

**Dòng 211**:
```java
// 🌟 HÀM TẢI LỊCH SỬ CHỈ TÁC ĐỘNG VÀO BẢNG LỊCH SỬ THEO ĐÚNG Ý NAM
    private void requestBidHistoryFromServer(String auctionId) {
        try {
```

**Dòng 223**:
```java
Platform.runLater(() -> {
                            // 🌟 CHỈ THÊM VÀO ĐÚNG BẢNG LỊCH SỬ ĐẶT GIÁ THÔI, GIỮ NGUYÊN BIỂU ĐỒ CỦA NAM
                            bidHistoryTable.getItems().clear();
                            bidHistoryTable.getItems().addAll(list);
```

**Dòng 306**:
```java
// 🌟 ĐỔI CÂU NÀY: Thông báo rõ ràng cho người dùng
        if (amount <= currentPrice) {
            showMessage("Giá đặt mới phải LỚN HƠN giá hiện tại (" + formatMoney(currentPrice) + ")!", false);
```

**Dòng 317**:
```java
// 🌟 ĐỔI CÂU NÀY: Thông báo khi tài khoản hết tiền
        if (amount > currentUser.getBalance().doubleValue()) {
            showMessage("Số dư tài khoản không đủ để thực hiện lượt đặt giá này!", false);
```

**Dòng 399**:
```java
// Áp dụng hiệu ứng mượt mà khi quay lại
            com.uet.client.util.TransitionUtils.applyFadeIn(root);
```

---
### bidding-client\src\main\java\com\uet\client\ui\EditProductController.java
**Dòng 62**:
```java
// 1. Nạp danh sách ComboBox Danh mục
        if (ProductType != null) {
            ProductType.getItems().clear();
```

**Dòng 84**:
```java
case "Gaming" -> "Trò chơi";
                    default -> dbCategory; // Giữ nguyên nếu là danh mục tự do
                };
```

**Dòng 104**:
```java
// 2. Đổ toàn bộ dữ liệu cũ của phiên lên Form
        productNameField.setText(item.getProductName());
        productDescriptionField.setText(item.getDescription());
```

**Dòng 109**:
```java
// Gán dữ liệu hãng sản xuất
        if (brandField != null) {
            brandField.setText(item.getBrand() != null ? item.getBrand() : "");
```

**Dòng 120**:
```java
// Đổ ảnh cũ lên khung Preview
        if (item.getProductImageBytes() != null && item.getProductImageBytes().length > 0) {
            try (ByteArrayInputStream bais = new ByteArrayInputStream(item.getProductImageBytes())) {
```

**Dòng 138**:
```java
// Hiệu ứng Hover kéo thả mượt mà cho vùng ảnh
        dropImageZone.setOnDragEntered(e -> {
            if (e.getDragboard().hasFiles()) {
```

**Dòng 168**:
```java
// Gán dữ liệu sửa đổi vào gói tin gốc AuctionItem gửi đi
            targetItem.setProductName(name);
            targetItem.setDescription(desc);
```

**Dòng 185**:
```java
case "Khác" -> "Other";
                    default -> selectedType; // Fallback
                };
                targetItem.setCategory(dbCategory);
```

**Dòng 197**:
```java
// Hứng gói tin phản hồi cập nhật từ Server
            myListenerInstance = response -> {
                if (response instanceof Response res) {
```

**Dòng 213**:
```java
ClientSocket.getInstance().addMessageListener(myListenerInstance);
            ClientSocket.getInstance().send(targetItem); // Tiến hành gửi

        } catch (NumberFormatException e) {
```

**Dòng 239**:
```java
// --- LUỒNG DUYỆT ẢNH MỚI BẰNG CHUỘT HOẶC KÉO THẢ ---
    @FXML
    private void handleSelectFile() {
```

---
### bidding-client\src\main\java\com\uet\client\ui\HomeController.java
**Dòng 23**:
```java
import org.slf4j.LoggerFactory;
// ... existing code ...

import javafx.scene.image.ImageView;
```

**Dòng 33**:
```java
@FXML
    private VBox sideContent; // Sidebar màu xanh
    @FXML
    private Button openBtn; // Nút 3 gạch (nằm ngoài sidebar)
```

**Dòng 35**:
```java
@FXML
    private Button openBtn; // Nút 3 gạch (nằm ngoài sidebar)
    @FXML
    private Button closeBtn; // Nút X (nằm trong sidebar)
```

**Dòng 37**:
```java
@FXML
    private Button closeBtn; // Nút X (nằm trong sidebar)

    @FXML
```

**Dòng 60**:
```java
// Đồng bộ hiển thị chữ trên Sidebar trang chủ
        userNameLabel.setText(user.getFullName() != null ? user.getFullName() : user.getUsername());
        balanceLabel.setText("Số dư: " + formatMoney(user.getBalance()));
```

**Dòng 70**:
```java
// 🔥 THÊM ĐOẠN NÀY: Để khi từ Profile quay lại Home, ảnh đại diện ở Sidebar
        // Home cũng được cập nhật mới tinh!
        if (user.getAvatarBytes() != null && user.getAvatarBytes().length > 0) {
```

**Dòng 71**:
```java
// 🔥 THÊM ĐOẠN NÀY: Để khi từ Profile quay lại Home, ảnh đại diện ở Sidebar
        // Home cũng được cập nhật mới tinh!
        if (user.getAvatarBytes() != null && user.getAvatarBytes().length > 0) {
            try {
```

**Dòng 74**:
```java
try {
                userAvatar.setImage(null); // Xóa cache ảnh cũ trên Sidebar Home
                javafx.scene.image.Image img = new javafx.scene.image.Image(
                        new java.io.ByteArrayInputStream(user.getAvatarBytes()));
```

**Dòng 154**:
```java
// Nếu là Người mua (Bidder): Ẩn hoàn toàn nút thêm sản phẩm đi
            addProductBtn.setVisible(false);
            addProductBtn.setManaged(false);
```

**Dòng 161**:
```java
// Nếu là Người bán (Seller): Hiện nút Thêm sản phẩm lên ngay!
            addProductBtn.setVisible(true);
            addProductBtn.setManaged(true);
```

**Dòng 165**:
```java
// Tùy chọn: Người bán vẫn có thể xem danh sách sản phẩm chung
            productContainer.setVisible(true);
            productContainer.setManaged(true);
```

**Dòng 185**:
```java
// Ban đầu ẩn Sidebar và nút X đi
        // Giả sử chiều rộng sidebar là 300
        sideContent.setTranslateX(300);
```

**Dòng 186**:
```java
// Ban đầu ẩn Sidebar và nút X đi
        // Giả sử chiều rộng sidebar là 300
        sideContent.setTranslateX(300);
        sideContent.setVisible(false);
```

**Dòng 242**:
```java
for (AuctionItem item : auctions) {
            // 1. Khởi tạo Khung VBox cho Thẻ Card và gán Class CSS đổ bóng, bo góc
            VBox card = new VBox(10);
            card.getStyleClass().add("product-card-node"); // Ăn theo .product-card-node trong CSS
```

**Dòng 244**:
```java
VBox card = new VBox(10);
            card.getStyleClass().add("product-card-node"); // Ăn theo .product-card-node trong CSS
            card.setPrefWidth(210);
            card.setPadding(new Insets(15));
```

**Dòng 248**:
```java
// 2. Xử lý ảnh sản phẩm mẫu/Bytes
            ImageView cardImageView = new ImageView();
            cardImageView.setFitWidth(180);
```

**Dòng 271**:
```java
// 3. Tên sản phẩm
            Label nameLabel = new Label(item.getProductName());
            nameLabel.getStyleClass().add("product-title-label"); // Ăn theo .product-title-label trong CSS
```

**Dòng 273**:
```java
Label nameLabel = new Label(item.getProductName());
            nameLabel.getStyleClass().add("product-title-label"); // Ăn theo .product-title-label trong CSS
            nameLabel.setWrapText(true);
```

**Dòng 276**:
```java
// 4. Cụm hiển thị Giá (Gộp chung mượt mà)
            Label priceTitle = new Label("Giá hiện tại");
            priceTitle.setStyle("-fx-font-family: 'Segoe UI'; -fx-text-fill: #64748b; -fx-font-size: 12px;");
```

**Dòng 281**:
```java
Label currentPriceLabel = new Label(String.format("%,.0f đ", item.getCurrentPrice()));
            currentPriceLabel.getStyleClass().add("product-price-label"); // Ăn theo .product-price-label trong CSS

            VBox priceBox = new VBox(2, priceTitle, currentPriceLabel);
```

**Dòng 285**:
```java
// 5. Thời gian kết thúc phiên
            Label endTimeLabel = new Label("Kết thúc: " + item.getEndTime());
            endTimeLabel.setStyle("-fx-font-family: 'Segoe UI'; -fx-text-fill: #94a3b8; -fx-font-size: 11px;");
```

**Dòng 289**:
```java
// 6. Nút Đấu giá ngay phong cách phẳng bo tròn
            Button bidButton = new Button("🔨 Đấu giá ngay");
            bidButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-weight: bold; -fx-background-radius: 20; -fx-cursor: hand;");
```

**Dòng 297**:
```java
// 7. Gom tất cả cấu trúc phân cấp vào thẻ Card chính
            card.getChildren().addAll(
                    cardImageView,
```

**Dòng 306**:
```java
// Đẩy toàn bộ khối card vào lưới hiển thị FlowPane
            productContainer.getChildren().add(card);
        }
```

**Dòng 322**:
```java
// Áp dụng hiệu ứng chuyển cảnh mượt mà
            com.uet.client.util.TransitionUtils.applyFadeIn(root);
```

**Dòng 326**:
```java
Stage stage = (Stage) productContainer.getScene().getWindow();
            stage.getScene().setRoot(root); // Thay thế ruột scene cực kỳ mượt mà, giữ nguyên trạng thái maximized!
            stage.setTitle("Chi tiết phiên đấu giá - " + item.getProductName());
```

**Dòng 342**:
```java
// Hiệu ứng đẩy Sidebar vào từ phải sang trái
        TranslateTransition menuSlide = new TranslateTransition(Duration.millis(300), sideContent);
        menuSlide.setToX(0);
```

**Dòng 346**:
```java
// Khi mở ra thì ẩn nút Menu đi ngay lập tức hoặc sau animation
        openBtn.setVisible(false);
```

**Dòng 357**:
```java
// Đẩy sidebar ra ngoài
        menuSlide.setToX(width);
```

**Dòng 364**:
```java
closeBtn.setVisible(false);
            // Hiện lại nút Menu ban đầu
            openBtn.setVisible(true);
        });
```

**Dòng 387**:
```java
ProfileController controller = loader.getController();
            controller.setUser(currentUser); // Bắn session user mới nhất sang Profile

            Stage stage = (Stage) userNameLabel.getScene().getWindow(); // Lấy stage từ label bất kỳ
```

**Dòng 389**:
```java
Stage stage = (Stage) userNameLabel.getScene().getWindow(); // Lấy stage từ label bất kỳ

            // Áp dụng hiệu ứng chuyển cảnh mượt mà
```

**Dòng 391**:
```java
// Áp dụng hiệu ứng chuyển cảnh mượt mà
            com.uet.client.util.TransitionUtils.applyFadeIn(root);
```

**Dòng 394**:
```java
// Đổi scene mượt mà, giữ nguyên trạng thái maximized, tuyệt đối không co giãn
            // màn hình
            stage.getScene().setRoot(root);
```

**Dòng 395**:
```java
// Đổi scene mượt mà, giữ nguyên trạng thái maximized, tuyệt đối không co giãn
            // màn hình
            stage.getScene().setRoot(root);
            stage.setTitle("Thông tin tài khoản");
```

**Dòng 413**:
```java
// 1. Lấy Controller của màn hình thêm sản phẩm và truyền User sang
            InformationUploadController controller = loader.getController();
            controller.setUser(currentUser); // Ép truyền dữ liệu ở đây!
```

**Dòng 415**:
```java
InformationUploadController controller = loader.getController();
            controller.setUser(currentUser); // Ép truyền dữ liệu ở đây!

            // Áp dụng hiệu ứng chuyển cảnh mượt mà
```

**Dòng 417**:
```java
// Áp dụng hiệu ứng chuyển cảnh mượt mà
            com.uet.client.util.TransitionUtils.applyFadeIn(root);
```

**Dòng 421**:
```java
Stage stage = (Stage) productContainer.getScene().getWindow();
            stage.getScene().setRoot(root); // Thay thế ruột scene cực kỳ mượt mà, giữ nguyên maximized!
            stage.setTitle("Đăng bán sản phẩm mới");
```

**Dòng 455**:
```java
cleanupListener();
            // 1. Gửi chuỗi "LOGOUT" báo Server đóng luồng phía Server
            ClientSocket.getInstance().send("LOGOUT");
```

**Dòng 458**:
```java
// 🌟 2. GỌI HÀM CLOSE() VỪA SỬA ĐỂ ĐẬP VỠ SOCKET CŨ Ở CLIENT AN TOÀN TRƯỚC KHI
            // VỀ LOGIN
            ClientSocket.getInstance().close();
```

**Dòng 459**:
```java
// 🌟 2. GỌI HÀM CLOSE() VỪA SỬA ĐỂ ĐẬP VỠ SOCKET CŨ Ở CLIENT AN TOÀN TRƯỚC KHI
            // VỀ LOGIN
            ClientSocket.getInstance().close();
```

**Dòng 464**:
```java
// 3. Chuyển Scene về Login mượt mà như cũ...
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login_view.fxml"));
            Parent root = loader.load();
```

**Dòng 468**:
```java
// Hiệu ứng mượt mà
            com.uet.client.util.TransitionUtils.applyFadeIn(root);
```

**Dòng 516**:
```java
try {
            // 1. Nạp file FXML thông báo mà anh em mình vừa tạo
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/notifications.fxml"));
            Parent root = loader.load();
```

**Dòng 520**:
```java
// 2. Lấy controller của trang thông báo và truyền thông tin user hiện tại sang để cào dữ liệu
            NotificationController controller = loader.getController();
            controller.setUser(currentUser); // currentUser là biến lưu Session người dùng ở HomeController của Nam
```

**Dòng 522**:
```java
NotificationController controller = loader.getController();
            controller.setUser(currentUser); // currentUser là biến lưu Session người dùng ở HomeController của Nam

            // 3. Áp dụng hiệu ứng FadeIn mượt mà (nếu có class util hỗ trợ)
```

**Dòng 524**:
```java
// 3. Áp dụng hiệu ứng FadeIn mượt mà (nếu có class util hỗ trợ)
            if (com.uet.client.util.TransitionUtils.class != null) {
                com.uet.client.util.TransitionUtils.applyFadeIn(root);
```

**Dòng 529**:
```java
// 4. Thay ruột màn hình chính sang trang thông báo không chớp nháy
            Stage stage = (Stage) productContainer.getScene().getWindow();
            stage.getScene().setRoot(root);
```

---
### bidding-client\src\main\java\com\uet\client\ui\InformationUploadController.java
**Dòng 36**:
```java
// --- KHAI BÁO CÁC PHẦN TỬ MAP CHÍNH XÁC VỚI FXML MỚI ---
    @FXML private TextField productNameField;
    @FXML private TextArea productDescriptionField;
```

**Dòng 57**:
```java
private User currentUser;
    private ImageData selectedProductImage; // Bộ nhớ đệm lưu mảng byte của duy nhất 1 ảnh

    public void setUser(User user) {
```

**Dòng 71**:
```java
public void initialize() {
        // 1. Nạp danh sách danh mục Tiếng Việt mượt mà
        if (ProductType != null) {
            ProductType.getItems().clear();
```

**Dòng 91**:
```java
// 2. Cấu hình Spinner và mốc thời gian mặc định
        hourStartSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 12));
        hourEndSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 18));
```

**Dòng 100**:
```java
// 3. HIỆU ỨNG DI CHUỘT: Tự động đổi màu Drop Zone khi kéo thả ảnh qua lại
        dropImageZone.setOnDragEntered(e -> {
            if (e.getDragboard().hasFiles()) {
```

**Dòng 108**:
```java
dropImageZone.setOnDragExited(e -> {
            dropImageZone.setStyle(null); // Quay về Class CSS mặc định trong file upload_style.css
        });
    }
```

**Dòng 112**:
```java
// --- LUỒNG XỬ LÝ MEDIA FILE (CLICK CHUỘT HOẶC KÉO THẢ) ---

    @FXML
```

**Dòng 144**:
```java
if (!files.isEmpty()) {
                processAndPreviewImage(files.get(0)); // Chỉ bốc duy nhất file đầu tiên
                success = true;
            }
```

**Dòng 156**:
```java
// Đóng gói dữ liệu ảnh vào Common Model
            this.selectedProductImage = new ImageData(
                    file.getName(),
```

**Dòng 163**:
```java
// Ẩn chữ gợi ý, đẩy ảnh lên khít khịt khung hình
            uploadPromptBox.setVisible(false);
```

**Dòng 169**:
```java
// Ép ảnh tự động co dãn căn giữa mượt mà trong Drop Zone
            productImageView.setPreserveRatio(true);
            productImageView.setSmooth(true);
```

**Dòng 181**:
```java
// --- LUỒNG CHỐT DỮ LIỆU ĐĂNG BÁN LÊN SERVER ---

    @FXML
```

**Dòng 190**:
```java
// 1. Chặn validate rỗng lề
        if (name.isEmpty() || priceText.isEmpty() || selectedProductImage == null) {
            showAlert("Cảnh báo", "Vui lòng điền Tên sản phẩm, Giá khởi điểm và CHỌN 1 ẢNH MINH HỌA!", Alert.AlertType.WARNING);
```

**Dòng 199**:
```java
// 2. Đồng bộ gộp dữ liệu thời gian thành LocalDateTime hoàn chỉnh
            LocalDateTime startDateTime = LocalDateTime.of(
                    startDatePicker.getValue(),
```

**Dòng 215**:
```java
// 3. Mapping chuẩn ngôn ngữ quốc tế sang Server
            String selectedType = ProductType.getSelectionModel().getSelectedItem().trim();
            String itemType = switch (selectedType) {
```

**Dòng 229**:
```java
// 4. Đóng gói chuẩn chỉ 9 tham số gửi qua Socket lên Server
            AddProductRequest req = new AddProductRequest(
                    currentUser.getId(),
```

**Dòng 241**:
```java
// 5. Khởi tạo Listener bắt kết quả phản hồi của Server
            java.util.function.Consumer<Object> addProductListener = new java.util.function.Consumer<>() {
                @Override
```

**Dòng 249**:
```java
showAlert("Thành công", res.getMessage(), Alert.AlertType.INFORMATION);
                                handleCancel(); // Đăng thành công tự trả về màn hình trang chủ
                            } else {
                                showAlert("Thất bại", res.getMessage(), Alert.AlertType.ERROR);
```

**Dòng 260**:
```java
ClientSocket.getInstance().addMessageListener(addProductListener);
            ClientSocket.getInstance().send(req); // Gửi Request

        } catch (NumberFormatException e) {
```

**Dòng 270**:
```java
// NÚT HỦY: QUAY VỀ TRANG CHỦ GIỮ NGUYÊN SESSION TÀI KHOẢN
    @FXML
    private void handleCancel() {
```

**Dòng 278**:
```java
HomeController controller = loader.getController();
            controller.setUser(currentUser); // Bắn lại session user để giữ nguyên avatar/số dư ở Sidebar

            com.uet.client.util.TransitionUtils.applyFadeIn(root);
```

---
### bidding-client\src\main\java\com\uet\client\ui\LoginController.java
**Dòng 36**:
```java
@FXML
    private TextField passTextField; // Cần thêm cái này để khớp với fx:id trong FXML

    @FXML
```

**Dòng 39**:
```java
@FXML
    private ImageView eyeIcon; // Cần @FXML để JavaFX kết nối với ImageView trong Button

    private final Image imageOpen = new Image(getClass().getResourceAsStream("/photo/openEye.png"));
```

**Dòng 61**:
```java
// 2. Đóng gói vào đối tượng
            LoginRequest request = new LoginRequest(user, pass);
```

**Dòng 64**:
```java
// 3. Gửi qua Socket (Dùng Singleton của Nam)
            ClientSocket network = ClientSocket.getInstance();
            network.connect();
```

**Dòng 105**:
```java
if (passField.isVisible()) {
            // Hiện mật khẩu dạng thường
            passTextField.setText(passField.getText());
            passField.setVisible(false);
```

**Dòng 109**:
```java
passTextField.setVisible(true);
            // Đổi ảnh con mắt mở ra (Nam nhớ kiểm tra đường dẫn ảnh của mình nha)
            eyeIcon.setImage(imageOpen);
            isPasswordShown = true;
```

**Dòng 113**:
```java
} else {
            // Ẩn mật khẩu vào dấu chấm
            passField.setText(passTextField.getText());
            passTextField.setVisible(false);
```

**Dòng 142**:
```java
// Hiệu ứng chuyển trang mượt mà
            com.uet.client.util.TransitionUtils.applyFadeIn(root);
```

**Dòng 150**:
```java
// Tạo scene mới và đập thẳng vào Stage
            Scene scene = new Scene(root);
            stage.setTitle(title);
```

**Dòng 155**:
```java
// BẬT MAXIMIZED LUÔN, KHÔNG DÙNG MẸO CO GIÃN GÂY KHỰNG
            stage.setMaximized(true);
            stage.setResizable(true);
```

**Dòng 170**:
```java
// Hiệu ứng chuyển trang mượt mà
            com.uet.client.util.TransitionUtils.applyFadeIn(root);
```

---
### bidding-client\src\main\java\com\uet\client\ui\MyProductsController.java
**Dòng 96**:
```java
// --- Phân quyền hiển thị các nút chức năng theo vai trò ---
        if (user.getRole() == Role.BIDDER) {
            logger.info("Người dùng đăng nhập vai trò Người mua (BUYER). Ẩn các tính năng của Người bán.");
```

**Dòng 196**:
```java
if ("FINISHED".equalsIgnoreCase(item.getStatus())) {
                        // Nếu đấu giá đã kết thúc, chỉ hiển thị bên người bán khi KHÔNG CÓ người thắng (giao dịch thất bại)
                        if (item.getWinnerId() == null || item.getWinnerId().trim().isEmpty()) {
                            renderProductCard(item, false);
```

**Dòng 201**:
```java
} else {
                        // Các trạng thái khác (PENDING, RUNNING) thì luôn hiện bên người bán
                        renderProductCard(item, false);
                    }
```

**Dòng 252**:
```java
if (isWonTab) {
            // --- TRANG TRÚNG ĐẤU GIÁ: CHỈ CÓ NÚT XÓA ---
            Button deleteBtn = createDeleteButton(item, card);
            actionBox.getChildren().add(deleteBtn);
```

**Dòng 256**:
```java
} else {
            // --- TRANG TÔI ĐĂNG BÁN: PHÂN CHIA THEO TRẠNG THÁI ---
            String status = item.getStatus() != null ? item.getStatus().trim().toUpperCase() : "PENDING";
            if ("PENDING".equals(status)) {
```

**Dòng 259**:
```java
if ("PENDING".equals(status)) {
                // ĐANG CHỜ DUYỆT (PENDING): CÓ NÚT SỬA VÀ NÚT XÓA
                Button editBtn = createEditButton(item);
                Button deleteBtn = createDeleteButton(item, card);
```

**Dòng 264**:
```java
} else if ("RUNNING".equals(status)) {
                // ĐANG CHẠY (RUNNING) CHỈ HIỆN NÚT XEM PHIÊN CHI TIẾT
                Button viewBtn = new Button("👁 Xem phiên");
                viewBtn.getStyleClass().add("action-button-view");
```

**Dòng 270**:
```java
} else if ("FINISHED".equals(status)) {
                // ĐÃ KẾT THÚC NHƯNG KHÔNG CÓ NGƯỜI THẮNG (GIAO DỊCH THẤT BẠI): HIỆN NÚT XÓA SẢN PHẨM
                Button deleteBtn = createDeleteButton(item, card);
                actionBox.getChildren().add(deleteBtn);
```

**Dòng 341**:
```java
try {
                // Đổi nút thành trạng thái đang tải
                editBtn.setDisable(true);
                editBtn.setText("⏳ Đang tải...");
```

**Dòng 357**:
```java
cleanupListener(); // Hủy nghe rác mạng

                                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/edit_product_view.fxml"));
```

**Dòng 362**:
```java
// Bắn cả Session USER và bản ghi ITEM đầy đủ từ Server sang trang sửa
                                        EditProductController editController = loader.getController();
                                        editController.setInitData(currentUser, fullItem);
```

---
### bidding-client\src\main\java\com\uet\client\ui\NotificationController.java
**Dòng 19**:
```java
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region; // 🌟 ĐÃ SỬA: Import chuẩn JavaFX Node
import javafx.stage.Stage;
```

**Dòng 73**:
```java
// Phân quyền hiển thị nút thêm sản phẩm
        if (addProductBtn != null) {
            if (user.getRole() == com.uet.common.model.user.Role.SELLER) {
```

**Dòng 93**:
```java
// 1. Tạo gói tin yêu cầu cầm theo ID của Nam lên Server
        GetNotificationsRequest req = new GetNotificationsRequest(currentUser.getId());
```

**Dòng 96**:
```java
// 2. Tạo vị quan sát (Listener) để hứng phản hồi từ Server về
        Consumer<Object> responseListener = new Consumer<>() {
            @Override
```

**Dòng 100**:
```java
public void accept(Object response) {
                // Kiểm tra xem có đúng là gói tin Response thành công chứa danh sách không
                if (response instanceof Response res && res.isSuccess()) {
                    if (res.getData() instanceof List<?> list) {
```

**Dòng 104**:
```java
// 🌟 BẮT BUỘC: Chạy trong Platform.runLater để vẽ giao diện JavaFX không bị treo luồng
                        Platform.runLater(() -> {
                            notificationContainer.getChildren().clear(); // Dọn sạch các thẻ cũ hoặc chữ "Đang tải..."
```

**Dòng 106**:
```java
Platform.runLater(() -> {
                            notificationContainer.getChildren().clear(); // Dọn sạch các thẻ cũ hoặc chữ "Đang tải..."

                            if (list.isEmpty()) {
```

**Dòng 115**:
```java
// Vòng lặp đúc các thẻ dọc từ danh sách Server trả về
                            for (Object item : list) {
                                if (item instanceof Notification noti) {
```

**Dòng 125**:
```java
// Đọc xong dữ liệu thì hủy bỏ Listener này để giải phóng bộ nhớ, tránh trùng lặp tin nhắn
                    ClientSocket.getInstance().removeMessageListener(this);
                }
```

**Dòng 132**:
```java
try {
            // 3. ĐĂNG KÝ VỚI HỆ THỐNG: Báo cho Socket biết để chuẩn bị hứng tai nghe gói tin trả về
            ClientSocket.getInstance().addMessageListener(responseListener);
```

**Dòng 135**:
```java
// 4. BẮN TIN LÊN SERVER: Ra lệnh cho Socket gửi gói tin đi ngay lập tức
            ClientSocket.getInstance().send(req);
```

**Dòng 143**:
```java
e.printStackTrace();
            // Nếu gửi lỗi thì dọn dẹp luôn tai nghe cho đỡ rác hệ thống
            ClientSocket.getInstance().removeMessageListener(responseListener);
        }
```

**Dòng 161**:
```java
// 🌟 Chạy mượt mà vì spacer đã là một Region của JavaFX layout chính hiệu
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
```

---
### bidding-client\src\main\java\com\uet\client\ui\ProfileController.java
**Dòng 9**:
```java
import com.uet.common.network.UpdateProfileRequest;
//import com.uet.common.network.TransactionRequest;
import javafx.application.Platform;
import javafx.fxml.FXML;
```

**Dòng 67**:
```java
// ⚡ BA DÒNG QUYẾT ĐỊNH: Ép ảnh tự động co giãn đều, không bị bóp méo
                avatarImage.setPreserveRatio(false); // Ép vừa khít khung vuông 80x80
                avatarImage.setSmooth(true);         // Khử răng cưa giúp viền ảnh mượt
```

**Dòng 68**:
```java
// ⚡ BA DÒNG QUYẾT ĐỊNH: Ép ảnh tự động co giãn đều, không bị bóp méo
                avatarImage.setPreserveRatio(false); // Ép vừa khít khung vuông 80x80
                avatarImage.setSmooth(true);         // Khử răng cưa giúp viền ảnh mượt
```

**Dòng 69**:
```java
avatarImage.setPreserveRatio(false); // Ép vừa khít khung vuông 80x80
                avatarImage.setSmooth(true);         // Khử răng cưa giúp viền ảnh mượt

            } catch (Exception e) {
```

**Dòng 93**:
```java
// Đổi màu nền sáng hơn cho TẤT CẢ các ô được phép gõ
        String activeStyle = "-fx-background-color: #4a5056; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 10;";
        fullNameField.setStyle(activeStyle);
```

**Dòng 135**:
```java
if (ProfileController.this.currentUser.getAvatarBytes() != null && ProfileController.this.currentUser.getAvatarBytes().length > 0) {
                                    avatarImage.setImage(null); // Xóa bộ nhớ đệm
                                    java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(ProfileController.this.currentUser.getAvatarBytes());
                                    javafx.scene.image.Image img = new javafx.scene.image.Image(bis);
```

**Dòng 163**:
```java
// Hàm khóa lại Form (trả lại màu sẫm)
    private void lockForm() {
        editing = false;
```

**Dòng 173**:
```java
// Khóa đồng bộ màu sẫm cho TẤT CẢ các ô
        String lockedStyle = "-fx-background-color: #2a2e31; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 10;";
        fullNameField.setStyle(lockedStyle);
```

**Dòng 184**:
```java
// 💵 NẠP TIỀN
    @FXML
    private void handleDeposit() {
```

**Dòng 190**:
```java
// 💸 RÚT TIỀN
    @FXML
    private void handleWithdraw() {
```

**Dòng 196**:
```java
// Hàm gom chung xử lý tiền tệ
    private void handleMoneyTransaction(String title, String type) {
        TextInputDialog dialog = new TextInputDialog();
```

**Dòng 226**:
```java
// Server trả user mới
                                        if (res.getData() instanceof User updatedUser) {
```

**Dòng 318**:
```java
// Nút quay lại Trang chủ full màn hình
    @FXML
    private void handleBack() {
```

**Dòng 325**:
```java
// Truyền ngược lại user đã cập nhật về cho trang chủ để đồng bộ UI Sidebar Home
            HomeController controller = loader.getController();
            controller.setUser(currentUser);
```

**Dòng 329**:
```java
// Hiệu ứng chuyển trang mượt mà
            com.uet.client.util.TransitionUtils.applyFadeIn(root);
```

**Dòng 334**:
```java
// Thay ruột scene cực mượt, không chớp màn hình
            stage.getScene().setRoot(root);
            stage.setTitle("Trang chủ Đấu giá");
```

---
### bidding-client\src\main\java\com\uet\client\ui\RegisterController.java
**Dòng 50**:
```java
private void handleTogglePassword(ActionEvent event) {
        // Xác định nút nào vừa được bấm
        Object source = event.getSource();
```

**Dòng 54**:
```java
if (source == btnTogglePassword) {
            // Xử lý cho mật khẩu chính
            toggleLogic(passwordField, passwordText, eyeIcon);
        } else if (source == btnToggleConfirm) {
```

**Dòng 57**:
```java
} else if (source == btnToggleConfirm) {
            // Xử lý cho xác nhận mật khẩu
            toggleLogic(confirmPasswordField, confirmPasswordText, confirmEyeIcon);
        }
```

**Dòng 62**:
```java
// Hàm logic bổ trợ
    private void toggleLogic(PasswordField pField, TextField tField, ImageView icon) {
        if (pField.isVisible()) {
```

**Dòng 84**:
```java
try {
            // Kiểm tra xem các ô nhập liệu có bị null không (do quên đặt fx:id)
            if (fullNameField == null) {
                logger.error("Lỗi: fullNameField bị null. Kiểm tra lại fx:id trong FXML!");
```

**Dòng 99**:
```java
: confirmPasswordField.getText().trim();
            // ... các logic kiểm tra khác ...

            if (fullName.isEmpty()) {
```

**Dòng 202**:
```java
// Hiệu ứng mượt mà
            com.uet.client.util.TransitionUtils.applyFadeIn(root);
```

---
### bidding-client\src\main\java\com\uet\client\ui\admin\AdminAuctionsController.java
**Dòng 54**:
```java
}
        // Cấu hình các cột thông tin cơ bản
        idColumn.setCellValueFactory(new PropertyValueFactory<>("auctionId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
```

**Dòng 74**:
```java
String display = item;
                    String color = "#111827"; // dark
                    if ("PENDING".equalsIgnoreCase(item)) {
                        display = "Chờ duyệt";
```

**Dòng 77**:
```java
display = "Chờ duyệt";
                        color = "#F59E0B"; // orange
                    } else if ("ACTIVE".equalsIgnoreCase(item)) {
                        display = "Chờ chạy";
```

**Dòng 80**:
```java
display = "Chờ chạy";
                        color = "#2563EB"; // blue
                    } else if ("RUNNING".equalsIgnoreCase(item)) {
                        display = "Đang chạy";
```

**Dòng 83**:
```java
display = "Đang chạy";
                        color = "#16A34A"; // green
                    } else if ("FINISHED".equalsIgnoreCase(item)) {
                        display = "Đã đóng";
```

**Dòng 86**:
```java
display = "Đã đóng";
                        color = "#6B7280"; // gray
                    } else if ("REJECTED".equalsIgnoreCase(item)) {
                        display = "Từ chối";
```

**Dòng 89**:
```java
display = "Từ chối";
                        color = "#DC2626"; // red
                    }
                    setText(display);
```

**Dòng 97**:
```java
// Sinh cặp nút bấm Duyệt / Từ chối động trên từng dòng
        setupActionColumn();
```

**Dòng 110**:
```java
// Tải dữ liệu từ server
        loadPendingAuctions();
    }
```

**Dòng 189**:
```java
{
                        // Style các nút
                        btnApprove.setStyle("-fx-background-color: #16A34A; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand;");
                        btnReject.setStyle("-fx-background-color: #DC2626; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand;");
```

**Dòng 208**:
```java
// 1. Nếu là CHỜ DUYỆT
                            if ("PENDING".equalsIgnoreCase(status)) {
                                btnApprove.setOnAction(e -> handleProcessApproval(currentItem.getAuctionId(), true));
```

**Dòng 215**:
```java
}
                            // 2. Nếu là ĐÃ DUYỆT (Chờ đến giờ chạy)
                            else if ("ACTIVE".equalsIgnoreCase(status)) {
                                btnForceEnd.setText("Hủy phiên"); // Admin có thể hủy nếu muốn
```

**Dòng 217**:
```java
else if ("ACTIVE".equalsIgnoreCase(status)) {
                                btnForceEnd.setText("Hủy phiên"); // Admin có thể hủy nếu muốn
                                btnForceEnd.setOnAction(e -> handleForceEnd(currentItem.getAuctionId()));
                                pane.getChildren().add(btnForceEnd);
```

**Dòng 221**:
```java
}
                            // 3. 🌟 NẾU ĐANG CHẠY ĐẤU GIÁ (RUNNING) -> HIỆN NÚT ÉP KẾT THÚC SỚM
                            else if ("RUNNING".equalsIgnoreCase(status)) {
                                btnForceEnd.setText("Kết thúc sớm");
```

**Dòng 228**:
```java
}
                            // 4. Nếu là ĐÃ KẾT THÚC THẬT SỰ (FINISHED / COMPLETED)
                            else {
                                lblFinished.setText("Đã đóng");
```

**Dòng 255**:
```java
if (res.isSuccess()) {
                                loadPendingAuctions(); // Refresh lại bảng sau khi duyệt thành công
                            }
                        });
```

**Dòng 281**:
```java
// --- Các hàm chuyển màn hình Sidebar có sẵn của Nam ---
    @FXML private void goDashboard() { switchScene("/view/admin/admin_dashboard.fxml"); }
    @FXML private void goUsers() { switchScene("/view/admin/admin_users.fxml"); }
```

**Dòng 319**:
```java
try {
            // 1. Tạo gói tin yêu cầu kết thúc sớm
            ForceEndRequest request = new ForceEndRequest(auctionId);
```

**Dòng 322**:
```java
// 2. Tạo Listener để hứng kết quả phản hồi từ Server trả về
            java.util.function.Consumer<Object> listener = new java.util.function.Consumer<>() {
                @Override
```

**Dòng 329**:
```java
if (res.isSuccess()) {
                                // Nếu thành công, load lại bảng để cập nhật giao diện lập tức
                                loadPendingAuctions();
                            } else {
```

**Dòng 335**:
```java
});
                        // Nhận xong thì gỡ Listener ra cho đỡ rác bộ nhớ
                        ClientSocket.getInstance().removeMessageListener(this);
                    }
```

**Dòng 341**:
```java
// 3. Đăng ký nhận tin và bắn gói Request lên Server
            ClientSocket.getInstance().addMessageListener(listener);
            ClientSocket.getInstance().send(request);
```

---
### bidding-client\src\main\java\com\uet\client\ui\admin\AdminDashboardController.java
**Dòng 41**:
```java
// 2. Load dữ liệu
        refreshData();
    }
```

---
### bidding-client\src\main\java\com\uet\client\ui\admin\AdminUsersController.java
**Dòng 42**:
```java
// Cột Đăng nhập gần nhất khớp với fx:id trong file FXML
    @FXML private TableColumn<User, LocalDateTime> lastLoginColumn;
```

**Dòng 52**:
```java
public void initialize() {
        // 1. Map dữ liệu vào các cột TableView
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
```

**Dòng 75**:
```java
// 2. Cấu hình định dạng và ĐÃ ĐỔI MÀU CHỮ TỐI cho cột Đăng nhập gần nhất
        lastLoginColumn.setCellValueFactory(new PropertyValueFactory<>("lastLoginAt"));
        lastLoginColumn.setCellFactory(column -> new TableCell<>() {
```

**Dòng 85**:
```java
setText("Chưa từng đăng nhập");
                    setStyle("-fx-text-fill: #888888; -fx-font-style: italic;"); // Chữ màu xám nghiêng
                } else {
                    setText(item.format(formatter));
```

**Dòng 88**:
```java
setText(item.format(formatter));
                    // Đã sửa thành #1e293b để chữ hiển thị màu xanh đen đậm rõ nét trên nền trắng
                    setStyle("-fx-text-fill: #1e293b; -fx-font-weight: bold; -fx-font-style: normal;");
                }
```

**Dòng 94**:
```java
// 3. Thiết lập dữ liệu lọc cho TableView
        filteredData = new FilteredList<>(masterData, p -> true);
        userTable.setItems(filteredData);
```

**Dòng 108**:
```java
// 4. Đăng ký nhận gói tin kết quả từ Socket và kéo dữ liệu thời gian thực
        setupSocketListener();
        fetchUsersFromServer();
```

---
### bidding-client\src\main\java\com\uet\client\util\TransitionUtils.java
**Dòng 17**:
```java
// Cấu hình ban đầu ẩn và lệch xuống dưới 15px
        node.setOpacity(0.0);
        node.setTranslateY(15);
```

**Dòng 21**:
```java
// Hiệu ứng mờ dần (Fade-in)
        FadeTransition fade = new FadeTransition(Duration.millis(350), node);
        fade.setFromValue(0.0);
```

**Dòng 26**:
```java
// Hiệu ứng trượt nhẹ lên (Slide-up)
        TranslateTransition translate = new TranslateTransition(Duration.millis(350), node);
        translate.setFromY(15);
```

**Dòng 31**:
```java
// Kết hợp chạy đồng thời cả 2 hiệu ứng
        ParallelTransition parallel = new ParallelTransition(fade, translate);
        parallel.play();
```

---
### bidding-common\src\main\java\com\uet\common\model\auction\Auction.java
**Dòng 13**:
```java
private LocalDateTime endTime;
    private String status; // OPEN / RUNNING / FINISHED / PAID / CANCELED
    private BidRecord highestBid;
```

---
### bidding-common\src\main\java\com\uet\common\model\auction\BidRecord.java
**Dòng 12**:
```java
private String userId;
    private String username;    // Thêm tên người trả giá để hiển thị lên giao diện Client
    private double bidAmount;
    private LocalDateTime bidTime;
```

**Dòng 16**:
```java
// Constructor mặc định
    public BidRecord() {}
```

**Dòng 19**:
```java
// Getter và Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
```

---
### bidding-common\src\main\java\com\uet\common\model\notification\Notification.java
**Dòng 10**:
```java
private int id;
    private String userId; // 🌟 Đã chuyển sang kiểu String cho khớp với VARCHAR(50) trong DB của Nam
    private String title;
    private String content;
```

---
### bidding-common\src\main\java\com\uet\common\model\user\User.java
**Dòng 5**:
```java
import java.math.BigDecimal;
import java.time.LocalDateTime; // Đảm bảo có dòng này

public class User implements Serializable {
```

**Dòng 37**:
```java
// 2. CHỈ THÊM GETTER VÀ SETTER NÀY Ở DƯỚI CÙNG
    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
```

**Dòng 46**:
```java
// --- TẤT CẢ CÁC GETTER / SETTER CŨ CỦA BẠN GIỮ NGUYÊN HOÀN TOÀN ---
    public String getId() { return id; }
    public String getUsername() { return username; }
```

---
### bidding-common\src\main\java\com\uet\common\network\AddProductRequest.java
**Dòng 7**:
```java
public class AddProductRequest implements Serializable {
    // Mã định danh phiên bản để đảm bảo Client và Server đồng bộ cấu trúc Object
    private static final long serialVersionUID = 1L;
```

**Dòng 14**:
```java
private double startPrice;
    private ImageData productImage; // Đối tượng chứa mảng byte ảnh đơn của Nam
    private String itemType;        // Chuỗi tiếng Anh lưu loại sản phẩm ("Electronics", "Fashion"...)
```

**Dòng 15**:
```java
private ImageData productImage; // Đối tượng chứa mảng byte ảnh đơn của Nam
    private String itemType;        // Chuỗi tiếng Anh lưu loại sản phẩm ("Electronics", "Fashion"...)

    // 🌟 ĐÃ ĐƯA ĐÚNG VÀO TRONG CLASS: Khai báo thêm 3 thuộc tính mới
```

**Dòng 17**:
```java
// 🌟 ĐÃ ĐƯA ĐÚNG VÀO TRONG CLASS: Khai báo thêm 3 thuộc tính mới
    private String brand;
    private LocalDateTime startTime;
```

**Dòng 22**:
```java
// 🌟 ĐÃ CẬP NHẬT CONSTRUCTOR: Truyền đầy đủ tham số từ Client lên
    public AddProductRequest(String sellerId, String productName, String description, double startPrice,
                             ImageData productImage, String itemType, String brand,
```

**Dòng 37**:
```java
// =========================================================================
    // ĐẦY ĐỦ CÁC HÀM GETTER VÀ SETTER
    // =========================================================================
```

**Dòng 38**:
```java
// =========================================================================
    // ĐẦY ĐỦ CÁC HÀM GETTER VÀ SETTER
    // =========================================================================
```

**Dòng 39**:
```java
// ĐẦY ĐỦ CÁC HÀM GETTER VÀ SETTER
    // =========================================================================

    public String getSellerId() {
```

---
### bidding-common\src\main\java\com\uet\common\network\ApproveAuctionRequest.java
**Dòng 11**:
```java
// Hàm tạo (Constructor)
    public ApproveAuctionRequest(String auctionId, boolean approved) {
        this.auctionId = auctionId;
```

**Dòng 17**:
```java
// Các hàm Getter để Server bóc tách dữ liệu
    public String getAuctionId() {
        return auctionId;
```

---
### bidding-common\src\main\java\com\uet\common\network\DeleteProductRequest.java
**Dòng 8**:
```java
private String auctionId; // Dùng mã phiên đấu giá để Server biết cần xóa phiên nào
    private String userId;    // Gửi kèm ID người xóa để Server check quyền (tránh người khác xóa trộm)
```

**Dòng 9**:
```java
private String auctionId; // Dùng mã phiên đấu giá để Server biết cần xóa phiên nào
    private String userId;    // Gửi kèm ID người xóa để Server check quyền (tránh người khác xóa trộm)

    public DeleteProductRequest(String auctionId, String userId) {
```

---
### bidding-common\src\main\java\com\uet\common\network\DeleteUserRequest.java
**Dòng 11**:
```java
// Constructor để bọc ID user cần xóa
    public DeleteUserRequest(String userId) {
        this.userId = userId;
```

**Dòng 16**:
```java
// Getter và Setter để Server có thể trích xuất thông tin
    public String getUserId() {
        return userId;
```

---
### bidding-common\src\main\java\com\uet\common\network\ForceEndRequest.java
**Dòng 10**:
```java
// 🌟 Constructor mặc định không tham số (Bắt buộc phải có để gửi nhận Object)
    public ForceEndRequest() {
    }
```

**Dòng 18**:
```java
// Getter và Setter
    public String getAuctionId() {
        return auctionId;
```

---
### bidding-common\src\main\java\com\uet\common\network\GetActiveAuctionsRequest.java
**Dòng 9**:
```java
private String userId;
    private String type; // "ACTIVE" or "USER"

    public GetActiveAuctionsRequest() {
```

---
### bidding-common\src\main\java\com\uet\common\network\GetAllUsersRequest.java
**Dòng 7**:
```java
public GetAllUsersRequest() {
        // Constructor rỗng dùng để tạo đối tượng gửi đi làm tín hiệu
    }
}
```

---
### bidding-common\src\main\java\com\uet\common\network\GetBidHistoryRequest.java
**Dòng 8**:
```java
private String auctionId; // Client truyền ID phiên đấu giá muốn xem lịch sử

    public GetBidHistoryRequest(String auctionId) {
```

---
### bidding-common\src\main\java\com\uet\common\network\GetNotificationsRequest.java
**Dòng 11**:
```java
private String userId; // ID của người dùng đang đăng nhập (kiểu VARCHAR/String)

    // Constructor (Hàm khởi tạo)
```

**Dòng 13**:
```java
// Constructor (Hàm khởi tạo)
    public GetNotificationsRequest(String userId) {
        this.userId = userId;
```

**Dòng 18**:
```java
// Getter để Server lấy ra ID và quét database
    public String getUserId() {
        return userId;
```

---
### bidding-common\src\main\java\com\uet\common\network\ImageData.java
**Dòng 11**:
```java
private byte[] data;
    private String imageType;      // ✅ NEW: "AVATAR", "PRODUCT", etc.
    private String imageId;        // ✅ NEW: userId, productId, auctionId, etc.
```

**Dòng 12**:
```java
private String imageType;      // ✅ NEW: "AVATAR", "PRODUCT", etc.
    private String imageId;        // ✅ NEW: userId, productId, auctionId, etc.

    // Constructor 1: Cho upload (client → server)
```

**Dòng 14**:
```java
// Constructor 1: Cho upload (client → server)
    public ImageData(String originalFileName, String contentType, byte[] data) {
        this.originalFileName = originalFileName;
```

**Dòng 21**:
```java
// Constructor 2: Đầy đủ (server → client)
    public ImageData(String originalFileName, String contentType, byte[] data,
                     String imageType, String imageId) {
```

---
### bidding-common\src\main\java\com\uet\common\network\UpdateUserStatusRequest.java
**Dòng 10**:
```java
private String userId;
    private boolean active; // true: Mở khóa (Hoạt động), false: Khóa tài khoản

    // Constructor đầy đủ tham số
```

**Dòng 12**:
```java
// Constructor đầy đủ tham số
    public UpdateUserStatusRequest(String userId, boolean active) {
        this.userId = userId;
```

**Dòng 18**:
```java
// Hàm lấy ID người dùng cần xử lý
    public String getUserId() {
        return userId;
```

**Dòng 27**:
```java
// Hàm lấy trạng thái mong muốn (định dạng của kiểu boolean bắt đầu bằng 'is')
    public boolean isActive() {
        return active;
```

---
### bidding-server\src\main\java\com\uet\server\database\DBConnection.java
**Dòng 16**:
```java
// 🌟 BIẾN QUYẾT ĐỊNH: Cờ hiệu kiểm tra xem đã in log kết nối lần nào chưa
    private static boolean isLogPrinted = false;
```

**Dòng 23**:
```java
// Đọc cấu hình từ file db.properties
            FileInputStream fis = new FileInputStream("bidding-server/config/db.properties");
            properties.load(fis);
```

**Dòng 31**:
```java
// Khởi tạo kết nối động tới MySQL để tránh nghẽn luồng
            Connection conn = DriverManager.getConnection(url, username, password);
```

**Dòng 34**:
```java
// 🌟 CHỈ HIỆN 1 LẦN ĐẦU: Nếu cờ hiệu chưa bật thì mới in log và bật cờ lên
            if (!isLogPrinted) {
                logger.info("DB URL = {}", url);
```

**Dòng 38**:
```java
logger.info("Connected to database successfully!");
                isLogPrinted = true; // Khóa cờ lại, các lần gọi sau sẽ bỏ qua khối lệnh này
            }
```

---
### bidding-server\src\main\java\com\uet\server\database\dao\AuctionDAO.java
**Dòng 220**:
```java
// 🌟 ĐÃ SỬA: Bồi thêm cột id vào câu lệnh INSERT của bảng auctions
        String sqlAuction = """
                INSERT INTO auctions
```

**Dòng 230**:
```java
conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Bật Transaction bảo mật

            // 🌟 1. Sinh ID ngẫu nhiên cho sản phẩm
```

**Dòng 232**:
```java
// 🌟 1. Sinh ID ngẫu nhiên cho sản phẩm
            String generatedItemId = com.uet.server.util.IdGenerator.generateId();
```

**Dòng 235**:
```java
// 🌟 2. Sinh ID ngẫu nhiên cho phiên đấu giá (auction_id)
            String generatedAuctionId = com.uet.server.util.IdGenerator.generateId();
```

**Dòng 238**:
```java
// --- BƯỚC 1: CHÈN VÀO BẢNG ITEMS ---
            try (PreparedStatement psItem = conn.prepareStatement(sqlItem)) {
                psItem.setString(1, generatedItemId);
```

**Dòng 254**:
```java
// --- BƯỚC 2: CHÈN VÀO BẢNG AUCTIONS ---
            try (PreparedStatement psAuction = conn.prepareStatement(sqlAuction)) {
                psAuction.setString(1, generatedAuctionId); // Ném ID phiên đấu giá vừa sinh vào cột id
```

**Dòng 256**:
```java
try (PreparedStatement psAuction = conn.prepareStatement(sqlAuction)) {
                psAuction.setString(1, generatedAuctionId); // Ném ID phiên đấu giá vừa sinh vào cột id
                psAuction.setString(2, generatedItemId);    // Khóa ngoại liên kết sang bảng items
                psAuction.setDouble(3, startPrice);
```

**Dòng 257**:
```java
psAuction.setString(1, generatedAuctionId); // Ném ID phiên đấu giá vừa sinh vào cột id
                psAuction.setString(2, generatedItemId);    // Khóa ngoại liên kết sang bảng items
                psAuction.setDouble(3, startPrice);
                psAuction.setDouble(4, startPrice);
```

**Dòng 302**:
```java
conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Bật Transaction để an toàn cho ví tiền và giá cả

            String bidId = com.uet.server.util.IdGenerator.generateId(); // Dùng hàm sinh ID của Nam
```

**Dòng 304**:
```java
String bidId = com.uet.server.util.IdGenerator.generateId(); // Dùng hàm sinh ID của Nam

             // 1. Chèn vào bảng lịch sử bids
```

**Dòng 306**:
```java
// 1. Chèn vào bảng lịch sử bids
            try (PreparedStatement psBid = conn.prepareStatement(sqlBid)) {
                psBid.setString(1, bidId);
```

**Dòng 321**:
```java
// 2. Cập nhật giá cao nhất hiện tại và người đang tạm thắng vào bảng auctions
            try (PreparedStatement psUpdate = conn.prepareStatement(sqlUpdateAuction)) {
                psUpdate.setDouble(1, bidAmount);
```

**Dòng 346**:
```java
// 🌟 HÀM 2: LẤY LỊCH SỬ ĐẤU GIÁ CỦA 1 SẢN PHẨM (Sắp xếp lượt mới nhất lên đầu)
    public List<BidRecord> getBidHistory(String auctionId) {
        List<BidRecord> history = new ArrayList<>();
```

**Dòng 367**:
```java
record.setUserId(rs.getString("user_id"));
                    record.setUsername(rs.getString("username")); // Lấy từ lệnh JOIN bảng users
                    record.setBidAmount(rs.getDouble("bid_amount"));
```

**Dòng 391**:
```java
// 🌟 Câu lệnh SQL đã được khớp 100% với tên cột thực tế của Nam:
        // a.product_id kết nối với i.id
        // Sắp xếp theo thời gian bắt đầu a.start_time DESC
```

**Dòng 392**:
```java
// 🌟 Câu lệnh SQL đã được khớp 100% với tên cột thực tế của Nam:
        // a.product_id kết nối với i.id
        // Sắp xếp theo thời gian bắt đầu a.start_time DESC
        String sql = "SELECT a.id AS auction_id, " +
```

**Dòng 393**:
```java
// a.product_id kết nối với i.id
        // Sắp xếp theo thời gian bắt đầu a.start_time DESC
        String sql = "SELECT a.id AS auction_id, " +
                "       i.name AS product_name, " +
```

**Dòng 455**:
```java
public int startEligibleAuctions() {
        // Lấy danh sách các phiên chuẩn bị được chuyển sang RUNNING để gửi thông báo
        String selectSql = "SELECT a.id, i.name, i.seller_id " +
                           "FROM auctions a JOIN items i ON a.product_id = i.id " +
```

**Dòng 467**:
```java
// 1. Đọc danh sách trước khi update
            List<String[]> startingAuctions = new ArrayList<>();
            try (ResultSet rs = psSelect.executeQuery()) {
```

**Dòng 478**:
```java
// 2. Cập nhật trạng thái
            int rows = ps.executeUpdate();
            if (rows > 0) {
```

**Dòng 483**:
```java
// 3. Gửi thông báo
                com.uet.server.database.dao.UserDAO userDAO = new com.uet.server.database.dao.UserDAO();
                for (String[] auctionInfo : startingAuctions) {
```

**Dòng 489**:
```java
// Xóa thông báo đã duyệt
                    userDAO.deleteNotificationByKeyword(sellerId, "Sản phẩm '" + productName + "' đã được phê duyệt");
```

**Dòng 492**:
```java
// Thêm thông báo đang đấu giá
                    userDAO.createNotification(sellerId, "Đang đấu giá", "Sản phẩm '" + productName + "' đã bắt đầu phiên đấu giá.");
                }
```

**Dòng 503**:
```java
// 2. Hàm quét các phiên RUNNING đã hết giờ để chuyển sang FINISHED
    public int finishExpiredAuctions() {
        // 1. Dùng INNER JOIN để bốc luôn mã người bán (i.sender_id hoặc i.seller_id) từ bảng items lên
```

**Dòng 505**:
```java
public int finishExpiredAuctions() {
        // 1. Dùng INNER JOIN để bốc luôn mã người bán (i.sender_id hoặc i.seller_id) từ bảng items lên
        // 💡 Chú ý: Ở ảnh HeidiSQL trước Nam chụp, cột người bán trong bảng items tên là 'seller_id' nhé!
        String selectSql = "SELECT a.id AS auction_id, a.winner_id, a.current_price, i.seller_id, i.name " +
```

**Dòng 506**:
```java
// 1. Dùng INNER JOIN để bốc luôn mã người bán (i.sender_id hoặc i.seller_id) từ bảng items lên
        // 💡 Chú ý: Ở ảnh HeidiSQL trước Nam chụp, cột người bán trong bảng items tên là 'seller_id' nhé!
        String selectSql = "SELECT a.id AS auction_id, a.winner_id, a.current_price, i.seller_id, i.name " +
                "FROM auctions a " +
```

**Dòng 514**:
```java
// Khởi tạo WalletDAO để xử lý luồng tiền
        com.uet.server.database.dao.WalletDAO walletDAO = new com.uet.server.database.dao.WalletDAO();
        com.uet.server.database.dao.UserDAO userDAO = new com.uet.server.database.dao.UserDAO();
```

**Dòng 526**:
```java
String winnerId = rs.getString("winner_id");
                String sellerId = rs.getString("seller_id"); // Mã của chủ sản phẩm (Người bán)
                String productName = rs.getString("name");
                double finalPrice = rs.getDouble("current_price");
```

**Dòng 530**:
```java
// Cập nhật trạng thái phiên này thành FINISHED
                try (java.sql.PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                    psUpdate.setString(1, auctionId);
```

**Dòng 538**:
```java
// Xóa thông báo "đang đấu giá" của người bán
                userDAO.deleteNotificationByKeyword(sellerId, "Sản phẩm '" + productName + "' đã bắt đầu phiên đấu giá");
```

**Dòng 541**:
```java
// Gửi thông báo kết thúc cho người bán
                userDAO.createNotification(sellerId, "Đấu giá kết thúc", "Sản phẩm '" + productName + "' đã kết thúc đấu giá. Đang chờ xử lý giao dịch.");
```

**Dòng 544**:
```java
// Có người thắng cuộc -> Tiến hành luân chuyển dòng tiền
                if (winnerId != null && !winnerId.trim().isEmpty()) {
```

**Dòng 547**:
```java
// Dòng 1: TRỪ TIỀN THẬT CỦA NGƯỜI THẮNG CUỘC (Giá trị âm)
                    boolean isDeducted = walletDAO.updateBalance(winnerId, -finalPrice);
```

**Dòng 550**:
```java
// Dòng 2: CỘNG TIỀN THẬT VÀO VÍ NGƯỜI BÁN (Giá trị dương)
                    boolean isCredited = walletDAO.updateBalance(sellerId, finalPrice);
```

**Dòng 557**:
```java
// Gửi thông báo thành công
                        userDAO.createNotification(winnerId, "Trúng đấu giá", "Chúc mừng! Bạn đã trúng đấu giá sản phẩm '" + productName + "' với mức giá " + String.format("%,.0f", finalPrice) + "đ.");
                        userDAO.createNotification(sellerId, "Giao dịch thành công", "Sản phẩm '" + productName + "' đã được bán với giá " + String.format("%,.0f", finalPrice) + "đ.");
```

**Dòng 561**:
```java
// Cập nhật lại số dư trên UI cho Client
                        com.uet.common.model.user.User updatedWinner = userDAO.findUserByUserId(winnerId);
                        if (updatedWinner != null) {
```

**Dòng 618**:
```java
// 1. Cập nhật trạng thái thành FINISHED
            try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                psUpdate.setString(1, auctionId);
```

**Dòng 627**:
```java
// Xóa thông báo "đang đấu giá" của người bán
            userDAO.deleteNotificationByKeyword(sellerId, "Sản phẩm '" + productName + "' đã bắt đầu phiên đấu giá");
```

**Dòng 630**:
```java
// Gửi thông báo kết thúc cho người bán
            userDAO.createNotification(sellerId, "Đấu giá bị buộc kết thúc", "Phiên đấu giá sản phẩm '" + productName + "' đã bị Admin buộc kết thúc.");
```

**Dòng 633**:
```java
// 2. Thực hiện luân chuyển dòng tiền
            if (winnerId != null && !winnerId.trim().isEmpty()) {
                boolean isDeducted = walletDAO.updateBalance(winnerId, -finalPrice);
```

**Dòng 642**:
```java
// Gửi thông báo thành công
                    userDAO.createNotification(winnerId, "Trúng đấu giá (Admin đóng)", "Phiên đấu giá bị đóng. Bạn đã trúng đấu giá sản phẩm '" + productName + "' với mức giá " + String.format("%,.0f", finalPrice) + "đ.");
                    userDAO.createNotification(sellerId, "Giao dịch thành công", "Sản phẩm '" + productName + "' đã được bán với giá " + String.format("%,.0f", finalPrice) + "đ.");
```

**Dòng 646**:
```java
// Cập nhật lại số dư trên UI cho Client
                    com.uet.common.model.user.User updatedWinner = userDAO.findUserByUserId(winnerId);
                    if (updatedWinner != null) {
```

**Dòng 697**:
```java
public boolean deleteAuction(String auctionId) {
        // 1. Câu lệnh lấy mã product_id trước khi xóa phiên đấu giá
        String sqlGetProductId = "SELECT product_id FROM auctions WHERE id = ?";
```

**Dòng 700**:
```java
// 2. Câu lệnh xóa ở bảng bids trước để gỡ ràng buộc khóa ngoại
        String sqlDeleteBids = "DELETE FROM bids WHERE auction_id = ?";
```

**Dòng 703**:
```java
// 3. Câu lệnh xóa ở bảng auctions sau khi bids sạch bóng
        String sqlDeleteAuction = "DELETE FROM auctions WHERE id = ?";
```

**Dòng 706**:
```java
// 4. Câu lệnh xóa ở bảng items sau khi auctions sạch bóng
        String sqlDeleteItem = "DELETE FROM items WHERE id = ?";
```

**Dòng 712**:
```java
conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // 🌟 BẬT TRANSACTION: Đảm bảo xóa là phải xóa sạch cả hai, lỗi là hủy lệnh

            String productId = null;
```

**Dòng 716**:
```java
// BƯỚC 1: Tìm mã product_id liên kết
            try (PreparedStatement psGet = conn.prepareStatement(sqlGetProductId)) {
                psGet.setString(1, auctionId);
```

**Dòng 726**:
```java
// Nếu không tìm thấy phiên đấu giá này trong hệ thống, dừng lại luôn
            if (productId == null) {
                logger.warn("[DELETE WARN] Không tìm thấy phiên đấu giá với ID: {}", auctionId);
```

**Dòng 732**:
```java
// BƯỚC 2: Xóa các lượt đặt giá trong bảng bids liên kết trước
            try (PreparedStatement psDelBids = conn.prepareStatement(sqlDeleteBids)) {
                psDelBids.setString(1, auctionId);
```

**Dòng 739**:
```java
// BƯỚC 3: Xóa dữ liệu tại bảng auctions
            try (PreparedStatement psDelAuction = conn.prepareStatement(sqlDeleteAuction)) {
                psDelAuction.setString(1, auctionId);
```

**Dòng 746**:
```java
// BƯỚC 4: Xóa dữ liệu tương ứng tại bảng items
            try (PreparedStatement psDelItem = conn.prepareStatement(sqlDeleteItem)) {
                psDelItem.setString(1, productId);
```

**Dòng 753**:
```java
// Vượt qua tất cả an toàn -> Chốt lưu thay đổi vào Database thật
            conn.commit();
            logger.info("[DELETE SUCCESS] Đã dọn dẹp sạch sẽ phiên {} và sản phẩm {} khỏi hệ thống!", auctionId, productId);
```

**Dòng 762**:
```java
try {
                    conn.rollback(); // Hủy toàn bộ các lệnh xóa dở dang nếu có một bảng bị lỗi
                } catch (Exception ignored) {}
            }
```

**Dòng 769**:
```java
try {
                    conn.setAutoCommit(true); // Trả lại trạng thái mặc định cho Connection Pool
                    conn.close();
                } catch (Exception ignored) {}
```

**Dòng 792**:
```java
conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Bật Transaction bảo mật

            // BƯỚC 1: CẬP NHẬT BẢNG ITEMS
```

**Dòng 794**:
```java
// BƯỚC 1: CẬP NHẬT BẢNG ITEMS
            try (PreparedStatement psItem = conn.prepareStatement(sqlUpdateItem)) {
                psItem.setString(1, item.getProductName());
```

**Dòng 801**:
```java
psItem.setString(5, item.getBrand());
                psItem.setString(6, item.getProductId()); // items.id is product_id
                psItem.executeUpdate();
            }
```

**Dòng 805**:
```java
// BƯỚC 2: CẬP NHẬT BẢNG AUCTIONS
            try (PreparedStatement psAuction = conn.prepareStatement(sqlUpdateAuction)) {
                psAuction.setDouble(1, item.getStartPrice());
```

**Dòng 808**:
```java
psAuction.setDouble(1, item.getStartPrice());
                psAuction.setDouble(2, item.getStartPrice()); // reset currentPrice về startPrice khi sửa
                
                java.util.Calendar cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
```

---
### bidding-server\src\main\java\com\uet\server\database\dao\UserDAO.java
**Dòng 41**:
```java
// --- CẬP NHẬT THỜI GIAN ĐĂNG NHẬP GẦN NHẤT VÀO DATABASE ---
        String updateSql = "UPDATE users SET last_login = NOW() WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
```

**Dòng 48**:
```java
// Đồng bộ luôn mốc thời gian này vào object user để trả về Client
            user.setLastLoginAt(LocalDateTime.now());
```

**Dòng 473**:
```java
// 🎯 Đã sửa: Hàm nạp tiền sử dụng Connection truyền từ ngoài vào để chạy chung Transaction
    public User deposit(Connection conn, String userId, double amount) throws Exception {
        if (amount <= 0) {
```

**Dòng 489**:
```java
// 🎯 Đã sửa: Hàm rút tiền sử dụng Connection truyền từ ngoài vào để chạy chung Transaction
    public User withdraw(Connection conn, String userId, double amount) throws Exception {
        String balanceSql = "SELECT balance FROM wallet WHERE user_id = ?";
```

**Dòng 499**:
```java
logger.warn("Số dư không đủ để rút! User: {}, Số dư: {}, Cần rút: {}", userId, balance, amount);
                        return null; // Số dư không đủ
                    }
                } else {
```

**Dòng 502**:
```java
} else {
                    return null; // Không thấy ví
                }
            }
```

**Dòng 517**:
```java
// 🎯 Đã sửa dứt điểm: Tách biệt quản lý Connection và quản lý đóng mở TRANSACTION an toàn
    public User approveTransaction(long transactionId) {
        String selectSql = "SELECT * FROM transactions WHERE id = ?";
```

**Dòng 529**:
```java
conn = DBConnection.getConnection();
            // 🌟 ĐỒNG BỘ: Tắt AutoCommit để bọc toàn bộ chuỗi cập nhật số dư + đổi status vào 1 phiên an toàn
            conn.setAutoCommit(false);
```

**Dòng 541**:
```java
// Gọi hàm nạp/rút dùng chung Connection đang tắt autocommit
                User updatedUser = null;
                if ("DEPOSIT".equals(type)) {
```

**Dòng 549**:
```java
// Nếu rút/nạp thất bại (ví dụ không đủ số dư) thì hủy bỏ luôn
                if (updatedUser == null) {
                    conn.rollback();
```

**Dòng 555**:
```java
// Cập nhật trạng thái hóa đơn
                updatePs = conn.prepareStatement(updateSql);
                updatePs.setLong(1, transactionId);
```

**Dòng 560**:
```java
// 🌟 LƯU THAY ĐỔI: Thành công mỹ mãn thì chốt hạ lưu vào ổ đĩa MySQL
                conn.commit();
```

**Dòng 563**:
```java
// Logic gửi thông báo
                String actionStr = "DEPOSIT".equals(type) ? "nạp tiền" : "rút tiền";
                String title = "Giao dịch " + actionStr + " thành công";
```

**Dòng 584**:
```java
} finally {
            // 🌟 KHÔI PHỤC: Trả lại trạng thái mặc định của Connection Pool để không làm hỏng hàm khác
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (Exception ex) { ex.printStackTrace(); }
```

---
### bidding-server\src\main\java\com\uet\server\database\dao\WalletDAO.java
**Dòng 47**:
```java
if (rs.next()) {
                    return rs.getDouble("total_frozen"); // Trả về tổng tiền đang giữ, nếu không có sẽ trả về 0
                }
            }
```

**Dòng 57**:
```java
public double getFrozenBalanceExcludeCurrent(String userId, String excludeAuctionId) {
        // Thêm điều kiện: AND id != ? để không tính tiền bị giam của chính phiên này
        String sql = "SELECT SUM(current_price) AS total_frozen FROM auctions " +
                "WHERE winner_id = ? AND status = 'RUNNING' AND id != ?";
```

**Dòng 65**:
```java
ps.setString(1, userId);
            ps.setString(2, excludeAuctionId); // Loại trừ phiên đang đứng ra

            try (ResultSet rs = ps.executeQuery()) {
```

**Dòng 96**:
```java
double available = total - frozen;
        return available < 0 ? 0.0 : available; // Đảm bảo không bao giờ bị âm do sai số
    }
```

**Dòng 104**:
```java
public boolean updateBalance(String userId, double amount) {
        // Lệnh này cộng/trừ trực tiếp vào số dư gốc trong DB
        String sql = "UPDATE wallet SET balance = balance + ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
```

**Dòng 147**:
```java
// Rút tiền đồng nghĩa với việc cộng một số âm vào tài khoản
        boolean success = updateBalance(userId, -amount);
        if (success) {
```

---
### bidding-server\src\main\java\com\uet\server\network\ClientHandler.java
**Dòng 72**:
```java
try {
            // 1. Chủ động đóng luồng ghi dữ liệu trước
            if (out != null) {
                try { out.close(); } catch (Exception ignored) {}
```

**Dòng 77**:
```java
// 2. Chủ động đóng luồng đọc (Ép in.readObject() văng Exception để thoát vòng lặp)
            if (in != null) {
                try { in.close(); } catch (Exception ignored) {}
```

**Dòng 82**:
```java
// 3. Đóng Socket vật lý
            if (socket != null && !socket.isClosed()) {
                socket.close();
```

**Dòng 90**:
```java
} catch (Exception ignored) {
            // Đúng bài Clean Code, những lỗi đóng tài nguyên này có thể bỏ qua
        } finally {
            // 🌟 BẮT BUỘC ĐỂ Ở ĐÂY: Dù đống đóng Socket ở trên có lỗi hay không,
```

**Dòng 92**:
```java
} finally {
            // 🌟 BẮT BUỘC ĐỂ Ở ĐÂY: Dù đống đóng Socket ở trên có lỗi hay không,
            // thì Client này VẪN PHẢI được xóa khỏi danh sách quản lý để tránh rò rỉ RAM!
            ClientManager.removeClient(this);
```

**Dòng 93**:
```java
// 🌟 BẮT BUỘC ĐỂ Ở ĐÂY: Dù đống đóng Socket ở trên có lỗi hay không,
            // thì Client này VẪN PHẢI được xóa khỏi danh sách quản lý để tránh rò rỉ RAM!
            ClientManager.removeClient(this);
            logger.info("[SERVER] Đã Xóa Client khỏi ClientManager thành công.");
```

---
### bidding-server\src\main\java\com\uet\server\network\ServerMain.java
**Dòng 15**:
```java
public static void main(String[] args){
        // Thiết lập múi giờ mặc định của Server JVM sang Asia/Ho_Chi_Minh (GMT+7)
        // giúp tránh lệch 7 tiếng khi deploy hệ thống Server lên các VPS quốc tế sử dụng múi giờ UTC.
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
```

**Dòng 16**:
```java
// Thiết lập múi giờ mặc định của Server JVM sang Asia/Ho_Chi_Minh (GMT+7)
        // giúp tránh lệch 7 tiếng khi deploy hệ thống Server lên các VPS quốc tế sử dụng múi giờ UTC.
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
```

**Dòng 32**:
```java
// Tạo một luồng (Thread) mới để phục vụ riêng client này
                ClientHandler handler = new ClientHandler(clientSocket);
                new Thread(handler).start();
```

---
### bidding-server\src\main\java\com\uet\server\service\AuctionRealtimeService.java
**Dòng 4**:
```java
import com.uet.common.model.auction.AuctionItem;
import com.uet.common.model.auction.BidRecord; // 🌟 Thêm import để dùng danh sách lịch sử
import com.uet.common.network.*;
import com.uet.server.database.dao.AuctionDAO;
```

**Dòng 46**:
```java
// Chặn người bán tự đấu giá sản phẩm của chính mình
        AuctionItem auctionItem = auctionDAO.getAuctionById(request.getAuctionId(), false);
        if (auctionItem != null && bidderId.equals(auctionItem.getSellerId())) {
```

**Dòng 77**:
```java
// Phát sóng danh sách cập nhật mới nhất cho tất cả Client ở trang chủ
        try {
            List<AuctionItem> activeAuctions = auctionDAO.getActiveAuctions();
```

**Dòng 86**:
```java
public void handleGetPendingAuctions(ClientHandler client) { // Giữ nguyên tên hàm ở Dispatcher đỡ phải sửa
        logger.info("==> Admin đang yêu cầu tải toàn bộ danh sách phiên đấu giá!");
```

**Dòng 90**:
```java
try {
            // 🌟 Lấy HẾT tất cả các phiên thay vì mỗi pending
            List<AuctionItem> allList = auctionDAO.getAllAuctionsForAdmin();
            client.send(Response.success("Tải danh sách chờ duyệt thành công", allList));
```

**Dòng 99**:
```java
// 🌟 Viết thêm hàm ép kết thúc phiên đấu giá đang chạy
    public void handleForceEndAuction(String auctionId, ClientHandler client) {
        try {
```

**Dòng 102**:
```java
try {
            // Cập nhật trạng thái phiên thành FINISHED và thực hiện giao dịch chuyển tiền giữa người mua và người bán
            boolean success = auctionDAO.forceEndAuctionAndProcessTransaction(auctionId);
            if (success) {
```

**Dòng 107**:
```java
// 💡 Realtime: Phát thông báo cho những người đang ở trong phòng biết phiên đã bị đóng
                com.uet.server.network.ClientManager.broadcastAuction(
                        auctionId,
```

**Dòng 113**:
```java
// Phát sóng danh sách cập nhật mới nhất cho tất cả Client ở trang chủ để xóa phiên đấu giá đã đóng
                try {
                    List<AuctionItem> activeAuctions = auctionDAO.getActiveAuctions();
```

**Dòng 133**:
```java
// Lấy thông tin auction để biết sellerId và productName
            AuctionItem item = auctionDAO.getAuctionById(request.getAuctionId(), false);
```

**Dòng 143**:
```java
com.uet.server.database.dao.UserDAO userDAO = new com.uet.server.database.dao.UserDAO();
                    // Xóa thông báo chờ duyệt
                    userDAO.deleteNotificationByKeyword(item.getSellerId(), "Sản phẩm '" + item.getProductName() + "' đang chờ Admin duyệt");
```

**Dòng 146**:
```java
// Tạo thông báo mới
                    String title = request.isApproved() ? "Sản phẩm đã duyệt" : "Sản phẩm bị từ chối";
                    String content = request.isApproved()
```

**Dòng 154**:
```java
// Nếu được phê duyệt, thử kích hoạt phiên đấu giá ngay lập tức nếu đến giờ
                if (request.isApproved()) {
                    try {
```

**Dòng 181**:
```java
// 1. Lấy dữ liệu phiên từ Database lên để kiểm tra điều kiện xóa
            AuctionItem item = auctionDAO.getAuctionById(auctionId);
```

**Dòng 189**:
```java
// 🛡️ Kiểm tra quyền: Chủ sở hữu (Seller) HOẶC Người thắng (Winner) đều có quyền xóa
            boolean isSeller = item.getSellerId() != null && item.getSellerId().equals(userId);
            boolean isWinner = item.getWinnerId() != null && item.getWinnerId().equals(userId);
```

**Dòng 198**:
```java
// 🛡️ Kiểm tra trạng thái:
            // 1. Chặn không cho xóa nếu phiên đấu giá đang diễn ra (RUNNING)
            if ("RUNNING".equalsIgnoreCase(item.getStatus())) {
```

**Dòng 199**:
```java
// 🛡️ Kiểm tra trạng thái:
            // 1. Chặn không cho xóa nếu phiên đấu giá đang diễn ra (RUNNING)
            if ("RUNNING".equalsIgnoreCase(item.getStatus())) {
                client.send(Response.fail("Không thể xóa! Phiên đấu giá đang diễn ra (RUNNING)."));
```

**Dòng 205**:
```java
// 2. Nếu là Seller tự xóa:
            if (isSeller && !isWinner) {
                // Chặn xóa nếu phiên đã kết thúc (FINISHED) và thực sự có người thắng
```

**Dòng 207**:
```java
if (isSeller && !isWinner) {
                // Chặn xóa nếu phiên đã kết thúc (FINISHED) và thực sự có người thắng
                if ("FINISHED".equalsIgnoreCase(item.getStatus())) {
                    if (item.getWinnerId() != null && !item.getWinnerId().trim().isEmpty()) {
```

**Dòng 214**:
```java
} else {
                    // Nếu phiên chưa kết thúc nhưng đã có người tham gia đấu giá (có leader hiện tại) thì cũng chặn
                    if (item.getWinnerId() != null && !item.getWinnerId().trim().isEmpty()) {
                        client.send(Response.fail("Không thể xóa! Phiên đấu giá đã có thành viên đặt giá."));
```

**Dòng 222**:
```java
// 2. Tiến hành xóa dữ liệu trong Database sau khi vượt qua các chốt chặn an toàn
            boolean isDeleted = auctionDAO.deleteAuction(auctionId);
```

**Dòng 226**:
```java
if (isDeleted) {
                // Bắn phản hồi thành công về cho duy nhất Client vừa bấm nút Xóa
                client.send( Response.success("Đã gỡ bỏ sản phẩm và hủy phiên đấu giá thành công!", null));
```

**Dòng 229**:
```java
// Phát sóng danh sách cập nhật mới nhất cho tất cả Client ở trang chủ để cập nhật giao diện realtime
                try {
                    List<AuctionItem> activeAuctions = auctionDAO.getActiveAuctions();
```

---
### bidding-server\src\main\java\com\uet\server\service\AuctionScheduler.java
**Dòng 23**:
```java
// Thiết lập: Cứ mỗi 5 giây (TimeUnit.SECONDS), bộ quét sẽ tự động chạy lại một lần
        scheduler.scheduleAtFixedRate(() -> {
            try {
```

**Dòng 26**:
```java
try {
                // 1. Kiểm tra kích hoạt phiên mới
                int activated = auctionDAO.startEligibleAuctions();
```

**Dòng 29**:
```java
// 2. Kiểm tra đóng phiên hết hạn
                int finished = auctionDAO.finishExpiredAuctions();
```

**Dòng 32**:
```java
// Nếu có bất cứ thay đổi nào, phát sóng danh sách mới đến trang chủ của tất cả Client
                if (activated > 0 || finished > 0) {
                    logger.info("[Scheduler] Phát hiện có thay đổi danh sách phiên đấu giá (Kích hoạt: {}, Kết thúc: {}). Tiến hành Broadcast...", activated, finished);
```

---
### bidding-server\src\main\java\com\uet\server\service\AuctionService.java
**Dòng 24**:
```java
// BẮT CHƯỚC 100% CÁCH LƯU ẢNH CỦA USER_DAO:
            if (req.getProductImage() != null && req.getProductImage().getData() != null && req.getProductImage().getData().length > 0) {
```

**Dòng 27**:
```java
// 1. Sinh tên file duy nhất bằng mã Timestamp chống trùng và chống cache
                String originalName = req.getProductImage().getOriginalFileName();
                String uniqueName = System.currentTimeMillis() + "_" + originalName;
```

**Dòng 32**:
```java
// 2. Gọi fileStorageService.save y hệt như bên UserDAO nhưng lưu vào thư mục "products"
                // Truyền vào: Đối tượng ImageData, Tên thư mục cha, và ID định danh (Dùng SellerId hoặc Tên sản phẩm đều được)
                imageUrl = fileStorageService.save(
```

**Dòng 33**:
```java
// 2. Gọi fileStorageService.save y hệt như bên UserDAO nhưng lưu vào thư mục "products"
                // Truyền vào: Đối tượng ImageData, Tên thư mục cha, và ID định danh (Dùng SellerId hoặc Tên sản phẩm đều được)
                imageUrl = fileStorageService.save(
                        req.getProductImage(),
```

**Dòng 41**:
```java
} else {
                imageUrl = "/images/default_product.png"; // Ảnh mặc định nếu lỗi
            }
```

**Dòng 44**:
```java
// 3. Gọi DAO chèn vào MySQL Database với trạng thái PENDING (Chờ duyệt) như Nam yêu cầu
            boolean isInserted = auctionDAO.createNewAuction(
                    req.getSellerId(),
```

**Dòng 50**:
```java
req.getStartPrice(),
                    imageUrl,         // Chuỗi đường dẫn file:// chuẩn chỉnh vừa lưu đĩa xong
                    req.getItemType(),
                    req.getBrand(),
```

**Dòng 59**:
```java
// 4. Trả phản hồi về cho Client
            if (isInserted) {
                client.send(Response.success("Đăng bán sản phẩm đấu giá thành công! Vui lòng chờ Admin phê duyệt.", null));
```

**Dòng 64**:
```java
// Gửi thông báo
                com.uet.server.database.dao.UserDAO userDAO = new com.uet.server.database.dao.UserDAO();
                userDAO.createNotification(req.getSellerId(), "Chờ duyệt sản phẩm", "Sản phẩm '" + req.getProductName() + "' đang chờ Admin duyệt.");
```

---
### bidding-server\src\main\java\com\uet\server\service\ClientRequestDispatcher.java
**Dòng 28**:
```java
public boolean dispatch(Object obj, ClientHandler client) {
        // 1. Xử lý yêu cầu lấy toàn bộ danh sách người dùng cho Admin
        if (obj instanceof GetAllUsersRequest) {
            handleGetAllUsers(client);
```

**Dòng 34**:
```java
// 2. Xử lý yêu cầu Khóa/Mở khóa tài khoản từ Admin
        if (obj instanceof UpdateUserStatusRequest request) {
            try {
```

**Dòng 47**:
```java
// 3. Xử lý yêu cầu XÓA tài khoản từ Admin gửi lên
        if (obj instanceof DeleteUserRequest request) {
            try {
```

**Dòng 60**:
```java
// 4. Xử lý Đăng nhập
        if (obj instanceof LoginRequest request) {
            handleLogin(request, client);
```

**Dòng 147**:
```java
// 🌟 ĐÃ GỘP: Xử lý chỉnh sửa thông tin phiên đấu giá gửi từ Client dạng AuctionItem
        if (obj instanceof AuctionItem item) {
            handleUpdateAuction(item, client);
```

**Dòng 153**:
```java
// 🌟 ĐÃ GỘP: Xử lý yêu cầu nạp/rút tiền (TransactionRequest) chờ duyệt
        if (obj instanceof TransactionRequest request) {
            try {
```

**Dòng 169**:
```java
// 🌟 ĐÃ GỘP: Tải toàn bộ danh sách giao dịch đang chờ duyệt cho màn hình Admin
        if (obj instanceof GetPendingTransactionRequest) {
            try {
```

**Dòng 250**:
```java
try {
            // 1. Nếu người dùng chọn tải ảnh mới, thực hiện lưu trữ vào đĩa cứng
            if (item.getProductImageBytes() != null && item.getProductImageBytes().length > 0) {
                FileStorageService fileStorageService = new FileStorageService();
```

**Dòng 261**:
```java
// 2. Lưu thay đổi vào Database
            boolean isUpdated = auctionDAO.updateAuction(item);
```

**Dòng 267**:
```java
// 3. Phát sóng danh sách cập nhật mới nhất cho tất cả Client ở trang chủ để xóa/ẩn sản phẩm đang chờ duyệt
                try {
                    List<AuctionItem> activeAuctions = auctionDAO.getActiveAuctions();
```

**Dòng 289**:
```java
try {
            // 1. Gọi DAO cào dữ liệu từ MySQL
            List<com.uet.common.model.notification.Notification> list = userDAO.getNotificationsByUserId(userId);
            logger.info("[Server] Đã tìm thấy {} thông báo trong DB của User: {}", list.size(), userId);
```

**Dòng 293**:
```java
// 2. Phản hồi kết quả về cho Client qua đường Socket
            client.send(Response.success("Tải danh sách thông báo thành công", list));
            logger.info("[Server] Đã bắn gói tin phản hồi thành công về Client.");
```

---
### bidding-server\src\main\java\com\uet\server\service\FileStorageService.java
**Dòng 15**:
```java
"app.upload.dir",
                    System.getenv().getOrDefault("APP_UPLOAD_DIR", "/root/uploads")  // ✅ Đúng path thực tế
            )
    ).toAbsolutePath().normalize();
```

**Dòng 23**:
```java
"APP_UPLOAD_BASE_URL",
                    "file://" + UPLOAD_ROOT.toString()  // ✅ Sẽ trả về file:///root/uploads
            )
    );
```

---
### bidding-server\src\test\java\com\uet\server\database\dao\WalletTest.java
**Dòng 9**:
```java
public void testAvailableBalanceCalculation() {
        double totalBalance = 10000000.0; // 10 triệu
        double frozenBalance = 6000000.0; // Đang giam 6 triệu ở phiên khác
```

**Dòng 10**:
```java
double totalBalance = 10000000.0; // 10 triệu
        double frozenBalance = 6000000.0; // Đang giam 6 triệu ở phiên khác

        double available = totalBalance - frozenBalance;
```

**Dòng 14**:
```java
// Mong đợi số dư khả dụng phải là 4 triệu
        Assertions.assertEquals(4000000.0, available, "Logic tính số dư khả dụng bị sai rồi Nam ơi!");
    }
```
