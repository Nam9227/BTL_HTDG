package com.uet.client.ui.admin;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class AdminProductsController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;

    @FXML private TableView<String[]> productTable;
    @FXML private TableColumn<String[], String> idColumn;
    @FXML private TableColumn<String[], String> nameColumn;
    @FXML private TableColumn<String[], String> ownerColumn;
    @FXML private TableColumn<String[], String> startPriceColumn;
    @FXML private TableColumn<String[], String> currentPriceColumn;
    @FXML private TableColumn<String[], String> createdAtColumn;
    @FXML private TableColumn<String[], String> statusColumn;

    @FXML
    public void initialize() {
        System.out.println("Khởi tạo màn hình Quản lý sản phẩm...");
        if (statusFilter != null) {
            statusFilter.getItems().addAll("Tất cả", "Chờ duyệt", "Đang đấu giá", "Đã bán", "Bị từ chối");
        }
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String informationSearch = searchField.getText();
        String selectedStatus = statusFilter.getValue();
        System.out.println("Đang tìm kiếm sản phẩm với từ khóa: " + informationSearch + " | Trạng thái: " + selectedStatus);
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        System.out.println("Đang làm mới danh sách sản phẩm...");
        searchField.clear();
        if (statusFilter != null) statusFilter.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleViewDetail(ActionEvent event) {
        System.out.println("Xem chi tiết sản phẩm đang chọn...");
    }

    @FXML
    private void handleApprove(ActionEvent event) {
        System.out.println("Phê duyệt sản phẩm...");
    }

    @FXML
    private void handleReject(ActionEvent event) {
        System.out.println("Từ chối sản phẩm...");
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        System.out.println("Xóa sản phẩm...");
    }

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
        switchScene(event, "/view/admin/admin_dashboard.fxml", "Dashboard");
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
    private void handleLogout(ActionEvent event) {
        switchScene(event,"/view/login_view.fxml","Đang đăng xuất...");
    }
}
