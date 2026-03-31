package com.uet.client.ui;

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

public class LoginController {

    @FXML
    private TextField userField;

    @FXML
    private PasswordField passField;

    @FXML
    private TextField passTextField; // Cần thêm cái này để khớp với fx:id trong FXML

    @FXML
    private ImageView eyeIcon; // Cần @FXML để JavaFX kết nối với ImageView trong Button

    private final Image imageOpen = new Image(getClass().getResourceAsStream("/photo/openeye.png"));
    private final Image imageClose = new Image(getClass().getResourceAsStream("/photo/closeeye.png"));
    private boolean isPasswordShown = false;

    @FXML
    public void handleLogin() {
        // Lấy pass từ trường đang hiển thị
        String username = userField.getText();
        String password = isPasswordShown ? passTextField.getText() : passField.getText();

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
    private void nextregiset() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/register_view.fxml"));
            Parent root = loader.load();
            Stage registerStage = new Stage();
            registerStage.setTitle("Trang Đăng Ký");
            registerStage.setScene(new Scene(root));
            registerStage.setResizable(false);
            registerStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Lỗi hệ thống", "Không tìm thấy giao diện đăng ký!");
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