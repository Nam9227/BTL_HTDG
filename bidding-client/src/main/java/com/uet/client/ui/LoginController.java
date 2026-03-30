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
    public void handleUserEnter() {
        passField.requestFocus();
    }
    @FXML
    private void nextregiset() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/register_view.fxml"));
            Parent root = loader.load();

            // 2. Tạo một Stage mới (Cửa sổ mới)
            Stage registerStage = new Stage();
            registerStage.setTitle("Trang Đăng Ký");
            registerStage.setScene(new Scene(root));
            registerStage.setResizable(false);

            registerStage.show();
            // ((Node)event.getSource()).getScene().getWindow().hide();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Lỗi: Không tìm thấy file register_view.fxml tại đường dẫn đã khai báo!");
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