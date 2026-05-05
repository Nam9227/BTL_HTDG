package com.uet.client.ui.admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import java.io.IOException;

public class AdminViewController {

    @FXML
    private StackPane mainContent; // Đây là nơi chứa các trang con

    @FXML
    public void initialize() {
        // Mặc định khi mở Admin lên sẽ hiện trang Thống kê hoặc trang đầu tiên
        loadScene("admin_dashboard.fxml");
    }

    // Các hàm xử lý sự kiện nút bấm bên trái (Left Menu)
    @FXML
    private void handleGoDashboard() {
        loadScene("admin_dashboard.fxml");
    }

    @FXML
    private void handleGoUsers() {
        loadScene("admin_users.fxml");
    }

    @FXML
    private void handleGoAuctions() {
        loadScene("admin_auctions.fxml");
    }

    @FXML
    private void handleGoCategories() {
        loadScene("admin_categories.fxml");
    }

    @FXML
    private void handleLogout() {
        // Logic đăng xuất (ví dụ quay lại màn hình Login)
        System.out.println("Đang đăng xuất...");
        System.exit(0);
    }

    // Hàm dùng chung để load file FXML vào vùng chính
    private void loadScene(String fxmlFile) {
        try {
            // Thêm đường dẫn thư mục chứa các file FXML admin vào đây
            String path = "/view/admin/" + fxmlFile;

            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();

            if (mainContent != null) {
                mainContent.getChildren().clear();
                mainContent.getChildren().add(root);
            } else {
                System.err.println("Lỗi: mainContent (StackPane) chưa được tiêm (inject) từ FXML!");
            }

        } catch (IOException e) {
            System.err.println("Không thể load file tại: /view/admin/" + fxmlFile);
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.err.println("Lỗi: Đường dẫn file FXML bị sai (Null), không tìm thấy file!");
        }
    }
}