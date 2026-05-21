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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
// ... existing code ...

import java.math.BigDecimal;
import java.util.List;;

public class HomeController{

        @FXML private VBox sideContent; // Sidebar màu xanh
        @FXML private Button openBtn;   // Nút 3 gạch (nằm ngoài sidebar)
        @FXML private Button closeBtn;  // Nút X (nằm trong sidebar)

        @FXML private Button addProductBtn;
        @FXML private Label userNameLabel;
        @FXML private Label balanceLabel;
        @FXML private FlowPane productContainer;
        private User currentUser;

        public void setUser(User user) {
            this.currentUser = user;

            userNameLabel.setText(user.getUsername());
            balanceLabel.setText("Số dư: " + formatMoney(user.getBalance()));

            if (user.getRole() == null) {
                showChooseRoleDialog();
            } else {
                applyRoleUI();
                loadActiveAuctions();
                }
            }
        private String formatMoney(BigDecimal amount) {
            if (amount==null)
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
                    getClass().getResource("/style/choose_role_dialog.css").toExternalForm()
            );

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
                        role
                );

                ClientSocket.getInstance().send(request);

            } catch (Exception e) {
                e.printStackTrace();
                showError("Lỗi", "Không lưu được vai trò!");
            }
        }

        private void applyRoleUI() {
            if (currentUser.getRole() == Role.BIDDER) {
                System.out.println("Người đấu giá");
                productContainer.setVisible(true);
                productContainer.setManaged(true);

                // Nếu là Người mua (Bidder): Ẩn hoàn toàn nút thêm sản phẩm đi
                addProductBtn.setVisible(false);
                addProductBtn.setManaged(false);

            } else if (currentUser.getRole() == Role.SELLER) {
                System.out.println("Người bán hàng");

                // Nếu là Người bán (Seller): Hiện nút Thêm sản phẩm lên ngay!
                addProductBtn.setVisible(true);
                addProductBtn.setManaged(true);

                // Tùy chọn: Người bán vẫn có thể xem danh sách sản phẩm chung
                productContainer.setVisible(true);
                productContainer.setManaged(true);
            }
        }

        @FXML
        public void initialize() {
            productContainer.getStylesheets().add(
                    getClass().getResource("/style/auction-card.css").toExternalForm()
            );
            productContainer.getStyleClass().add("root-container");
            productContainer.setHgap(20);
            productContainer.setVgap(20);
            // Ban đầu ẩn Sidebar và nút X đi
            // Giả sử chiều rộng sidebar là 300
            sideContent.setTranslateX(300);
            sideContent.setVisible(false);
            sideContent.setManaged(false);
            closeBtn.setVisible(false);
        }
        private void loadActiveAuctions() {
            try {
                ClientSocket socket = ClientSocket.getInstance();

                java.util.function.Consumer<Object> homeListener = new java.util.function.Consumer<>() {
                    @Override
                    public void accept(Object response) {
                        if (response instanceof GetActiveAuctionsResponse auctionResponse) {
                            List<AuctionItem> auctions = auctionResponse.getAuctions();

                            System.out.println("Home nhận danh sách đấu giá: " + auctions.size());

                            Platform.runLater(() -> renderAuctions(auctions));

                            ClientSocket.getInstance().removeMessageListener(this);
                        }
                    }
                };

                socket.addMessageListener(homeListener);
                socket.send(new GetActiveAuctionsRequest());

            } catch (Exception e) {
                e.printStackTrace();

                Platform.runLater(() ->
                        showError("Lỗi", "Không lấy được danh sách sản phẩm đấu giá!")
                );
            }
        }

        private void renderAuctions(List<AuctionItem> auctions) {

            productContainer.getChildren().clear();

            if (auctions == null || auctions.isEmpty()) {
                Label emptyLabel = new Label("Chưa có sản phẩm đấu giá nào.");
                productContainer.getChildren().add(emptyLabel);
                return;
            }

            for (AuctionItem item : auctions) {

                VBox card = new VBox(12);
                card.getStyleClass().add("auction-card");

                Label nameLabel = new Label(item.getProductName());
                nameLabel.getStyleClass().add("product-name");

                Label descriptionLabel = new Label(item.getDescription());
                descriptionLabel.setWrapText(true);
                descriptionLabel.getStyleClass().add("product-description");

                Label priceTitle = new Label("Giá hiện tại");
                priceTitle.getStyleClass().add("price-title");

                Label currentPriceLabel = new Label(
                        String.format("%,.0f đ", item.getCurrentPrice())
                );
                currentPriceLabel.getStyleClass().add("current-price");

                VBox priceBox = new VBox(2, priceTitle, currentPriceLabel);

                Label endTimeLabel = new Label(
                        "Kết thúc: " + item.getEndTime()
                );
                endTimeLabel.getStyleClass().add("end-time");

                Button bidButton = new Button("Đấu giá ngay");
                bidButton.getStyleClass().add("bid-button");

                bidButton.setMaxWidth(Double.MAX_VALUE);

                bidButton.setOnAction(e -> openAuctionDetail(item));

                card.getChildren().addAll(
                        nameLabel,
                        descriptionLabel,
                        priceBox,
                        endTimeLabel,
                        bidButton
                );

                productContainer.getChildren().add(card);
            }
        }
        private void openAuctionDetail(AuctionItem item) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/view/auction_detail.fxml")
                );

                Parent root = loader.load();

                AuctionDetailController controller = loader.getController();
                controller.setData(currentUser, item);

                Stage stage = (Stage) productContainer.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setResizable(true);
                stage.show();

                Platform.runLater(() -> {
                    stage.setMaximized(false);
                    stage.setMaximized(true);
                });

                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Lỗi", "Không mở được trang chi tiết đấu giá!");
                }

        }

        @FXML
        public void handleOpenSidebar() {
            sideContent.setVisible(true);
            sideContent.setManaged(true);
            closeBtn.setVisible(true);

            // Hiệu ứng đẩy Sidebar vào từ phải sang trái
            TranslateTransition menuSlide = new TranslateTransition(Duration.millis(300), sideContent);
            menuSlide.setToX(0);

            // Khi mở ra thì ẩn nút Menu đi ngay lập tức hoặc sau animation
            openBtn.setVisible(false);

            menuSlide.play();
        }

        @FXML
        public void handleCloseSidebar() {
            double width = sideContent.getWidth();
            TranslateTransition menuSlide = new TranslateTransition(Duration.millis(300), sideContent);

            // Đẩy sidebar ra ngoài
            menuSlide.setToX(width);

            menuSlide.setOnFinished(e -> {
                sideContent.setVisible(false);
                sideContent.setManaged(false);
                closeBtn.setVisible(false);
                // Hiện lại nút Menu ban đầu
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
                // 1. Tải file giao diện profile.fxml
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/view/profile_view.fxml") // Nam nhớ check đúng đường dẫn file fxml của Nam nhé
                );
                Parent root = loader.load();

                // 2. Lấy Controller của trang Profile và bắn currentUser sang để nó hiển thị thông tin
                ProfileController controller = loader.getController();
                controller.setUser(currentUser); // Truyền user đăng nhập sang ở đây!

                // 3. Lấy Stage hiện tại và đổi Scene sang trang Profile
                Stage stage = (Stage) productContainer.getScene().getWindow();
                stage.setScene(new Scene(root));

                // 4. Ép màn hình hiển thị bung lụa full toàn màn hình cho đẹp
                stage.setMaximized(true);
                stage.show();

                // Mẹo bảo hiểm của JavaFX giúp giao diện không bị co vỡ layout khi đổi màn hình
                Platform.runLater(() -> {
                    stage.setMaximized(false);
                    stage.setMaximized(true);
                });

            } catch (Exception e) {
                e.printStackTrace();
                showError("Lỗi", "Không thể mở trang thông tin tài khoản!");
            }
        }
        @FXML
        public void handleOpenAddProduct() {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/view/informationUpload_view.fxml")
                );
                Parent root = loader.load();

                // 1. Lấy Controller của màn hình thêm sản phẩm và truyền User sang
                informationUploadController controller = loader.getController();
                controller.setUser(currentUser); // Ép truyền dữ liệu ở đây!

                Stage stage = (Stage) productContainer.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setResizable(true);
                stage.setMaximized(true);
                stage.show();

                Platform.runLater(() -> {
                    stage.setMaximized(false);
                    stage.setMaximized(true);
                });

            } catch (Exception e) {
                e.printStackTrace();
                showError("Lỗi", "Không mở được giao diện thêm sản phẩm!");
            }
        }
}
