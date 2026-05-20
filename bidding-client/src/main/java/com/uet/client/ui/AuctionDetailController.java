package com.uet.client.ui;

import com.uet.client.network.ClientSocket;
import com.uet.common.model.auction.AuctionItem;
import com.uet.common.model.user.User;
import com.uet.common.network.AuctionUpdateResponse;
import com.uet.common.network.BidRequest;
import com.uet.common.network.JoinAuctionRequest;
import com.uet.common.network.LeaveAuctionRequest;
import com.uet.common.network.Response;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuctionDetailController {

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

    //@FXML private TableView<BidRow> bidHistoryTable;
    //@FXML private TableColumn<BidRow, String> bidderColumn;
    //@FXML private TableColumn<BidRow, String> amountColumn;
    //@FXML private TableColumn<BidRow, String> timeColumn;

    private final DecimalFormat moneyFormat = new DecimalFormat("#,###");
    private final XYChart.Series<String, Number> priceSeries = new XYChart.Series<>();

    private User currentUser;
    private AuctionItem auctionItem;

    private java.util.function.Consumer<Object> auctionMessageListener;

    private double currentPrice = 25000000;
    private LocalDateTime endTime = LocalDateTime.now().plusMinutes(45);

    @FXML
    public void initialize() {

        priceChart.getStylesheets().add(
                "data:text/css," +
                        /* 1. Đổi màu đường vẽ đồ thị sang màu tím Indigo (màu nút Đặt giá của Nam) và làm mỏng lại */
                        ".chart-series-line { -fx-stroke: #4F46E5; -fx-stroke-width: 2px; }" +
                        /* 2. Thu nhỏ chấm tròn to tướng thành điểm nếp nhỏ tinh tế */
                        ".chart-line-symbol { -fx-background-color: #4F46E5, white; -fx-background-radius: 2.5px; -fx-padding: 2.5px; }" +
                        /* 3. Đẩy nhẹ chữ thời gian xuống dưới một chút cho thoáng mắt */
                        ".axis-tick-label { -fx-translate-y: 5px; }"
        );

        priceChart.getData().add(priceSeries);
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

        if (item.getImageUrl() != null && !item.getImageUrl().isBlank()) {
            try {
                productImage.setImage(new Image(item.getImageUrl()));
            } catch (Exception e) {
                System.out.println("Không load được ảnh: " + item.getImageUrl());
            }
        }

        addBidHistory("Giá hiện tại", currentPrice);

        joinAuctionRoom();
    }

    private void joinAuctionRoom() {
        try {
            ClientSocket socket = ClientSocket.getInstance();

            auctionMessageListener = message -> {
                if (message instanceof AuctionUpdateResponse updateResponse) {
                    AuctionItem updatedItem = updateResponse.getAuctionItem();

                    if (updatedItem != null
                            && auctionItem != null
                            && auctionItem.getAuctionId().equals(updatedItem.getAuctionId())) {

                        Platform.runLater(() -> {
                            updateAuctionUI(updatedItem);

                            if (updateResponse.getMessage() != null
                                    && !updateResponse.getMessage().isBlank()) {
                                showMessage(updateResponse.getMessage(), true);
                            }
                        });
                    }

                } else if (message instanceof Response responseMessage) {
                    Platform.runLater(() ->
                            showMessage(responseMessage.getMessage(), responseMessage.isSuccess())
                    );
                }
            };

            socket.addMessageListener(auctionMessageListener);
            socket.send(new JoinAuctionRequest(auctionItem.getAuctionId()));

            showMessage("Đã tham gia phiên đấu giá.", true);

        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Không thể tham gia phiên đấu giá.", false);
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
        String text = bidAmountField.getText().trim();

        if (text.isEmpty()) {
            showMessage("Vui lòng nhập giá muốn đặt.", false);
            return;
        }

        double amount;

        try {
            amount = Double.parseDouble(text);
        } catch (NumberFormatException e) {
            showMessage("Giá đặt không hợp lệ.", false);
            return;
        }

        if (amount <= currentPrice) {
            showMessage("Giá đặt phải cao hơn giá hiện tại.", false);
            return;
        }

        if (currentUser == null || currentUser.getBalance() == null) {
            showMessage("Không lấy được thông tin số dư tài khoản.", false);
            return;
        }

        if (amount > currentUser.getBalance().doubleValue()) {
            showMessage("Số dư không đủ để đặt giá này.", false);
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
            e.printStackTrace();
            showMessage("Không gửi được giá đặt lên server.", false);
        }
    }

    private void addBidHistory(String bidder, double amount) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        // bidHistoryTable.getItems().add(
        //         0,
        //         new BidRow(bidder, formatMoney(amount), time)
        // );

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

            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/view/home_view.fxml")
            );

            javafx.scene.Parent root = loader.load();

            HomeController controller = loader.getController();
            controller.setUser(currentUser);

            javafx.stage.Stage stage = (javafx.stage.Stage) productNameLabel.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setResizable(true);
            stage.show();

            javafx.application.Platform.runLater(() -> {
                stage.setMaximized(false);
                stage.setMaximized(true);
            });

        } catch (Exception e) {
            e.printStackTrace();
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