package com.uet.client.ui;

import com.uet.common.model.user.User;
import com.uet.common.network.LoginRequest;
import com.uet.client.network.ClientSocket;
import com.uet.common.network.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.concurrent.CompletableFuture;



public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

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

            CompletableFuture<Object> loginFuture = new CompletableFuture<>();

            java.util.function.Consumer<Object> loginListener = message -> {
                if (message instanceof Response) {
                    loginFuture.complete(message);
                }
            };

            network.addMessageListener(loginListener);
            network.send(request);
            Object responseObj = loginFuture.get();
            network.removeMessageListener(loginListener);

            if (responseObj instanceof Response response && response.isSuccess()) {
                User loginUser = (User) response.getData();

                logger.info("Đăng nhập OK! User: {}", loginUser.getUsername());
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
            logger.error("Lỗi khi đăng nhập: ", e);
            showError("Lỗi kết nối", "Không thể kết nối Server!");
        }
    }

    @FXML
    void handleTogglePassword(ActionEvent event) {
        if (passField.isVisible()) {
            // Hiện mật khẩu dạng thường
            passTextField.setText(passField.getText());
            passField.setVisible(false);
            passTextField.setVisible(true);
            // Đổi ảnh con mắt mở ra (Nam nhớ kiểm tra đường dẫn ảnh của mình nha)
            eyeIcon.setImage(imageOpen);
            isPasswordShown = true;
        } else {
            // Ẩn mật khẩu vào dấu chấm
            passField.setText(passTextField.getText());
            passTextField.setVisible(false);
            passField.setVisible(true);
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

            // Hiệu ứng chuyển trang mượt mà
            com.uet.client.util.TransitionUtils.applyFadeIn(root);

            Object controller = loader.getController();
            if (controller instanceof HomeController homeController) {
                homeController.setUser(user);
            }

            // Tạo scene mới và đập thẳng vào Stage
            Scene scene = new Scene(root);
            stage.setTitle(title);
            stage.setScene(scene);

            // BẬT MAXIMIZED LUÔN, KHÔNG DÙNG MẸO CO GIÃN GÂY KHỰNG
            stage.setMaximized(true);
            stage.setResizable(true);
            stage.show();

        } catch (Exception e) {
            logger.error("Không tải được giao diện: " + fxmlPath, e);
            showError("Lỗi hệ thống", "Không tải được giao diện: " + fxmlPath);
        }
    }
    private void switchScene(String fxmlPath, String title) {
        try {
            Stage stage = (Stage) userField.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            
            // Hiệu ứng chuyển trang mượt mà
            com.uet.client.util.TransitionUtils.applyFadeIn(root);
            
            Scene scene = new Scene(root);
            stage.setTitle(title);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            logger.error("Không tải được giao diện: " + fxmlPath, e);
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