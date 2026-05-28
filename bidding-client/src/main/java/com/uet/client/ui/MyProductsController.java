package com.uet.client.ui;

import com.uet.client.network.ClientSocket;
import com.uet.common.model.auction.AuctionItem;
import com.uet.common.model.user.User;
import com.uet.common.model.user.Role;
import com.uet.common.network.GetActiveAuctionsRequest;
import com.uet.common.network.GetActiveAuctionsResponse;
import com.uet.common.network.DeleteProductRequest;
import com.uet.common.network.Response;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MyProductsController {
    private static final Logger logger = LoggerFactory.getLogger(MyProductsController.class);

    @FXML
    private HBox sidebarContainer;
    @FXML
    private VBox sideContent;
    @FXML
    private ImageView userAvatar;
    @FXML
    private Label userNameLabel;
    @FXML
    private Label balanceLabel;

    @FXML
    private Button btnTabWon;
    @FXML
    private Button btnTabMine;
    @FXML
    private FlowPane productContainer;
    @FXML
    private Button openBtn;
    @FXML
    private Button closeBtn;
    @FXML
    private Button addProductBtn;

    private User currentUser;
    private List<AuctionItem> allAuctionsFromServer = new ArrayList<>();
    private Consumer<Object> myProductsListener;

    public void setUser(User user) {
        this.currentUser = user;

        if (userNameLabel != null) {
            userNameLabel.setText(user.getFullName() != null ? user.getFullName() : user.getUsername());
        }
        if (balanceLabel != null) {
            balanceLabel.setText(String.format("💰 %,.0f đ", user.getBalance()));
        }
        if (user.getAvatarBytes() != null && user.getAvatarBytes().length > 0 && userAvatar != null) {
            try (ByteArrayInputStream bais = new ByteArrayInputStream(user.getAvatarBytes())) {
                userAvatar.setImage(new Image(bais));
            } catch (Exception e) {
                logger.error("Error loading avatar: ", e);
            }
        }

        // --- Phân quyền hiển thị các nút chức năng theo vai trò ---
        if (user.getRole() == Role.BIDDER) {
            logger.info("Người dùng đăng nhập vai trò Người mua (BUYER). Ẩn các tính năng của Người bán.");
            if (addProductBtn != null) {
                addProductBtn.setVisible(false);
                addProductBtn.setManaged(false);
            }
            if (btnTabMine != null) {
                btnTabMine.setVisible(false);
                btnTabMine.setManaged(false);
            }
        } else if (user.getRole() == Role.SELLER) {
            logger.info(
                    "Người dùng đăng nhập vai trò Người bán (SELLER). Hiện đầy đủ tính năng thêm/quản lý sản phẩm.");
            if (addProductBtn != null) {
                addProductBtn.setVisible(true);
                addProductBtn.setManaged(true);
            }
            if (btnTabMine != null) {
                btnTabMine.setVisible(true);
                btnTabMine.setManaged(true);
            }
        }

        loadDataFromServer();
    }

    @FXML
    public void initialize() {
        URL cssResource = getClass().getResource("/style/home.css");
        if (cssResource != null) {
            productContainer.getStylesheets().add(cssResource.toExternalForm());
        }

        if (sideContent != null) {
            sideContent.setTranslateX(300);
            sideContent.setVisible(false);
            sideContent.setManaged(false);
        }
        if (closeBtn != null) {
            closeBtn.setVisible(false);
        }

        if (openBtn != null) {
            openBtn.setDisable(false);
        }
        if (addProductBtn != null) {
            addProductBtn.setDisable(false);
        }
    }

    private void loadDataFromServer() {
        try {
            ClientSocket socket = ClientSocket.getInstance();
            cleanupListener();

            myProductsListener = response -> {
                if (response instanceof GetActiveAuctionsResponse auctionResponse) {
                    List<AuctionItem> items = auctionResponse.getAuctions();
                    logger.info("Data received: {} items", items.size());

                    Platform.runLater(() -> {
                        allAuctionsFromServer = items;
                        switchTabDisplay(true);
                    });
                }
            };

            socket.addMessageListener(myProductsListener);

            new Thread(() -> {
                try {
                    socket.send(new GetActiveAuctionsRequest(currentUser.getId(), "USER"));
                } catch (Exception e) {
                    logger.error("Error sending request: ", e);
                }
            }).start();

        } catch (Exception e) {
            logger.error("Connection error: ", e);
        }
    }

    private void switchTabDisplay(boolean isWonTab) {
        productContainer.getChildren().clear();

        if (allAuctionsFromServer == null || allAuctionsFromServer.isEmpty()) {
            productContainer.getChildren().add(new Label("Danh sách trống."));
            return;
        }

        for (AuctionItem item : allAuctionsFromServer) {
            if (isWonTab) {
                if (item.getWinnerId() != null && item.getWinnerId().equals(currentUser.getId())
                        && "FINISHED".equalsIgnoreCase(item.getStatus())) {
                    renderProductCard(item, true);
                }
            } else {
                if (item.getSellerId() != null && item.getSellerId().equals(currentUser.getId())) {
                    if ("FINISHED".equalsIgnoreCase(item.getStatus())) {
                        // Nếu đấu giá đã kết thúc, chỉ hiển thị bên người bán khi KHÔNG CÓ người thắng (giao dịch thất bại)
                        if (item.getWinnerId() == null || item.getWinnerId().trim().isEmpty()) {
                            renderProductCard(item, false);
                        }
                    } else {
                        // Các trạng thái khác (PENDING, RUNNING) thì luôn hiện bên người bán
                        renderProductCard(item, false);
                    }
                }
            }
        }

        if (productContainer.getChildren().isEmpty()) {
            Label emptyLabel = new Label("Trống! Chưa có sản phẩm mục này.");
            emptyLabel.setStyle("-fx-font-family: 'Segoe UI'; -fx-text-fill: #94a3b8;");
            productContainer.getChildren().add(emptyLabel);
        }
    }

    private void renderProductCard(AuctionItem item, boolean isWonTab) {
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
            try (ByteArrayInputStream bais = new ByteArrayInputStream(item.getProductImageBytes())) {
                cardImageView.setImage(new Image(bais));
            } catch (Exception e) {
                logger.error("Error rendering image: ", e);
            }
        } else {
            InputStream placeholderStream = getClass().getResourceAsStream("/photo/avatar.png");
            if (placeholderStream != null) {
                cardImageView.setImage(new Image(placeholderStream));
            }
        }

        Label nameLabel = new Label(item.getProductName());
        nameLabel.getStyleClass().add("product-title-label");
        nameLabel.setWrapText(true);

        Label priceLabel = new Label(String.format("%,.0f đ", item.getCurrentPrice()));
        priceLabel.getStyleClass().add("product-price-label");

        HBox actionBox = new HBox(8);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        actionBox.setPrefHeight(35);

        if (isWonTab) {
            // --- TRANG TRÚNG ĐẤU GIÁ: CHỈ CÓ NÚT XÓA ---
            Button deleteBtn = createDeleteButton(item, card);
            actionBox.getChildren().add(deleteBtn);
        } else {
            // --- TRANG TÔI ĐĂNG BÁN: PHÂN CHIA THEO TRẠNG THÁI ---
            String status = item.getStatus() != null ? item.getStatus().trim().toUpperCase() : "PENDING";
            if ("PENDING".equals(status)) {
                // ĐANG CHỜ DUYỆT (PENDING): CÓ NÚT SỬA VÀ NÚT XÓA
                Button editBtn = createEditButton(item);
                Button deleteBtn = createDeleteButton(item, card);
                actionBox.getChildren().addAll(editBtn, deleteBtn);
            } else if ("RUNNING".equals(status)) {
                // ĐANG CHẠY (RUNNING) CHỈ HIỆN NÚT XEM PHIÊN CHI TIẾT
                Button viewBtn = new Button("👁 Xem phiên");
                viewBtn.getStyleClass().add("action-button-view");
                viewBtn.setOnAction(e -> openAuctionDetail(item));
                actionBox.getChildren().add(viewBtn);
            } else if ("FINISHED".equals(status)) {
                // ĐÃ KẾT THÚC NHƯNG KHÔNG CÓ NGƯỜI THẮNG (GIAO DỊCH THẤT BẠI): HIỆN NÚT XÓA SẢN PHẨM
                Button deleteBtn = createDeleteButton(item, card);
                actionBox.getChildren().add(deleteBtn);
            }
        }

        card.getChildren().addAll(cardImageView, nameLabel, priceLabel, actionBox);
        productContainer.getChildren().add(card);
    }

    private Button createDeleteButton(AuctionItem item, VBox card) {
        Button deleteBtn = new Button("✕ Xóa");
        deleteBtn.getStyleClass().add("action-button-delete");
        deleteBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Xác nhận xóa");
            alert.setHeaderText(null);
            alert.setContentText(
                    "Bạn có chắc chắn muốn xóa sản phẩm \"" + item.getProductName() + "\" khỏi Cơ sở dữ liệu không?");

            ButtonType btnYes = new ButtonType("Đồng ý", ButtonBar.ButtonData.YES);
            ButtonType btnNo = new ButtonType("Hủy bỏ", ButtonBar.ButtonData.NO);
            alert.getButtonTypes().setAll(btnYes, btnNo);

            alert.showAndWait().ifPresent(type -> {
                if (type == btnYes) {
                    try {
                        Consumer<Object> singleDeleteListener = new Consumer<>() {
                            @Override
                            public void accept(Object response) {
                                if (response instanceof Response res) {
                                    Platform.runLater(() -> {
                                        if (res.isSuccess()) {
                                            productContainer.getChildren().remove(card);
                                            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                                            successAlert.setTitle("Thành công");
                                            successAlert.setHeaderText(null);
                                            successAlert.setContentText(res.getMessage());
                                            successAlert.showAndWait();
                                        } else {
                                            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                                            errorAlert.setTitle("Thất bại");
                                            errorAlert.setHeaderText(null);
                                            errorAlert.setContentText(res.getMessage());
                                            errorAlert.showAndWait();
                                        }
                                    });
                                    ClientSocket.getInstance().removeMessageListener(this);
                                }
                            }
                        };

                        ClientSocket.getInstance().addMessageListener(singleDeleteListener);
                        ClientSocket.getInstance()
                                .send(new DeleteProductRequest(item.getAuctionId(), currentUser.getId()));
                        logger.info("Sent DeleteProductRequest for auctionId: {}", item.getAuctionId());

                    } catch (Exception ex) {
                        logger.error("Error sending delete request: ", ex);
                    }
                }
            });
        });
        return deleteBtn;
    }

    private Button createEditButton(AuctionItem item) {
        Button editBtn = new Button("✎ Sửa");
        editBtn.getStyleClass().add("action-button-edit");
        editBtn.setOnAction(e -> {
            try {
                // Đổi nút thành trạng thái đang tải
                editBtn.setDisable(true);
                editBtn.setText("⏳ Đang tải...");

                Consumer<Object> singleEditFetchListener = new Consumer<>() {
                    @Override
                    public void accept(Object response) {
                        if (response instanceof GetActiveAuctionsResponse res) {
                            Platform.runLater(() -> {
                                try {
                                    editBtn.setDisable(false);
                                    editBtn.setText("✎ Sửa");

                                    if (res.getAuctions() != null && !res.getAuctions().isEmpty()) {
                                        AuctionItem fullItem = res.getAuctions().get(0);

                                        cleanupListener(); // Hủy nghe rác mạng

                                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/edit_product_view.fxml"));
                                        Parent root = loader.load();

                                        // Bắn cả Session USER và bản ghi ITEM đầy đủ từ Server sang trang sửa
                                        EditProductController editController = loader.getController();
                                        editController.setInitData(currentUser, fullItem);

                                        Stage stage = (Stage) productContainer.getScene().getWindow();
                                        stage.getScene().setRoot(root);
                                    } else {
                                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                                        errorAlert.setTitle("Lỗi");
                                        errorAlert.setHeaderText(null);
                                        errorAlert.setContentText("Không tìm thấy thông tin chi tiết sản phẩm trên hệ thống!");
                                        errorAlert.showAndWait();
                                    }
                                } catch (Exception ex) {
                                    logger.error("Không thể mở màn hình chỉnh sửa sản phẩm: ", ex);
                                }
                            });
                            ClientSocket.getInstance().removeMessageListener(this);
                        }
                    }
                };

                ClientSocket.getInstance().addMessageListener(singleEditFetchListener);
                ClientSocket.getInstance().send(new GetActiveAuctionsRequest(item.getAuctionId(), "SINGLE"));
                logger.info("Sent GetActiveAuctionsRequest (SINGLE) for auctionId: {}", item.getAuctionId());

            } catch (Exception ex) {
                editBtn.setDisable(false);
                editBtn.setText("✎ Sửa");
                logger.error("Error fetching single item details: ", ex);
            }
        });
        return editBtn;
    }

    private void openAuctionDetail(AuctionItem item) {
        try {
            cleanupListener();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/auction_detail.fxml"));
            Parent root = loader.load();

            AuctionDetailController controller = loader.getController();
            controller.setData(currentUser, item);

            com.uet.client.util.TransitionUtils.applyFadeIn(root);

            Stage stage = (Stage) productContainer.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle("Chi tiết phiên đấu giá - " + item.getProductName());

        } catch (Exception e) {
            logger.error("Không mở được trang chi tiết đấu giá: ", e);
            showError("Lỗi", "Không mở được trang chi tiết đấu giá!");
        }
    }

    @FXML
    void handleSwitchToWon(ActionEvent event) {
        if (event != null) {
            btnTabWon.getStyleClass().clear();
            btnTabWon.getStyleClass().add("custom-tab-button-active");
            btnTabMine.getStyleClass().clear();
            btnTabMine.getStyleClass().add("custom-tab-button-normal");
            switchTabDisplay(true);
        }
    }

    @FXML
    void handleSwitchToMine(ActionEvent event) {
        if (event != null) {
            btnTabMine.getStyleClass().clear();
            btnTabMine.getStyleClass().add("custom-tab-button-active");
            btnTabWon.getStyleClass().clear();
            btnTabWon.getStyleClass().add("custom-tab-button-normal");
            switchTabDisplay(false);
        }
    }

    private void cleanupListener() {
        if (myProductsListener != null) {
            try {
                ClientSocket.getInstance().removeMessageListener(myProductsListener);
            } catch (Exception e) {
                logger.error("Error removing listener: ", e);
            }
            myProductsListener = null;
        }
    }

    @FXML
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void handleOpenSidebar(ActionEvent event) {
        if (sideContent != null) {
            sideContent.setVisible(true);
            sideContent.setManaged(true);
            closeBtn.setVisible(true);

            TranslateTransition menuSlide = new TranslateTransition(Duration.millis(300), sideContent);
            menuSlide.setToX(0);

            if (openBtn != null)
                openBtn.setVisible(false);

            menuSlide.play();
        }
    }

    @FXML
    void handleCloseSidebar(ActionEvent event) {
        if (sideContent != null) {
            double width = sideContent.getWidth();
            TranslateTransition menuSlide = new TranslateTransition(Duration.millis(300), sideContent);
            menuSlide.setToX(width);

            menuSlide.setOnFinished(e -> {
                sideContent.setVisible(false);
                sideContent.setManaged(false);
                closeBtn.setVisible(false);
                if (openBtn != null)
                    openBtn.setVisible(true);
            });

            menuSlide.play();
        }
    }

    @FXML
    void handleOpenHome(ActionEvent event) {
        if (event == null)
            return;
        try {
            cleanupListener();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/home_view.fxml"));
            Parent root = loader.load();
            HomeController controller = loader.getController();
            controller.setUser(currentUser);
            Stage stage = (Stage) productContainer.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            logger.error("Navigation error: ", e);
        }
    }

    @FXML
    void handleOpenProfile(ActionEvent event) {
        if (event == null)
            return;
        try {
            cleanupListener();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/profile_view.fxml"));
            Parent root = loader.load();

            ProfileController controller = loader.getController();
            controller.setUser(currentUser);

            Stage stage = (Stage) productContainer.getScene().getWindow();
            com.uet.client.util.TransitionUtils.applyFadeIn(root);
            stage.getScene().setRoot(root);
            stage.setTitle("Thông tin tài khoản");
        } catch (Exception e) {
            logger.error("Không thể mở trang thông tin tài khoản: ", e);
            showError("Lỗi", "Không thể mở trang thông tin tài khoản!");
        }
    }

    @FXML
    void handleOpenAddProduct(ActionEvent event) {
        if (event == null)
            return;
        try {
            cleanupListener();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/informationUpload_view.fxml"));
            Parent root = loader.load();

            InformationUploadController controller = loader.getController();
            controller.setUser(currentUser);

            Stage stage = (Stage) productContainer.getScene().getWindow();
            com.uet.client.util.TransitionUtils.applyFadeIn(root);
            stage.getScene().setRoot(root);
            stage.setTitle("Đăng bán sản phẩm mới");
        } catch (Exception e) {
            logger.error("Không mở được giao diện thêm sản phẩm: ", e);
            showError("Lỗi", "Không mở được giao diện thêm sản phẩm!");
        }
    }

    @FXML
    void handleLogout(ActionEvent event) {
        if (event == null)
            return;
        try {
            cleanupListener();
            ClientSocket.getInstance().send("LOGOUT");
            ClientSocket.getInstance().close();
            this.currentUser = null;

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login_view.fxml"));
            Parent root = loader.load();

            com.uet.client.util.TransitionUtils.applyFadeIn(root);
            Stage stage = (Stage) productContainer.getScene().getWindow();
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
}