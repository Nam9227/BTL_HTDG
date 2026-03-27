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

public class registerController {
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneNumberField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
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
            else if(password.equals(confirmPassword)) {
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
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
