package com.uet.client.ui;

import com.uet.client.network.ClientSocket;
import com.uet.common.model.auction.AuctionItem;
import com.uet.common.model.auction.BidRecord; // 🌟 Đã import đối tượng lịch sử từ Common
import com.uet.common.model.user.User;
import com.uet.common.network.AuctionUpdateResponse;
import com.uet.common.network.BidRequest;
import com.uet.common.network.JoinAuctionRequest;
import com.uet.common.network.LeaveAuctionRequest;
import com.uet.common.network.GetBidHistoryRequest; // 🌟 Import gói tin xin lịch sử
import com.uet.common.network.Response;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AuctionDetailController {
    private static final Logger logger = LoggerFactory.getLogger(AuctionDetailController.class);

    @FXML private Label userNameLabel;
    @FXML private Label balanceLabel;

    @FXML private ImageView productImage;
    @FXML private Label productNameLabel;
    @FXML private Label sellerLabel;
    @FXML private TextArea descriptionArea;

    @FXML private Label startPriceLabel;
    @FXML private Label currentPriceLabel;
    @FXML private Label leaderLabel;
    @FXML private Label countdownLabel;

    @FXML private TextField bidAmountField;
    @FXML private Label messageLabel;

    @FXML private LineChart<String, Number> priceChart;

    // 🌟 ĐÃ KHAI BÁO BẢNG ĐÚNG FX:ID VÀ KIỂU DỮ LIỆU BID_RECORD
    @FXML private TableView<BidRecord> bidHistoryTable;
    @FXML private TableColumn<BidRecord, String> bidderColumn;
    @FXML private TableColumn<BidRecord, String> amountColumn;
    @FXML private TableColumn<BidRecord, String> timeColumn;

    private final DecimalFormat moneyFormat = new DecimalFormat("#,###");
    private final XYChart.Series<String, Number> priceSeries = new XYChart.Series<>();

    private User currentUser;
    private AuctionItem auctionItem;

    private java.util.function.Consumer<Object> auctionMessageListener;

    private double currentPrice = 25000000;
    private LocalDateTime endTime = LocalDateTime.now().plusMinutes(45);

    @FXML
    public void initialize() {
        priceChart.getData().add(priceSeries);

        // 🌟 BƯỚC CHÍ MẠNG: KẾT NỐI BIẾN CỦA BID_RECORD VÀO CỘT TRÊN GIAO DIỆN ĐỂ HIỆN CHỮ
        bidderColumn.setCellValueFactory(new PropertyValueFactory<>("username"));

        // Định dạng số double thành chuỗi tiền tệ #,###đ hiển thị lên bảng
        amountColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatMoney(cellData.getValue().getBidAmount()))
        );

        // Định dạng hiển thị Giờ:Phút:Giây cho cột thời gian đặt
        timeColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getBidTime() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                return new SimpleStringProperty(cellData.getValue().getBidTime().format(formatter));
            }
            return new SimpleStringProperty("");
        });

        startCountdown();
    }

    public void setData(User user, AuctionItem item) {
        this.currentUser = user;
        this.auctionItem = item;

        userNameLabel.setText(user.getUsername());

        if (user.getBalance() == null) {
            balanceLabel.setText("Số dư: 0đ");
        } else {
            balanceLabel.setText("Số dư: " + formatMoney(user.getBalance().doubleValue()));
        }

        productNameLabel.setText(item.getProductName());

        if (item.getSellerName() == null || item.getSellerName().isBlank()) {
            sellerLabel.setText("Người bán: " + item.getSellerId());
        } else {
            sellerLabel.setText("Người bán: " + item.getSellerName());
        }

        descriptionArea.setText(item.getDescription());

        currentPrice = item.getCurrentPrice();

        if (item.getEndTime() != null) {
            endTime = item.getEndTime();
        }

        startPriceLabel.setText(formatMoney(item.getStartPrice()));
        currentPriceLabel.setText(formatMoney(item.getCurrentPrice()));

        updateLeaderLabel(item);

        if (item.getProductImageBytes() != null && item.getProductImageBytes().length > 0) {
            try {
                javafx.scene.image.Image img = new javafx.scene.image.Image(
                        new java.io.ByteArrayInputStream(item.getProductImageBytes())
                );
                productImage.setImage(img);
            } catch (Exception e) {
                logger.error("Lỗi khi hiển thị ảnh sản phẩm từ bytes: ", e);
            }
        } else if (item.getImageUrl() != null && !item.getImageUrl().isBlank()) {
            try {
                productImage.setImage(new Image(item.getImageUrl(), true));
            } catch (Exception e) {
                logger.error("Không load được ảnh sản phẩm đấu giá từ URL: {}", item.getImageUrl(), e);
            }
        }

        // Logic vẽ biểu đồ gốc ban đầu của Nam giữ nguyên 100%
        addBidHistory("Giá hiện tại", currentPrice);

        joinAuctionRoom();

        // 🌟 LẤY LỊCH SỬ TỪ SERVER ĐỂ ĐỔ VÀO CHO BẢNG HIỂN THỊ LÊN LẦN ĐẦU
        requestBidHistoryFromServer(item.getAuctionId());
    }

    private void joinAuctionRoom() {
        try {
            ClientSocket socket = ClientSocket.getInstance();

            auctionMessageListener = message -> {
                if (message instanceof AuctionUpdateResponse updateResponse) {
                    AuctionItem updatedItem = updateResponse.getAuctionItem();

                    if (updatedItem != null && auctionItem != null && auctionItem.getAuctionId().equals(updatedItem.getAuctionId())) {

                        Platform.runLater(() -> {
                            // 1. Cập nhật thông tin chữ nghĩa giá cả và vẽ biểu đồ cục bộ
                            updateAuctionUI(updatedItem);

                            // 2. 🌟 GỘP CHUNG REALTIME: Bốc luôn danh sách lịch sử đi kèm đổ thẳng vào bảng!
                            if (updateResponse.getBidHistory() != null) {
                                bidHistoryTable.getItems().clear();
                                bidHistoryTable.getItems().addAll(updateResponse.getBidHistory());
                            }

                            // 3. Hiển thị dòng chữ thông báo xanh/vàng nếu có
                            if (updateResponse.getMessage() != null && !updateResponse.getMessage().isBlank()) {
                                showMessage(updateResponse.getMessage(), true);
                            } else {
                                messageLabel.setText(""); // Xóa sạch chữ báo lỗi cũ của lượt trước
                            }
                        });
                    }
                }
            };

            socket.addMessageListener(auctionMessageListener);
            socket.send(new JoinAuctionRequest(auctionItem.getAuctionId()));

            showMessage("Đã tham gia phiên đấu giá.", true);

        } catch (Exception e) {
            logger.error("Không thể tham gia phiên đấu giá: ", e);
            showMessage("Không thể tham gia phiên đấu giá.", false);
        }
    }

    // 🌟 HÀM TẢI LỊCH SỬ CHỈ TÁC ĐỘNG VÀO BẢNG LỊCH SỬ THEO ĐÚNG Ý NAM
    private void requestBidHistoryFromServer(String auctionId) {
        try {
            GetBidHistoryRequest historyReq = new GetBidHistoryRequest(auctionId);

            java.util.function.Consumer<Object> historyListener = new java.util.function.Consumer<>() {
                @Override
                public void accept(Object response) {
                    if (response instanceof Response res && "Tải lịch sử thành công".equals(res.getMessage())) {
                        List<BidRecord> list = (List<BidRecord>) res.getData();

                        Platform.runLater(() -> {
                            // 🌟 CHỈ THÊM VÀO ĐÚNG BẢNG LỊCH SỬ ĐẶT GIÁ THÔI, GIỮ NGUYÊN BIỂU ĐỒ CỦA NAM
                            bidHistoryTable.getItems().clear();
                            bidHistoryTable.getItems().addAll(list);
                        });

                        ClientSocket.getInstance().removeMessageListener(this);
                    }
                }
            };

            ClientSocket.getInstance().addMessageListener(historyListener);
            new Thread(() -> {
                try {
                    ClientSocket.getInstance().send(historyReq);
                } catch (Exception e) {
                    logger.error("Lỗi khi gửi yêu cầu lấy lịch sử đặt giá từ Client: ", e);
                }
            }).start();

        } catch (Exception e) {
            logger.error("Lỗi khi gửi yêu cầu lấy lịch sử đặt giá lên Server: ", e);
        }
    }

    private void updateAuctionUI(AuctionItem updatedItem) {
        this.auctionItem = updatedItem;
        this.currentPrice = updatedItem.getCurrentPrice();

        if (updatedItem.getEndTime() != null) {
            this.endTime = updatedItem.getEndTime();
        }

        productNameLabel.setText(updatedItem.getProductName());
        descriptionArea.setText(updatedItem.getDescription());

        if (updatedItem.getSellerName() == null || updatedItem.getSellerName().isBlank()) {
            sellerLabel.setText("Người bán: " + updatedItem.getSellerId());
        } else {
            sellerLabel.setText("Người bán: " + updatedItem.getSellerName());
        }

        startPriceLabel.setText(formatMoney(updatedItem.getStartPrice()));
        currentPriceLabel.setText(formatMoney(updatedItem.getCurrentPrice()));

        updateLeaderLabel(updatedItem);

        addBidHistory("Cập nhật", updatedItem.getCurrentPrice());
    }

    private void updateLeaderLabel(AuctionItem item) {
        if (item.getWinnerName() != null && !item.getWinnerName().isBlank()) {
            leaderLabel.setText(item.getWinnerName());
        } else if (item.getWinnerId() != null && !item.getWinnerId().isBlank()) {
            leaderLabel.setText(item.getWinnerId());
        } else {
            leaderLabel.setText("Chưa có");
        }
    }

    @FXML
    private void handlePlaceBid() {
        messageLabel.setText("");
        String text = bidAmountField.getText().trim();

        if (text.isEmpty()) {
            showMessage("Vui lòng nhập số tiền muốn đặt!", false);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(text);
        } catch (NumberFormatException e) {
            showMessage("Số tiền nhập vào không hợp lệ (chỉ được nhập số)!", false);
            return;
        }

        // 🌟 ĐỔI CÂU NÀY: Thông báo rõ ràng cho người dùng
        if (amount <= currentPrice) {
            showMessage("Giá đặt mới phải LỚN HƠN giá hiện tại (" + formatMoney(currentPrice) + ")!", false);
            return;
        }

        if (currentUser == null || currentUser.getBalance() == null) {
            showMessage("Lỗi: Không lấy được số dư tài khoản của bạn!", false);
            return;
        }

        // 🌟 ĐỔI CÂU NÀY: Thông báo khi tài khoản hết tiền
        if (amount > currentUser.getBalance().doubleValue()) {
            showMessage("Số dư tài khoản không đủ để thực hiện lượt đặt giá này!", false);
            return;
        }

        if (auctionItem == null || auctionItem.getAuctionId() == null) {
            showMessage("Không lấy được thông tin phiên đấu giá.", false);
            return;
        }

        try {
            BidRequest request = new BidRequest(
                    auctionItem.getAuctionId(),
                    currentUser.getId(),
                    amount
            );

            ClientSocket.getInstance().send(request);

            bidAmountField.clear();
            showMessage("Đã gửi yêu cầu đặt giá.", true);

        } catch (Exception e) {
            logger.error("Không gửi được giá đặt lên server: ", e);
            showMessage("Không gửi được giá đặt lên server.", false);
        }
    }

    private void addBidHistory(String bidder, double amount) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        priceSeries.getData().add(new XYChart.Data<>(time, amount));
        if (priceSeries.getData().size() > 10) {
            priceSeries.getData().remove(0);
        }
    }

    private void startCountdown() {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> updateCountdown())
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void updateCountdown() {
        LocalDateTime now = LocalDateTime.now();

        if (now.isAfter(endTime)) {
            countdownLabel.setText("Đã kết thúc");
            bidAmountField.setDisable(true);
            return;
        }

        long seconds = java.time.Duration.between(now, endTime).getSeconds();

        long hour = seconds / 3600;
        long minute = (seconds % 3600) / 60;
        long second = seconds % 60;

        countdownLabel.setText(String.format("%02d:%02d:%02d", hour, minute, second));
    }

    @FXML
    private void handleBack() {
        try {
            if (auctionMessageListener != null) {
                ClientSocket.getInstance().removeMessageListener(auctionMessageListener);
                auctionMessageListener = null;
            }

            if (auctionItem != null && auctionItem.getAuctionId() != null) {
                ClientSocket.getInstance().send(new LeaveAuctionRequest(auctionItem.getAuctionId()));
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/home_view.fxml"));
            Parent root = loader.load();

            HomeController controller = loader.getController();
            controller.setUser(currentUser);

            // Áp dụng hiệu ứng mượt mà khi quay lại
            com.uet.client.util.TransitionUtils.applyFadeIn(root);

            Stage stage = (Stage) productNameLabel.getScene().getWindow();

            stage.getScene().setRoot(root);
            stage.setTitle("Trang chủ Đấu giá");

        } catch (Exception e) {
            logger.error("Không quay lại được trang chủ: ", e);
            showMessage("Không quay lại được trang chủ.", false);
        }
    }

    private void showMessage(String message, boolean success) {
        messageLabel.setText(message);

        if (success) {
            messageLabel.setStyle("-fx-text-fill: #16A34A;");
        } else {
            messageLabel.setStyle("-fx-text-fill: #DC2626;");
        }
    }

    private String formatMoney(double money) {
        return moneyFormat.format(money) + "đ";
    }
}