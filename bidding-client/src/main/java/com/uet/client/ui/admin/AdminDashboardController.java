package com.uet.client.ui.admin;

import com.uet.client.model.dashboard.DashboardActivity;
import com.uet.client.network.ClientSocket;
import com.uet.common.network.AdminActivity;
import com.uet.common.network.AdminDashboardResponse;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

        
        refreshData();
    }

    private void refreshData() {
        
        java.util.function.Consumer<Object> responseListener = new java.util.function.Consumer<>() {
            @Override
            public void accept(Object response) {
                if (response instanceof AdminDashboardResponse data) {
                    
                    ClientSocket.getInstance().removeMessageListener(this);

                    
                    Platform.runLater(() -> {
                        
                        totalUsersLabel.setText(String.format("%,d", data.getTotalUsers()));
                        totalProductsLabel.setText(String.format("%,d", data.getTotalProducts()));
                        activeAuctionsLabel.setText(String.format("%,d", data.getActiveAuctions()));
                        pendingProductsLabel.setText(String.format("%,d", data.getPendingApprovals()));

                        
                        List<DashboardActivity> fxList = new ArrayList<>();
                        for (AdminActivity act : data.getRecentActivities()) {
                            fxList.add(new DashboardActivity(act.getTime(), act.getAction(), act.getTarget(), act.getStatus()));
                        }

                        
                        recentActivityTable.setItems(FXCollections.observableArrayList(fxList));
                    });
                }
            }
        };

        
        ClientSocket.getInstance().addMessageListener(responseListener);

        
        try {
            ClientSocket.getInstance().send("REQUEST_ADMIN_DASHBOARD");
        } catch (IOException e) {
            System.err.println("[Lỗi Mạng] Không thể lấy dữ liệu Dashboard Admin: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void switchScene(ActionEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);

            if (fxmlPath.contains("login_view.fxml")) {
                com.uet.client.util.TransitionUtils.applyFadeIn(root);
                stage.setTitle("Đăng nhập hệ thống");
                stage.setMaximized(false);
                stage.setWidth(850);
                stage.setHeight(500);
                stage.centerOnScreen();
            } else {
                stage.setTitle(title);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goDashboard(ActionEvent event) {
        refreshData();
    }

    @FXML
    private void goUsers(ActionEvent event) {
        switchScene(event, "/view/admin/admin_users.fxml", "Quản lý người dùng");
    }

    @FXML
    private void goProducts(ActionEvent event) {
        switchScene(event, "/view/admin/admin_wallet.fxml", "Quản lý sản phẩm");
    }

    @FXML
    private void goApprove(ActionEvent event) {
        switchScene(event, "/view/admin/admin_approve.fxml", "Quản lý phiên đấu giá");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            ClientSocket.getInstance().send("LOGOUT");
            ClientSocket.getInstance().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        switchScene(event, "/view/login_view.fxml", "Đăng nhập hệ thống");
    }
}