package com.uet.client.ui;

import com.uet.common.model.user.User;
import com.uet.common.network.LoginRequest;
import com.uet.client.network.ClientSocket;
import com.uet.common.network.Response;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Alert;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.scene.Node;



public class LoginController {

    @FXML
    private TextField userField;

    @FXML
    private PasswordField passField;

    @FXML
    private TextField passTextField; // Cần thêm cái này để khớp với fx:id trong FXML

    @FXML
    private ImageView eyeIcon; // Cần @FXML để JavaFX kết nối với ImageView trong Button

    private final Image imageOpen = new Image(getClass().getResourceAsStream("/photo/openEye.png"));
    private final Image imageClose = new Image(getClass().getResourceAsStream("/photo/closeEye.png"));
    private boolean isPasswordShown = false;

    @FXML
    public void handleLogin() {
        try {
            String user = userField.getText();
            String pass ;
            if (passTextField.isVisible()) {
                pass = passTextField.getText();
            } else {
                pass = passField.getText();
            }

            if (user.isEmpty() || pass.isEmpty()) {
                showError("Lỗi", "Vui lòng không để trống tài khoản hoặc mật khẩu!");
                return;
            }

            // 2. Đóng gói vào đối tượng
            LoginRequest request = new LoginRequest(user, pass);

            // 3. Gửi qua Socket (Dùng Singleton của Nam)
            ClientSocket network = ClientSocket.getInstance();
            network.connect();
            network.send(request);

            // 4. Đợi phản hồi từ Server
            Object responseObj = network.receive();

            if (responseObj instanceof Response response && response.isSuccess()) {
                User loginUser = (User) response.getData();

                System.out.println("Đăng nhập OK!");
                if (loginUser.getRole() != null && "ADMIN".equalsIgnoreCase(loginUser.getRole().name())) {
                    switchScene("/view/admin/admin_dashboard.fxml", "Trang Admin", loginUser);
                } else {
                    switchScene("/view/home_view.fxml", "Trang chủ", loginUser);
                }
            } else if (responseObj instanceof Response response) {
                showError("Lỗi", response.getMessage());
            } else {
                showError("Lỗi", "Phản hồi từ server không hợp lệ!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi kết nối", "Không thể kết nối Server!");
        }
    }

    @FXML
    private void handleTogglePassword(ActionEvent event) {
        if (!isPasswordShown) {
            // Hiện mật khẩu: Copy từ Password sang TextField
            passTextField.setText(passField.getText());
            passTextField.setVisible(true);
            passField.setVisible(false);
            eyeIcon.setImage(imageOpen);
            isPasswordShown = true;
        } else {
            // Ẩn mật khẩu: Copy từ TextField về PasswordField
            passField.setText(passTextField.getText());
            passField.setVisible(true);
            passTextField.setVisible(false);
            eyeIcon.setImage(imageClose);
            isPasswordShown = false;
        }
    }

    @FXML
    public void handleUserEnter() {
        if (isPasswordShown) {
            passTextField.requestFocus();
        } else {
            passField.requestFocus();
        }
    }

    @FXML
    private void nextregiset(ActionEvent event) {
        switchScene("/view/register_view.fxml", "Trang Đăng Ký");
    }
    private void switchScene(String fxmlPath, String title, User user) {
        try {
            Stage stage = (Stage) userField.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();

            if (controller instanceof HomeController homeController) {
                homeController.setUser(user);
            }

            Scene scene = new Scene(root);
            stage.setTitle(title);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi hệ thống", "Không tải được giao diện: " + fxmlPath);
        }
    }
    private void switchScene(String fxmlPath, String title) {
        try {
            Stage stage = (Stage) userField.getScene().getWindow();
            Scene scene = new Scene(FXMLLoader.load(getClass().getResource(fxmlPath)));
            stage.setTitle(title);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi hệ thống", "Không tải được giao diện: " + fxmlPath);
        }
    }
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}