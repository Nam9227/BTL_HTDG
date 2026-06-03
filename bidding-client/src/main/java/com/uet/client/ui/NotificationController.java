package com.uet.client.ui;

import com.uet.client.network.ClientSocket;
import com.uet.common.model.notification.Notification;
import com.uet.common.model.user.User;
import com.uet.common.network.GetNotificationsRequest;
import com.uet.common.network.Response;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region; 
import javafx.stage.Stage;

import java.util.List;
import java.util.function.Consumer;
import com.uet.client.util.TransitionUtils;
import com.uet.common.model.user.Role;

public class NotificationController {

    @FXML private VBox notificationContainer;
    @FXML private ImageView userAvatar;
    @FXML private Label userNameLabel;
    @FXML private Label balanceLabel;
    @FXML private VBox sideContent;
    @FXML private Button openBtn;
    @FXML private Button closeBtn;
    @FXML private Button addProductBtn;

    private User currentUser;

    @FXML
    public void initialize() {
        if (sideContent != null) {
            sideContent.setTranslateX(300);
            sideContent.setVisible(false);
            sideContent.setManaged(false);
        }
        if (closeBtn != null) {
            closeBtn.setVisible(false);
        }
    }

    public void setUser(User user) {
        this.currentUser = user;

        if (userNameLabel != null) userNameLabel.setText(user.getFullName() != null ? user.getFullName() : user.getUsername());
        if (balanceLabel != null) balanceLabel.setText(String.format("💰 %,.0fđ", user.getBalance() != null ? user.getBalance().doubleValue() : 0));

        if (userAvatar != null && user.getAvatarBytes() != null && user.getAvatarBytes().length > 0) {
            try {
                userAvatar.setImage(null);
                javafx.scene.image.Image img = new javafx.scene.image.Image(
                        new java.io.ByteArrayInputStream(user.getAvatarBytes()));
                userAvatar.setImage(img);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ClientSocket.onUserUpdated = updatedUser -> {
            if (this.currentUser != null && this.currentUser.getId().equals(updatedUser.getId())) {
                javafx.application.Platform.runLater(() -> setUser(updatedUser));
            }
        };

        
        if (addProductBtn != null) {
            if (user.getRole() == Role.SELLER) {
                addProductBtn.setVisible(true);
                addProductBtn.setManaged(true);
            } else {
                addProductBtn.setVisible(false);
                addProductBtn.setManaged(false);
            }
        }

        loadNotifications();
    }

    private void loadNotifications() {
        if (currentUser == null) {
            System.out.println("[LỖI] currentUser bị null, không thể lấy thông báo!");
            return;
        }

        
        GetNotificationsRequest req = new GetNotificationsRequest(currentUser.getId());

        
        Consumer<Object> responseListener = new Consumer<>() {
            @Override
            public void accept(Object response) {
                
                if (response instanceof Response res && res.isSuccess()) {
                    if (res.getData() instanceof List<?> list) {

                        
                        Platform.runLater(() -> {
                            notificationContainer.getChildren().clear(); 

                            if (list.isEmpty()) {
                                Label emptyLabel = new Label("📭 Hộp thư của bạn đang trống trơn.");
                                emptyLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 14px; -fx-padding: 20;");
                                notificationContainer.getChildren().add(emptyLabel);
                                return;
                            }

                            
                            for (Object item : list) {
                                if (item instanceof Notification noti) {
                                    VBox card = createNotificationCard(noti);
                                    notificationContainer.getChildren().add(card);
                                }
                            }
                        });
                    }

                    
                    ClientSocket.getInstance().removeMessageListener(this);
                }
            }
        };

        try {
            
            ClientSocket.getInstance().addMessageListener(responseListener);

            
            ClientSocket.getInstance().send(req);

            System.out.println("[Client] Đã bắn GetNotificationsRequest lên Server cho User ID: " + currentUser.getId());

        } catch (Exception e) {
            System.out.println("[LỖI] Không thể gửi yêu cầu lấy thông báo lên Server!");
            e.printStackTrace();
            
            ClientSocket.getInstance().removeMessageListener(responseListener);
        }
    }

    private VBox createNotificationCard(Notification noti) {
        VBox card = new VBox(8);
        card.getStyleClass().add("notification-card");

        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label titleLbl = new Label(noti.getTitle());
        titleLbl.getStyleClass().add("noti-card-title");

        Label timeLbl = new Label(noti.getCreatedAt() != null ? noti.getCreatedAt().toString() : "");
        timeLbl.getStyleClass().add("noti-card-time");

        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topRow.getChildren().addAll(titleLbl, spacer, timeLbl);

        Label contentLbl = new Label(noti.getContent());
        contentLbl.getStyleClass().add("noti-card-content");
        contentLbl.setWrapText(true);

        card.getChildren().addAll(topRow, contentLbl);

        return card;
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/home_view.fxml"));
            Parent root = loader.load();
            HomeController controller = loader.getController();
            controller.setUser(currentUser);
            Stage stage = (Stage) notificationContainer.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    public void handleOpenProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/profile_view.fxml"));
            Parent root = loader.load();

            ProfileController controller = loader.getController();
            controller.setUser(currentUser); 

            Stage stage = (Stage) userNameLabel.getScene().getWindow(); 
            if (TransitionUtils.class != null) {
                TransitionUtils.applyFadeIn(root);
            }
            stage.getScene().setRoot(root);
            stage.setTitle("Thông tin tài khoản");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleOpenAddProduct() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/informationUpload_view.fxml"));
            Parent root = loader.load();

            InformationUploadController controller = loader.getController();
            controller.setUser(currentUser);

            Stage stage = (Stage) notificationContainer.getScene().getWindow();
            if (TransitionUtils.class != null) {
                TransitionUtils.applyFadeIn(root);
            }
            stage.getScene().setRoot(root);
            stage.setTitle("Đăng bán sản phẩm mới");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleOpenMyProducts() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/my_products.fxml"));
            Parent root = loader.load();

            MyProductsController controller = loader.getController();
            controller.setUser(currentUser);

            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            if (TransitionUtils.class != null) {
                TransitionUtils.applyFadeIn(root);
            }
            stage.getScene().setRoot(root);
            stage.setTitle("Sản phẩm của tôi");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleOpenSidebar() {
        if (sideContent != null) {
            sideContent.setVisible(true);
            sideContent.setManaged(true);
            if (closeBtn != null) closeBtn.setVisible(true);

            javafx.animation.TranslateTransition menuSlide = new javafx.animation.TranslateTransition(javafx.util.Duration.millis(300), sideContent);
            menuSlide.setToX(0);

            if (openBtn != null) openBtn.setVisible(false);

            menuSlide.play();
        }
    }

    @FXML
    public void handleCloseSidebar() {
        if (sideContent != null) {
            double width = sideContent.getWidth();
            javafx.animation.TranslateTransition menuSlide = new javafx.animation.TranslateTransition(javafx.util.Duration.millis(300), sideContent);

            menuSlide.setToX(width);

            menuSlide.setOnFinished(e -> {
                sideContent.setVisible(false);
                sideContent.setManaged(false);
                if (closeBtn != null) closeBtn.setVisible(false);
                if (openBtn != null) openBtn.setVisible(true);
            });

            menuSlide.play();
        }
    }

    @FXML
    public void handleLogout() {
        try {
            ClientSocket.getInstance().send("LOGOUT");
            ClientSocket.getInstance().close();
            this.currentUser = null;

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login_view.fxml"));
            Parent root = loader.load();

            if (TransitionUtils.class != null) {
                TransitionUtils.applyFadeIn(root);
            }

            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle("Đăng nhập hệ thống");

            stage.setMaximized(false);
            stage.setWidth(800);
            stage.setHeight(600);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}