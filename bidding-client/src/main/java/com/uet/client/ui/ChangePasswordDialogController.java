package com.uet.client.ui;

import com.uet.client.network.ClientSocket;
import com.uet.common.network.ChangePasswordRequest;
import com.uet.common.network.Response;
import com.uet.common.model.user.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import java.io.IOException;

public class ChangePasswordDialogController {

    @FXML private PasswordField oldPasswordTxt;
    @FXML private PasswordField newPasswordTxt;
    @FXML private PasswordField confirmPasswordTxt;

    private User currentUser;

    public void initData(User user) {
        this.currentUser = user;
    }

    @FXML
    private void handleSave() {
        String oldPass = oldPasswordTxt.getText().trim();
        String newPass = newPasswordTxt.getText().trim();
        String confirmPass = confirmPasswordTxt.getText().trim();

        if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            showAlert("Thông báo", "Không được bỏ trống trường nào!");
            return;
        }
        if (newPass.length() < 6) {
            showAlert("Thông báo", "Mật khẩu mới phải từ 6 ký tự trở lên!");
            return;
        }
        if (!newPass.equals(confirmPass)) {
            showAlert("Thông báo", "Xác nhận mật khẩu mới không trùng khớp!");
            return;
        }

        ChangePasswordRequest req = new ChangePasswordRequest(currentUser.getId(), oldPass, newPass);

        java.util.function.Consumer<Object> listener = new java.util.function.Consumer<>() {
            @Override
            public void accept(Object response) {
                if (response instanceof Response res) {
                    ClientSocket.getInstance().removeMessageListener(this);
                    Platform.runLater(() -> {
                        if (res.isSuccess()) {
                            showAlert("Thành công", "Đổi mật khẩu tài khoản thành công!");
                            Stage stage = (Stage) oldPasswordTxt.getScene().getWindow();
                            stage.close();
                        } else {
                            showAlert("Lỗi hệ thống", res.getMessage());
                        }
                    });
                }
            }
        };

        ClientSocket.getInstance().addMessageListener(listener);

        try {
            ClientSocket.getInstance().send(req);
        } catch (IOException e) {
            showAlert("Lỗi kết nối", "Không thể gửi yêu cầu đến Server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) oldPasswordTxt.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
