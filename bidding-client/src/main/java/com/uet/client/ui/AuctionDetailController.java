package com.uet.client.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.scene.Node;

public class AuctionDetailController {
    @FXML
    void handleBack(ActionEvent event) {
        switchScene(event, "/com/app/views/HomeView.fxml", "Trang Chủ Đấu Giá");
    }
    private void switchScene(ActionEvent event, String fxmlPath, String title) {
        try {
            // Lấy Stage từ sự kiện bấm nút
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());

            stage.setTitle(title);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.centerOnScreen(); // Nên thêm để cửa sổ không bị lệch khi đổi kích thước Scene
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
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
