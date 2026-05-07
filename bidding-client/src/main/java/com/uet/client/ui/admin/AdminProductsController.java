package com.uet.client.ui.admin;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class AdminProductsController {
    private void switchScene(ActionEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            System.err.println("Lỗi chuyển trang: " + fxmlPath);
            e.printStackTrace();
        }
    }
    @FXML
    private void goDashboard(ActionEvent event) {
        switchScene(event, "/view/admin/admin_dashboard.fxml","Dashboard");
    }
    @FXML
    private void goUsers(ActionEvent event) {
        switchScene(event, "/view/admin/admin_users.fxml", "Quản lý người dùng");
    }
    @FXML
    private void goAuctions(ActionEvent event) {
        switchScene(event, "/view/admin/admin_auctions.fxml", "Quản lý phiên đấu giá");
    }
    @FXML
    private void handleLogout() {
        System.out.println("Đang đăng xuất...");
        System.exit(0);
    }
    private void handleSearch
}
