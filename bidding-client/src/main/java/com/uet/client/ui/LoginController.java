package com.uet.client.ui;

import com.uet.client.model.network.LoginRequest;
import com.uet.client.network.ClientSocket;
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

    private final Image imageOpen = new Image(getClass().getResourceAsStream("/photo/openeye.png"));
    private final Image imageClose = new Image(getClass().getResourceAsStream("/photo/closeeye.png"));
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
            network.connect(); // Nhớ check port 27915 trong file này nhé
            network.send(request);

            // 4. Đợi phản hồi từ Server
            Object response = network.receive();

            if ("LOGIN_SUCCESS".equals(response)) {
                System.out.println("Đăng nhập OK!");
                // Code chuyển màn hình sang Home ở đây
            } else {
                showError("Lỗi","Sai tài khoản hoặc mật khẩu!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Lỗi kết nối Server: ");
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/register_view.fxml"));
            Parent root = loader.load();
            Stage registerStage = new Stage();
            registerStage.setTitle("Trang Đăng Ký");
            registerStage.setScene(new Scene(root));
            registerStage.setResizable(false);
            registerStage.show();
            Node source = (Node) event.getSource();
            Stage currentStage = (Stage) source.getScene().getWindow();
            currentStage.hide();
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