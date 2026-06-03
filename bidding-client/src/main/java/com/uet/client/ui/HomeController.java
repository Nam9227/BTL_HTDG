package com.uet.client.ui;

import com.uet.client.network.ClientSocket;
import com.uet.common.model.auction.AuctionItem;
import com.uet.common.model.user.Role;
import com.uet.common.model.user.User;
import com.uet.common.network.GetActiveAuctionsRequest;
import com.uet.common.network.GetActiveAuctionsResponse;
import com.uet.common.network.UpdateRoleRequest;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javafx.scene.image.ImageView;
import java.math.BigDecimal;
import java.util.List;
import com.uet.client.util.ThreadPoolManager;
import com.uet.client.util.TransitionUtils;

public class HomeController {
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @FXML
    private VBox sideContent; 
    @FXML
    private Button openBtn; 
    @FXML
    private Button closeBtn; 

    @FXML
    private Button addProductBtn;
    @FXML
    private Button myProductsBtn;
    @FXML
    private Label userNameLabel;
    @FXML
    private Label balanceLabel;
    @FXML
    private FlowPane productContainer;
    @FXML
    private ImageView userAvatar;
    @FXML
    private TextField searchField;
    private User currentUser;
    private List<AuctionItem> allActiveAuctions = new java.util.ArrayList<>();
    private java.util.function.Consumer<Object> homeListener;

    public void setUser(User user) {
        this.currentUser = user;

        
        userNameLabel.setText(user.getFullName() != null ? user.getFullName() : user.getUsername());
        balanceLabel.setText("Số dư: " + formatMoney(user.getBalance()));

        ClientSocket.onUserUpdated = updatedUser -> {
            if (this.currentUser != null && this.currentUser.getId().equals(updatedUser.getId())) {
                javafx.application.Platform.runLater(() -> setUser(updatedUser));
            }
        };

        
        
        if (user.getAvatarBytes() != null && user.getAvatarBytes().length > 0) {
            try {
                userAvatar.setImage(null); 
                javafx.scene.image.Image img = new javafx.scene.image.Image(
                        new java.io.ByteArrayInputStream(user.getAvatarBytes()));
                userAvatar.setImage(img);
            } catch (Exception e) {
                logger.error("Lỗi hiển thị avatar tại Sidebar Home: ", e);
            }
        }

        if (user.getRole() == null) {
            showChooseRoleDialog();
        } else {
            applyRoleUI();
            loadActiveAuctions();
        }
    }

    private String formatMoney(BigDecimal amount) {
        if (amount == null)
            return "0 đ";
        return String.format("%,.0f đ", amount);
    }

    private void showChooseRoleDialog() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Chọn vai trò");
        alert.setHeaderText("Bạn muốn sử dụng hệ thống với vai trò nào?");
        alert.setContentText("Chỉ cần chọn một lần để bắt đầu sử dụng hệ thống.");

        ButtonType bidderBtn = new ButtonType("Người đấu giá");
        ButtonType sellerBtn = new ButtonType("Người bán hàng");

        alert.getButtonTypes().setAll(bidderBtn, sellerBtn);

        DialogPane pane = alert.getDialogPane();
        pane.setPrefWidth(420);

        pane.getStylesheets().add(
                getClass().getResource("/style/choose_role_dialog.css").toExternalForm());

        Button bidderButton = (Button) pane.lookupButton(bidderBtn);
        bidderButton.getStyleClass().add("bidder-button");

        Button sellerButton = (Button) pane.lookupButton(sellerBtn);
        sellerButton.getStyleClass().add("seller-button");

        alert.showAndWait().ifPresent(result -> {
            if (result == bidderBtn) {
                currentUser.setRole(Role.BIDDER);
                saveRoleToServer(Role.BIDDER);
            } else if (result == sellerBtn) {
                currentUser.setRole(Role.SELLER);
                saveRoleToServer(Role.SELLER);
            }

            applyRoleUI();
            loadActiveAuctions();
        });
    }

    private void saveRoleToServer(Role role) {
        try {
            UpdateRoleRequest request = new UpdateRoleRequest(
                    currentUser.getId(),
                    role);

            ClientSocket.getInstance().send(request);

        } catch (Exception e) {
            logger.error("Không lưu được vai trò: ", e);
            showError("Lỗi", "Không lưu được vai trò!");
        }
    }

    private void applyRoleUI() {
        if (currentUser.getRole() == Role.BIDDER) {
            logger.info("Chọn giao diện vai trò: Người đấu giá");
            productContainer.setVisible(true);
            productContainer.setManaged(true);

            
            addProductBtn.setVisible(false);
            addProductBtn.setManaged(false);

        } else if (currentUser.getRole() == Role.SELLER) {
            logger.info("Chọn giao diện vai trò: Người bán hàng");

            
            addProductBtn.setVisible(true);
            addProductBtn.setManaged(true);

            
            productContainer.setVisible(true);
            productContainer.setManaged(true);
        }
    }

    @FXML
    public void initialize() {
        productContainer.getStylesheets().add(
                getClass().getResource("/style/home.css").toExternalForm());
        productContainer.getStyleClass().add("root-container");
        productContainer.setHgap(20);
        productContainer.setVgap(20);

        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterAuctions(newValue);
            });
        }

        
        
        sideContent.setTranslateX(300);
        sideContent.setVisible(false);
        sideContent.setManaged(false);
        closeBtn.setVisible(false);
    }

    private void loadActiveAuctions() {
        try {
            ClientSocket socket = ClientSocket.getInstance();

            cleanupListener();

            homeListener = new java.util.function.Consumer<>() {
                @Override
                public void accept(Object response) {
                    if (response instanceof GetActiveAuctionsResponse auctionResponse) {
                        List<AuctionItem> auctions = auctionResponse.getAuctions();

                        logger.info("Home nhận danh sách đấu giá: {}", auctions.size());

                        Platform.runLater(() -> {
                            allActiveAuctions = auctions;
                            filterAuctions(searchField != null ? searchField.getText() : "");
                        });
                    }
                }
            };

            socket.addMessageListener(homeListener);
            ThreadPoolManager.execute(() -> {
                try {
                    socket.send(new GetActiveAuctionsRequest());
                } catch (Exception e) {
                    logger.error("Lỗi khi gửi yêu cầu danh sách đấu giá từ Home: ", e);
                }
            });

        } catch (Exception e) {
            logger.error("Không lấy được danh sách sản phẩm đấu giá: ", e);

            Platform.runLater(() -> showError("Lỗi", "Không lấy được danh sách sản phẩm đấu giá!"));
        }
    }

    private void renderAuctions(List<AuctionItem> auctions) {
        productContainer.getChildren().clear();

        if (auctions == null || auctions.isEmpty()) {
            Label emptyLabel = new Label("Chưa có sản phẩm đấu giá nào.");
            emptyLabel.setStyle("-fx-font-family: 'Segoe UI'; -fx-text-fill: #64748b; -fx-font-size: 14px;");
            productContainer.getChildren().add(emptyLabel);
            return;
        }

        for (AuctionItem item : auctions) {
            
            VBox card = new VBox(10);
            card.getStyleClass().add("product-card-node"); 
            card.setPrefWidth(210);
            card.setPadding(new Insets(15));

            
            ImageView cardImageView = new ImageView();
            cardImageView.setFitWidth(180);
            cardImageView.setFitHeight(130);
            cardImageView.setPreserveRatio(true);
            cardImageView.getStyleClass().add("product-image-container");

            if (item.getProductImageBytes() != null && item.getProductImageBytes().length > 0) {
                try {
                    javafx.scene.image.Image img = new javafx.scene.image.Image(
                            new java.io.ByteArrayInputStream(item.getProductImageBytes()));
                    cardImageView.setImage(img);
                } catch (Exception e) {
                    logger.error("Lỗi khi vẽ ảnh preview từ bytes: ", e);
                }
            } else if (item.getImageUrl() != null && !item.getImageUrl().isBlank()) {
                try {
                    cardImageView.setImage(new javafx.scene.image.Image(item.getImageUrl(), true));
                } catch (Exception e) {
                    logger.error("Lỗi khi vẽ ảnh preview từ URL: ", e);
                }
            }

            
            Label nameLabel = new Label(item.getProductName());
            nameLabel.getStyleClass().add("product-title-label"); 
            nameLabel.setWrapText(true);

            
            Label priceTitle = new Label("Giá hiện tại");
            priceTitle.setStyle("-fx-font-family: 'Segoe UI'; -fx-text-fill: #64748b; -fx-font-size: 12px;");

            Label currentPriceLabel = new Label(String.format("%,.0f đ", item.getCurrentPrice()));
            currentPriceLabel.getStyleClass().add("product-price-label"); 

            VBox priceBox = new VBox(2, priceTitle, currentPriceLabel);

            
            Label endTimeLabel = new Label("Kết thúc: " + item.getEndTime());
            endTimeLabel.setStyle("-fx-font-family: 'Segoe UI'; -fx-text-fill: #94a3b8; -fx-font-size: 11px;");

            
            Button bidButton = new Button("🔨 Đấu giá ngay");
            bidButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-weight: bold; -fx-background-radius: 20; -fx-cursor: hand;");
            bidButton.setMaxWidth(Double.MAX_VALUE);
            bidButton.setPadding(new Insets(8, 0, 8, 0));

            bidButton.setOnAction(e -> openAuctionDetail(item));

            
            card.getChildren().addAll(
                    cardImageView,
                    nameLabel,
                    priceBox,
                    endTimeLabel,
                    bidButton
            );

            
            productContainer.getChildren().add(card);
        }
    }

    private void openAuctionDetail(AuctionItem item) {
        try {
            cleanupListener();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/auction_detail.fxml"));

            Parent root = loader.load();

            AuctionDetailController controller = loader.getController();
            controller.setData(currentUser, item);

            
            TransitionUtils.applyFadeIn(root);

            Stage stage = (Stage) productContainer.getScene().getWindow();
            stage.getScene().setRoot(root); 
            stage.setTitle("Chi tiết phiên đấu giá - " + item.getProductName());

        } catch (Exception e) {
            logger.error("Không mở được trang chi tiết đấu giá: ", e);
            showError("Lỗi", "Không mở được trang chi tiết đấu giá!");
        }

    }

    @FXML
    public void handleOpenSidebar() {
        sideContent.setVisible(true);
        sideContent.setManaged(true);
        closeBtn.setVisible(true);

        
        TranslateTransition menuSlide = new TranslateTransition(Duration.millis(300), sideContent);
        menuSlide.setToX(0);

        
        openBtn.setVisible(false);

        menuSlide.play();
    }

    @FXML
    public void handleCloseSidebar() {
        double width = sideContent.getWidth();
        TranslateTransition menuSlide = new TranslateTransition(Duration.millis(300), sideContent);

        
        menuSlide.setToX(width);

        menuSlide.setOnFinished(e -> {
            sideContent.setVisible(false);
            sideContent.setManaged(false);
            closeBtn.setVisible(false);
            
            openBtn.setVisible(true);
        });

        menuSlide.play();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void handleOpenProfile() {
        try {
            cleanupListener();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/profile_view.fxml"));
            Parent root = loader.load();

            ProfileController controller = loader.getController();
            controller.setUser(currentUser); 

            Stage stage = (Stage) userNameLabel.getScene().getWindow(); 

            
            TransitionUtils.applyFadeIn(root);

            
            
            stage.getScene().setRoot(root);
            stage.setTitle("Thông tin tài khoản");

        } catch (Exception e) {
            logger.error("Không thể mở trang thông tin tài khoản: ", e);
            showError("Lỗi", "Không thể mở trang thông tin tài khoản!");
        }
    }

    @FXML
    public void handleOpenAddProduct() {
        try {
            cleanupListener();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/informationUpload_view.fxml"));
            Parent root = loader.load();

            
            InformationUploadController controller = loader.getController();
            controller.setUser(currentUser); 

            
            TransitionUtils.applyFadeIn(root);

            Stage stage = (Stage) productContainer.getScene().getWindow();
            stage.getScene().setRoot(root); 
            stage.setTitle("Đăng bán sản phẩm mới");

        } catch (Exception e) {
            logger.error("Không mở được giao diện thêm sản phẩm: ", e);
            showError("Lỗi", "Không mở được giao diện thêm sản phẩm!");
        }
    }

    @FXML
    public void handleOpenMyProducts() {
        try {
            cleanupListener();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/my_products.fxml"));
            Parent root = loader.load();

            MyProductsController controller = loader.getController();
            controller.setUser(currentUser);

            TransitionUtils.applyFadeIn(root);

            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle("Sản phẩm của tôi");
        } catch (Exception e) {
            logger.error("Không mở được trang sản phẩm của tôi: ", e);
            showError("Lỗi", "Không mở được trang sản phẩm của tôi!");
        }
    }

    @FXML
    public void handleLogout() {
        try {
            cleanupListener();
            
            ClientSocket.getInstance().send("LOGOUT");

            
            
            ClientSocket.getInstance().close();

            this.currentUser = null;

            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login_view.fxml"));
            Parent root = loader.load();

            
            TransitionUtils.applyFadeIn(root);

            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle("Đăng nhập hệ thống");

            stage.setMaximized(false);
            stage.setWidth(800);
            stage.setHeight(600);
            stage.centerOnScreen();

        } catch (Exception e) {
            logger.error("Lỗi xảy ra khi đăng xuất: ", e);
        }
    }

    private void cleanupListener() {
        if (homeListener != null) {
            try {
                ClientSocket.getInstance().removeMessageListener(homeListener);
            } catch (Exception e) {
                logger.error("Lỗi khi hủy đăng ký listener: ", e);
            }
            homeListener = null;
        }
    }

    private void filterAuctions(String query) {
        if (query == null || query.isBlank()) {
            renderAuctions(allActiveAuctions);
            return;
        }

        String lowerQuery = query.toLowerCase().trim();
        List<AuctionItem> filtered = new java.util.ArrayList<>();
        for (AuctionItem item : allActiveAuctions) {
            boolean matchesName = item.getProductName() != null && item.getProductName().toLowerCase().contains(lowerQuery);
            boolean matchesDesc = item.getDescription() != null && item.getDescription().toLowerCase().contains(lowerQuery);
            if (matchesName || matchesDesc) {
                filtered.add(item);
            }
        }
        renderAuctions(filtered);
    }
    @FXML
    private void handleOpenNotifications() {
        try {
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/notifications.fxml"));
            Parent root = loader.load();

            
            NotificationController controller = loader.getController();
            controller.setUser(currentUser); 

            
            if (TransitionUtils.class != null) {
                TransitionUtils.applyFadeIn(root);
            }

            
            Stage stage = (Stage) productContainer.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle("Hộp Thư Thông Báo - Sàn Đấu Giá UET");

        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(HomeController.class).error("Lỗi khi mở trang thông báo: ", e);
        }
    }
    @FXML
    public void handleSearch() {
        filterAuctions(searchField != null ? searchField.getText() : "");
    }
}
