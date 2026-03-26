package com.uet.client.ui;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Alert;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.io.IOException;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
public class LoginController {

    @FXML
    private TextField userField;
    @FXML
    private PasswordField passField;
    // Các thành phần của trang Đăng ký (Đặt @FXML để kết nối với FXML)
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneNumberField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML
    public void handleLogin() {
        String username = userField.getText();
        String password = passField.getText();
        if (username.isEmpty() || password.isEmpty()) {
            showError("Lỗi", "Vui lòng không để trống tài khoản hoặc mật khẩu!");
            return;
        }
        if (username.equals("admin") && password.equals("123")) {
            System.out.println("Đăng nhập thành công!");
        } else {
            showError("Thất bại", "Tài khoản hoặc mật khẩu không đúng.");
        }
    }

    @FXML
    public void handleShowRegister() {
        System.out.println("Nam vừa nhấn vào link Đăng ký ngay!");
        try {
            // 1. Tải file FXML của trang đăng ký
            // Đảm bảo file Register.fxml nằm cùng gói (package) với file Login.fxml
            // Bạn phải đi từ gốc của thư mục resources (bắt đầu bằng dấu /)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/register_view.fxml"));
            Scene registerScene = loader.load();

            // 2. Lấy Stage (cửa sổ) hiện tại
            // Vì handleShowRegister thường được gọi từ một ActionEvent,
            // nhưng ở đây bạn không truyền tham số, ta có thể lấy qua userField
            Stage stage = (Stage) userField.getScene().getWindow();

            // 3. Chuyển sang Scene đăng ký
            //Scene registerScene = new Scene(registerRoot);
            stage.setScene(registerScene);
            stage.setTitle("Đăng ký tài khoản");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Lỗi hệ thống", "Không thể tải trang đăng ký: " + e.getMessage());
        }
    }
    @FXML
    public void handleRegister() {
        // Dòng này để kiểm tra xem nút có hoạt động không (xem ở tab Run/Console)
        System.out.println("Nút Đăng ký đã được bấm!");

        try {
            // Kiểm tra xem các ô nhập liệu có bị null không (do quên đặt fx:id)
            if (fullNameField == null) {
                System.out.println("Lỗi: fullNameField bị null. Kiểm tra lại fx:id trong FXML!");
                return;
            }

            String fullName = fullNameField.getText().trim();
            String email = emailField.getText().trim();
            String phoneNumber = phoneNumberField.getText().trim();
            String password = passwordField.getText().trim();
            String confirmPassword = confirmPasswordField.getText().trim();
            // ... các logic kiểm tra khác ...

            if (fullName.isEmpty()) {
                showError("Thông báo", "Bạn chưa nhập tên!");
            }
            else if(email.isEmpty()) {
                showError("Thông báo", "Bạn chưa nhập email");
            }
            else if(phoneNumber.isEmpty()) {
                showError("Thông báo", "Bạn chưa nhập số điện thoại");
            }
            else if(password.isEmpty()) {
                showError("Thông báo", "Bạn chưa nhập mật khẩu");
            }
            else if(confirmPassword.isEmpty()) {
                showError("Thông báo", "Bạn chưa xác nhận mật khẩu");
            }
            if(password.equals(confirmPassword)) {
                // Hiện thông báo thành công
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Thành công");
                alert.setHeaderText(null);
                alert.setContentText("Tạo tài khoản thành công! Nhấn OK để quay lại đăng nhập.");

                // Đợi người dùng bấm OK
                alert.showAndWait();

            }else{
                showError("Thông báo", "Mật khẩu không khớp");
            }
        } catch (Exception a) {
            a.printStackTrace(); // In lỗi chi tiết ra màn hình đen (Console)
        }

    }

    @FXML
    public void handleUserEnter() {
        passField.requestFocus();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}