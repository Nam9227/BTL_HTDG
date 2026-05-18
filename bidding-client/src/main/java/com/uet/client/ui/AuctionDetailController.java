package com.uet.client.ui;

import com.uet.common.model.auction.AuctionItem;
import com.uet.common.model.user.User;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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

    private double currentPrice = 25000000;
    private LocalDateTime endTime = LocalDateTime.now().plusMinutes(45);

    @FXML
    public void initialize() {
        //bidderColumn.setCellValueFactory(data -> data.getValue().bidderProperty());
        //amountColumn.setCellValueFactory(data -> data.getValue().amountProperty());
        //timeColumn.setCellValueFactory(data -> data.getValue().timeProperty());
        priceChart.getData().add(priceSeries);
        startCountdown();
    }

    public void setData(User user, AuctionItem item) {
        this.currentUser = user;
        this.auctionItem = item;

        userNameLabel.setText(user.getUsername());
        balanceLabel.setText("Số dư: " + formatMoney(user.getBalance().doubleValue()));

        productNameLabel.setText(item.getProductName());
        sellerLabel.setText("Người bán: " + item.getSellerName());
        descriptionArea.setText(item.getDescription());

        currentPrice = item.getCurrentPrice();
        endTime = item.getEndTime();

        startPriceLabel.setText(formatMoney(item.getStartPrice()));
        currentPriceLabel.setText(formatMoney(item.getCurrentPrice()));

        if (item.getWinnerName() == null || item.getWinnerName().isBlank()) {
            leaderLabel.setText("Chưa có");
        } else {
            leaderLabel.setText(item.getWinnerName());
        }

        if (item.getImageUrl() != null && !item.getImageUrl().isBlank()) {
            try {
                productImage.setImage(new Image(item.getImageUrl()));
            } catch (Exception e) {
                System.out.println("Không load được ảnh: " + item.getImageUrl());
            }
        }

        addBidHistory("Giá hiện tại", currentPrice);
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

        currentPrice = amount;
        currentPriceLabel.setText(formatMoney(currentPrice));
        leaderLabel.setText(userNameLabel.getText());

        addBidHistory(userNameLabel.getText(), currentPrice);

        bidAmountField.clear();
        showMessage("Đặt giá thành công.", true);

        // Sau này thay đoạn trên bằng gửi socket:
        // ClientSocket.getInstance().send(new PlaceBidRequest(auctionId, userId, amount));
    }

    private void addBidHistory(String bidder, double amount) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

       // bidHistoryTable.getItems().add(
       //         0,
       //         new BidRow(bidder, formatMoney(amount), time)
       // );

        priceSeries.getData().add(new XYChart.Data<>(time, amount));
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