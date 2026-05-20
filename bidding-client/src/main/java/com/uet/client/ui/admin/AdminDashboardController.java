package com.uet.client.ui.admin;

import com.uet.client.model.dashboard.DashboardActivity;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.io.IOException;

public class AdminDashboardController {


    @FXML private Label totalUsersLabel;
    @FXML private Label totalProductsLabel;
    @FXML private Label activeAuctionsLabel;
    @FXML private Label pendingProductsLabel;


    @FXML private TableView<DashboardActivity> recentActivityTable;
    @FXML private TableColumn<DashboardActivity, String> timeColumn;
    @FXML private TableColumn<DashboardActivity, String> actionColumn;
    @FXML private TableColumn<DashboardActivity, String> targetColumn;
    @FXML private TableColumn<DashboardActivity, String> statusColumn;

    @FXML
    public void initialize() {

        timeColumn.setCellValueFactory(cellData -> cellData.getValue().timeProperty());
        actionColumn.setCellValueFactory(cellData -> cellData.getValue().actionProperty());
        targetColumn.setCellValueFactory(cellData -> cellData.getValue().targetProperty());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        // 2. Load dữ liệu
        refreshData();
    }

    private void refreshData() {

        totalUsersLabel.setText("1,250");
        totalProductsLabel.setText("450");
        activeAuctionsLabel.setText("12");
        pendingProductsLabel.setText("5");


        ObservableList<DashboardActivity> activities = FXCollections.observableArrayList(
                new DashboardActivity("10:30", "Đăng nhập hệ thống", "Admin_01", "Thành công"),
                new DashboardActivity("10:25", "Duyệt sản phẩm", "Laptop Dell XPS", "Thành công"),
                new DashboardActivity("10:15", "Khóa tài khoản", "User_Bad_01", "Hoàn tất")
        );
        recentActivityTable.setItems(activities);
    }



    private void switchScene(ActionEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.getScene().setRoot(root);

            stage.setTitle(title);

        } catch (IOException e) {
            System.err.println("Lỗi chuyển trang: " + fxmlPath);
            e.printStackTrace();
        }
    }

    @FXML
    private void showDashboard(ActionEvent event) {
        refreshData();
    }

    @FXML
    private void showUsers(ActionEvent event) {
        switchScene(event, "/view/admin/admin_users.fxml", "Quản lý người dùng");
    }

    @FXML
    private void showProducts(ActionEvent event) {
        switchScene(event, "/view/admin/admin_products.fxml", "Quản lý sản phẩm");
    }

    @FXML
    private void showAuctions(ActionEvent event) {
        switchScene(event, "/view/admin/admin_auctions.fxml", "Quản lý phiên đấu giá");
    }

    @FXML
    private void showPendingProducts(ActionEvent event) {
        switchScene(event, "/view/admin/admin_pending.fxml", "Duyệt sản phẩm");
    }

    @FXML
    private void showReports(ActionEvent event) {
        switchScene(event, "/view/admin/admin_reports.fxml", "Thống kê báo cáo");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        switchScene(event,"/view/login_view.fxml","Đang đăng xuất...");
    }
}