package com.uet.client.ui;

import com.uet.client.network.ClientSocket;
import com.uet.common.network.RegisterRequest;
import com.uet.common.network.Response;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import java.util.regex.Pattern;
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
    @FXML private TextField confirmPasswordText;
    @FXML private ImageView eyeIcon;
    @FXML private ImageView confirmEyeIcon;
    @FXML private Button btnTogglePassword;
    @FXML private Button btnToggleConfirm;
    private Image imageOpen = new Image(getClass().getResourceAsStream("/photo/openEye.png"));
    private Image imageClose = new Image(getClass().getResourceAsStream("/photo/closeEye.png"));
    @FXML
    private static final String GMAIL_REGEX = "^[A-Za-z0-9+_.-]+@gmail\\.com$";
    @FXML
    public static boolean isValidGmail(String email) {
        if (email == null) {
            return false;
        }
        return Pattern.matches(GMAIL_REGEX, email);
    }
    @FXML
    private void handleTogglePassword(ActionEvent event) {
        // Xác định nút nào vừa được bấm
        Object source = event.getSource();

        if (source == btnTogglePassword) {
            // Xử lý cho mật khẩu chính
            toggleLogic(passwordField, passwordText, eyeIcon);
        } else if (source == btnToggleConfirm) {
            // Xử lý cho xác nhận mật khẩu
            toggleLogic(confirmPasswordField, confirmPasswordText, confirmEyeIcon);
        }
    }

    // Hàm logic bổ trợ
    private void toggleLogic(PasswordField pField, TextField tField, ImageView icon) {
        if (pField.isVisible()) {
            tField.setText(pField.getText());
            tField.setVisible(true);
            pField.setVisible(false);
            icon.setImage(imageOpen);
        } else {
            pField.setText(tField.getText());
            pField.setVisible(true);
            tField.setVisible(false);
            icon.setImage(imageClose);
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
            String password = passwordText.isVisible()
                    ? passwordText.getText().trim()
                    : passwordField.getText().trim();
            String confirmPassword = confirmPasswordText.isVisible()
                    ? confirmPasswordText.getText().trim()
                    : confirmPasswordField.getText().trim();
            // ... các logic kiểm tra khác ...

            if (fullName.isEmpty()) {
                showError("Thông báo", "Bạn chưa nhập tên!");
            }
            else if(email.isEmpty()) {
                showError("Thông báo", "Bạn chưa nhập email");
                return;
            }
            else if(!isValidGmail(email)) {
                showError("Thông báo", "Email chưa đúng định dạng");
                return;
            }
            else if(username.isEmpty()) {
                showError("Thông báo", "Bạn chưa nhập tên tài khoản");
                return;
            }
            else if(password.isEmpty()) {
                showError("Thông báo", "Bạn chưa nhập mật khẩu");
                return;
            }
            else if(confirmPassword.isEmpty()) {
                showError("Thông báo", "Bạn chưa xác nhận mật khẩu");
                return;
            }
            else if(!password.equals(confirmPassword)) {
                showError("Thông báo", "Mật khẩu không khớp");
                return;
            }
            RegisterRequest request = new RegisterRequest(username, password, fullName, email);

            ClientSocket network = ClientSocket.getInstance();
            network.connect();
            network.send(request);

            Object responseObj = network.receive();

            if (responseObj instanceof Response response && response.isSuccess()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Thành công");
                alert.setHeaderText(null);
                alert.setContentText(response.getMessage() + "! Nhấn OK để quay lại đăng nhập.");
                alert.showAndWait();

                switchScene("/view/login_view.fxml", "Trang Đăng Nhập");

            } else if (responseObj instanceof Response response) {
                showError("Thông báo", response.getMessage());
                return;
            } else {
                showError("Thông báo", "Phản hồi từ server không hợp lệ!");
                return;
            }


        } catch (Exception a) {
            a.printStackTrace();
            showError("Lỗi kết nối", "Không thể kết nối Server!");
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
