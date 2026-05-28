package com.uet.client.ui.admin;

import com.uet.client.network.ClientSocket;
import com.uet.common.model.transaction.Transaction;
import com.uet.common.network.ApproveTransactionRequest;
import com.uet.common.network.GetPendingTransactionRequest;
import com.uet.common.network.Response;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public class AdminWalletController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;

    @FXML private TableView<String[]> productTable;
    @FXML private TableColumn<Transaction, Long> idColumn;
    @FXML private TableColumn<Transaction, String> userIdColumn;
    @FXML private TableColumn<Transaction, String> typeColumn;
    @FXML private TableColumn<Transaction, Double> amountColumn;
    @FXML private TableColumn<Transaction, String> dateColumn;
    @FXML private TableColumn<String[], String> createdAtColumn;
    @FXML private TableColumn<Transaction, String> statusColumn;

    @FXML
    public void initialize() {
        System.out.println("Khởi tạo màn hình Quản lý sản phẩm...");
        if (statusFilter != null) {
            statusFilter.getItems().addAll("Tất cả", "Chờ duyệt", "Đang đấu giá", "Đã bán", "Bị từ chối");
        }
        idColumn.setCellValueFactory(
                new PropertyValueFactory<>("id")
        );

        userIdColumn.setCellValueFactory(
                new PropertyValueFactory<>("userId")
        );

        typeColumn.setCellValueFactory(
                new PropertyValueFactory<>("type")
        );

        amountColumn.setCellValueFactory(
                new PropertyValueFactory<>("amount")
        );

        dateColumn.setCellValueFactory(
                new PropertyValueFactory<>("createdAt")
        );

        statusColumn.setCellValueFactory(
                new PropertyValueFactory<>("status")
        );
        loadPendingTransactions();
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String informationSearch = searchField.getText();
        String selectedStatus = statusFilter.getValue();
        System.out.println("Đang tìm kiếm sản phẩm với từ khóa: " + informationSearch + " | Trạng thái: " + selectedStatus);
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadPendingTransactions();
    }

    @FXML
    private void handleViewDetail(ActionEvent event) {
        System.out.println("Xem chi tiết sản phẩm đang chọn...");
    }

    @FXML
    private TableView<Transaction> transactionTable;
    private void loadPendingTransactions(){
        try {

            ClientSocket.getInstance().send(
                    new GetPendingTransactionRequest()
            );

            ClientSocket.getInstance().addMessageListener(response -> {

                if (response instanceof Response res) {

                    if (res.isSuccess()) {

                        List<Transaction> list =
                                (List<Transaction>) res.getData();
                        System.out.println(list.size());


                        Platform.runLater(() -> {
                            System.out.println(list.get(0).getAmount());

                            transactionTable
                                    .getItems()
                                    .setAll(list);
                        });
                    }
                }
            });

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
    @FXML
    private void handleApprove() {

        Transaction transaction =
                transactionTable
                        .getSelectionModel()
                        .getSelectedItem();

        if (transaction == null) {

            showAlert(
                    "Lỗi",
                    "Hãy chọn giao dịch cần duyệt!",
                    Alert.AlertType.INFORMATION
            );

            return;
        }

        try {

            ApproveTransactionRequest req =
                    new ApproveTransactionRequest(
                            transaction.getId()
                    );

            ClientSocket.getInstance().send(req);

            showAlert(
                    "Thông báo",
                    "Đã duyệt thành công",
                    Alert.AlertType.INFORMATION
            );

            loadPendingTransactions();

        } catch (Exception e) {

            e.printStackTrace();
        }

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

            stage.getScene().setRoot(root);

            stage.setTitle(title);

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
    private void goApprove(ActionEvent event) {
        switchScene(event, "/view/admin/admin_approve.fxml", "Quản lý phiên đấu giá");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        switchScene(event,"/view/login_view.fxml","Đang đăng xuất...");
    }
    private void showAlert(String title,
                           String text,
                           Alert.AlertType type) {

        Alert alert = new Alert(type);

        alert.setTitle(title);

        alert.setHeaderText(null);

        alert.setContentText(text);

        alert.showAndWait();
    }
}
