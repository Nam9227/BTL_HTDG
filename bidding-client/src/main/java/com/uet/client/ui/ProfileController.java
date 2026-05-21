package com.uet.client.ui;

import com.uet.client.network.ClientSocket;
import com.uet.common.model.user.User;
import com.uet.common.network.FileUploadData;
import com.uet.common.network.Response;
import com.uet.common.network.UpdateProfileRequest;
//import com.uet.common.network.TransactionRequest;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.Optional;

public class ProfileController {

    @FXML private Label fullNameLabel, usernameLabel, statusLabel, balanceLabel;
    @FXML private TextField fullNameField, usernameField, emailField, phoneField, addressField;
    @FXML private Button editButton, saveButton, avatarChangeBtn, changePasswordButton;
    @FXML private ImageView avatarImage;

    private User currentUser;
    private boolean editing = false;
    private FileUploadData selectedAvatar;

    public void setUser(User user) {
        this.currentUser = user;

        fullNameLabel.setText(user.getFullName() != null ? user.getFullName() : user.getUsername());
        usernameLabel.setText("@" + user.getUsername());
        balanceLabel.setText(String.format("%,.0f đ", user.getBalance() != null ? user.getBalance().doubleValue() : 0));

        fullNameField.setText(user.getFullName());
        usernameField.setText(user.getUsername());
        emailField.setText(user.getEmail());
        phoneField.setText(user.getPhone());
        addressField.setText(user.getAddress());

        if (user.getAvatarPath() != null && !user.getAvatarPath().isBlank()) {
            File avatarFile = new File(user.getAvatarPath());
            if (avatarFile.exists()) {
                avatarImage.setImage(new Image(avatarFile.toURI().toString()));
            }
        }
    }

    // 🛠 LOGIC CHỈNH SỬA: Cập nhật màu nền sáng hơn để phân biệt
    @FXML
    private void handleEdit() {
        editing = true;

        fullNameField.setEditable(true);
        emailField.setEditable(true);
        phoneField.setEditable(true);
        addressField.setEditable(true);

        // Đổi màu nền sáng hơn một chút để người dùng biết là ô này GÕ ĐƯỢC
        String activeStyle = "-fx-background-color: #4a5056; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 10;";
        fullNameField.setStyle(activeStyle);
        emailField.setStyle(activeStyle);
        phoneField.setStyle(activeStyle);
        addressField.setStyle(activeStyle);

        saveButton.setDisable(false);
        editButton.setDisable(true);
    }

    @FXML
    private void handleSave() {
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String address = addressField.getText().trim();

        // Validate cơ bản phía Client trước khi gửi
        if (fullName.isEmpty()) {
            showAlert("Cảnh báo", "Họ và tên không được để trống!", Alert.AlertType.WARNING);
            return;
        }

        try {
            UpdateProfileRequest req = new UpdateProfileRequest(
                    currentUser.getId(),
                    fullName,
                    email,
                    phone,
                    address,
                    selectedAvatar
            );

            java.util.function.Consumer<Object> updateProfileListener = new java.util.function.Consumer<>() {
                @Override
                public void accept(Object response) {
                    if (response instanceof Response res) {
                        Platform.runLater(() -> {
                            if (res.isSuccess()) {
                                currentUser.setFullName(fullName);
                                currentUser.setEmail(email);
                                currentUser.setPhone(phone);
                                currentUser.setAddress(address);

                                if (res.getData() instanceof String avatarPath) {
                                    currentUser.setAvatarPath(avatarPath);
                                }

                                fullNameLabel.setText(fullName);

                                lockForm();
                                showAlert("Thành công", res.getMessage(), Alert.AlertType.INFORMATION);
                            } else {
                                showAlert("Thất bại", res.getMessage(), Alert.AlertType.ERROR);
                            }
                        });

                        ClientSocket.getInstance().removeMessageListener(this);
                    }
                }
            };

            ClientSocket.getInstance().addMessageListener(updateProfileListener);
            ClientSocket.getInstance().send(req);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể kết nối tới Server để lưu thay đổi!", Alert.AlertType.ERROR);
        }
    }

    // Hàm khóa lại Form (trả lại màu sẫm)
    private void lockForm() {
        editing = false;
        selectedAvatar = null;

        fullNameField.setEditable(false);
        emailField.setEditable(false);
        phoneField.setEditable(false);
        addressField.setEditable(false);

        String lockedStyle = "-fx-background-color: #2a2e31; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 10;";
        fullNameField.setStyle(lockedStyle);
        emailField.setStyle(lockedStyle);
        phoneField.setStyle(lockedStyle);
        addressField.setStyle(lockedStyle);

        saveButton.setDisable(true);
        editButton.setDisable(false);
    }

    // 💵 NẠP TIỀN
    @FXML
    private void handleDeposit() {
        handleMoneyTransaction("NẠP TIỀN VÀO VÍ", "DEPOSIT");
    }

    // 💸 RÚT TIỀN
    @FXML
    private void handleWithdraw() {
        handleMoneyTransaction("RÚT TIỀN MẶT", "WITHDRAW");
    }

    // Hàm gom chung xử lý tiền tệ
    private void handleMoneyTransaction(String title, String type) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText("Nhập số tiền muốn giao dịch (đ):");
        dialog.setContentText("Số tiền (VND):");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(amountStr -> {
            try {
                double amount = Double.parseDouble(amountStr.trim());
                if (amount <= 0) throw new NumberFormatException();

                if (type.equals("WITHDRAW") && amount > currentUser.getBalance().doubleValue()) {
                    showAlert("Lỗi", "Số dư ví hiện không đủ!", Alert.AlertType.ERROR);
                    return;
                }

                // Gửi lệnh xử lý tiền mặt lên Server cập nhật DB
                // TransactionRequest req = new TransactionRequest(currentUser.getId(), amount, type);
                // ClientSocket.getInstance().send(req);

                showAlert("Thông báo", "Yêu cầu giao dịch đã được gửi xử lý!", Alert.AlertType.INFORMATION);

            } catch (NumberFormatException e) {
                showAlert("Lỗi", "Số tiền nhập vào không hợp lệ!", Alert.AlertType.ERROR);
            }
        });
    }

    @FXML
    private void handleAvatarChange() {
        if (!editing) {
            showAlert("Thông báo", "Bạn cần bấm 'Chỉnh sửa thông tin' trước khi đổi avatar!", Alert.AlertType.INFORMATION);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh avatar");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Ảnh", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp"),
                new FileChooser.ExtensionFilter("Tất cả file", "*.*")
        );

        Stage stage = (Stage) avatarImage.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try {
                byte[] fileBytes = Files.readAllBytes(selectedFile.toPath());

                selectedAvatar = new FileUploadData(
                        selectedFile.getName(),
                        Files.probeContentType(selectedFile.toPath()),
                        fileBytes
                );

                avatarImage.setImage(new Image(selectedFile.toURI().toString()));

            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Lỗi", "Không thể đọc ảnh avatar!", Alert.AlertType.ERROR);
            }
        }
    }
    @FXML private void handleChangePassword() { /* Logic đổi mật khẩu */ }

    // Nút quay lại Trang chủ full màn hình
    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/home_view.fxml"));
            Parent root = loader.load();

            // Truyền ngược lại user về cho trang chủ để không bị mất Session đăng nhập
            HomeController controller = loader.getController();
            controller.setUser(currentUser);

            Stage stage = (Stage) fullNameLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String text, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();
    }

}