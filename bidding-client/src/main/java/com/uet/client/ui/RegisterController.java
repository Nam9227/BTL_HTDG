package com.uet.client.ui;

import com.uet.client.network.ClientSocket;
import com.uet.common.network.RegisterRequest;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import javafx.scene.control.Alert;

import javafx.event.ActionEvent;
import javafx.scene.control.TextField;

public class RegisterController {
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField passwordText;
    @FXML private Button togglePassword;
    @FXML
    private ImageView eyeIcon;
    private Image imageOpen = new Image(getClass().getResourceAsStream("/photo/openEye.png"));
    private Image imageClose = new Image(getClass().getResourceAsStream("/photo/closeEye.png"));
    private boolean isPasswordShown = false;

    @FXML
    void handleTogglePassword(ActionEvent event) {
        if (isPasswordShown) {
            // Chuyển từ hiện sang ẩn
            passwordField.setText(passwordText.getText());
            passwordField.setVisible(true);
            passwordText.setVisible(false);
            // Thay đổi icon sang mắt đóng (nếu có)
            eyeIcon.setImage(imageClose);
            isPasswordShown = false;
        } else {
            // Chuyển từ ẩn sang hiện
            passwordText.setText(passwordField.getText());
            passwordText.setVisible(true);
            passwordField.setVisible(false);
            // Thay đổi icon sang mắt mở (nếu có)
            eyeIcon.setImage(imageOpen);
            isPasswordShown = true;
        }
    }
    @FXML
    private TextField inputField;

    @FXML
    public void handleRegister() {
        System.out.println("Nút Đăng ký đã được bấm!");

        try {
            // Kiểm tra xem các ô nhập liệu có bị null không (do quên đặt fx:id)
            if (fullNameField == null) {
                System.out.println("Lỗi: fullNameField bị null. Kiểm tra lại fx:id trong FXML!");
                return;
            }

            String fullName = fullNameField.getText().trim();
            String email = emailField.getText().trim();
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            String confirmPassword = confirmPasswordField.getText().trim();
            // ... các logic kiểm tra khác ...

            if (fullName.isEmpty()) {
                showError("Thông báo", "Bạn chưa nhập tên!");
            }
            else if(email.isEmpty()) {
                showError("Thông báo", "Bạn chưa nhập email");
            }
            else if(username.isEmpty()) {
                showError("Thông báo", "Bạn chưa nhập tên tài khoản");
            }
            else if(password.isEmpty()) {
                showError("Thông báo", "Bạn chưa nhập mật khẩu");
            }
            else if(confirmPassword.isEmpty()) {
                showError("Thông báo", "Bạn chưa xác nhận mật khẩu");
            }
            else if(!password.equals(confirmPassword)) {
                showError("Thông báo", "Mật khẩu không khớp");
            }
            RegisterRequest request = new RegisterRequest(username, password, fullName, email);

            ClientSocket network = ClientSocket.getInstance();
            network.connect();
            network.send(request);

            Object response = network.receive();

            if ("REGISTER_SUCCESS".equals(response)) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Thành công");
                alert.setHeaderText(null);
                alert.setContentText("Tạo tài khoản thành công! Nhấn OK để quay lại đăng nhập.");
                alert.showAndWait();

                switchScene("/view/login_view.fxml", "Trang Đăng Nhập");

            } else if ("USERNAME_EXISTS".equals(response)) {
                showError("Thông báo", "Tên tài khoản đã tồn tại!");

            } else if ("EMAIL_EXISTS".equals(response)) {
                showError("Thông báo", "Email đã tồn tại!");

            } else {
                showError("Thông báo", "Đăng ký thất bại!");
            }


        } catch (Exception a) {
            a.printStackTrace(); // In lỗi chi tiết ra màn hình đen (Console)
        }

    }
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void handleEnter(ActionEvent event) {
        TextField source = (TextField) event.getSource();

        if (source == fullNameField) {
            emailField.requestFocus();
        } else if (source == emailField) {
            usernameField.requestFocus();
        } else if (source == usernameField) {
            passwordField.requestFocus();
        } else if (source == passwordField) {
            confirmPasswordField.requestFocus();
        }
    }
    @FXML
    private void nextregiset(ActionEvent event) {
        switchScene("/view/login_view.fxml", "Trang Đăng Nhập");
    }
    private void switchScene(String fxmlPath, String title) {
        try {
            Stage stage = (Stage) fullNameField.getScene().getWindow();
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

}
