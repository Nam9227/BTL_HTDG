package com.uet.client.ui;

import com.uet.client.network.ClientSocket;
import com.uet.common.model.user.Role;
import com.uet.common.model.user.User;
import com.uet.common.network.UpdateRoleRequest;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.util.Duration;

public class HomeController{

        @FXML private VBox sideContent; // Sidebar màu xanh
        @FXML private Button openBtn;   // Nút 3 gạch (nằm ngoài sidebar)
        @FXML private Button closeBtn;  // Nút X (nằm trong sidebar)


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
                }
            }
        private String formatMoney(double amount) {
            return String.format("%,.0f đ", amount);
        }
        private void showChooseRoleDialog() {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Chọn vai trò");
            alert.setHeaderText("Bạn muốn sử dụng hệ thống với vai trò nào?");
            alert.setContentText("Chỉ cần chọn một lần.");

            ButtonType bidderBtn = new ButtonType("Người đấu giá");
            ButtonType sellerBtn = new ButtonType("Người bán hàng");

            alert.getButtonTypes().setAll(bidderBtn, sellerBtn);

            alert.showAndWait().ifPresent(result -> {
                if (result == bidderBtn) {
                    currentUser.setRole(Role.BIDDER);
                    saveRoleToServer(Role.BIDDER);
                } else if (result == sellerBtn) {
                    currentUser.setRole(Role.SELLER);
                    saveRoleToServer(Role.SELLER);
                }

                applyRoleUI();
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

            } else if (currentUser.getRole() == Role.SELLER) {
                System.out.println("Người bán hàng");

                // Nếu người bán KHÔNG được thấy danh sách đấu giá thì mở 2 dòng này:
                // productContainer.setVisible(false);
                // productContainer.setManaged(false);
            }
        }
        public void initialize() {
            // Ban đầu ẩn Sidebar và nút X đi
            // Giả sử chiều rộng sidebar là 300
            sideContent.setTranslateX(300);
            sideContent.setVisible(false);
            sideContent.setManaged(false);
            closeBtn.setVisible(false);
        }

        @FXML
        public void handleOpenSidebar() {
            sideContent.setVisible(true);
            sideContent.setManaged(true);
            closeBtn.setVisible(true);

            // Hiệu ứng đẩy Sidebar vào từ phải sang trái (hoặc trái sang phải tùy layout của bạn)
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

            // Đẩy sidebar ra ngoài (ví dụ sang phải)
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
}
