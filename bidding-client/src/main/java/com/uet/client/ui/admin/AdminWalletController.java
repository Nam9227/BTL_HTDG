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

    private final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(AdminWalletController.class.getName());

    @FXML private ComboBox<String> statusFilter;
    @FXML private TextField searchField;
    @FXML private TableView<Transaction> transactionTable;

    @FXML private TableColumn<Transaction, Long> idColumn;
    @FXML private TableColumn<Transaction, String> userIdColumn;
    @FXML private TableColumn<Transaction, String> typeColumn;
    @FXML private TableColumn<Transaction, Double> amountColumn;
    @FXML private TableColumn<Transaction, String> dateColumn;
    @FXML private TableColumn<Transaction, String> statusColumn;

    private final javafx.collections.ObservableList<Transaction> masterData = javafx.collections.FXCollections.observableArrayList();
    private javafx.collections.transformation.FilteredList<Transaction> filteredData;

    @FXML
    public void initialize() {
        logger.info("Khởi tạo màn hình Quản lý sản phẩm...");
        if (statusFilter != null) {
            statusFilter.getItems().addAll("Tất cả", "PENDING", "APPROVED", "REJECTED");
            statusFilter.getSelectionModel().selectFirst();
        }

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        filteredData = new javafx.collections.transformation.FilteredList<>(masterData, p -> true);
        transactionTable.setItems(filteredData);

        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> handleSearch(null));
        }
        if (statusFilter != null) {
            statusFilter.valueProperty().addListener((observable, oldValue, newValue) -> handleSearch(null));
        }

        loadPendingTransactions();
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String searchText = (searchField == null || searchField.getText() == null) ? "" : searchField.getText().toLowerCase().trim();
        String selectedStatus = (statusFilter == null) ? "Tất cả" : statusFilter.getValue();

        if (filteredData != null) {
            filteredData.setPredicate(transaction -> {
                boolean matchesText = searchText.isEmpty()
                        || String.valueOf(transaction.getId()).contains(searchText)
                        || (transaction.getUserId() != null && transaction.getUserId().toLowerCase().contains(searchText));

                boolean matchesStatus = selectedStatus == null || selectedStatus.equals("Tất cả")
                        || (transaction.getStatus() != null && transaction.getStatus().equalsIgnoreCase(selectedStatus));

                return matchesText && matchesStatus;
            });
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        if (searchField != null) searchField.clear();
        if (statusFilter != null) statusFilter.getSelectionModel().selectFirst();
        loadPendingTransactions();
    }

    @FXML
    private void handleViewDetail(ActionEvent event) {
        logger.info("Xem chi tiết sản phẩm đang chọn...");
    }

    private java.util.function.Consumer<Object> pendingTransactionsListener;

    private void loadPendingTransactions(){
        try {
            if (pendingTransactionsListener == null) {
                pendingTransactionsListener = response -> {
                    if (response instanceof Response res) {
                        if (res.isSuccess() && "Load pending transaction thành công".equals(res.getMessage())) {
                            List<Transaction> list = (List<Transaction>) res.getData();
                            javafx.application.Platform.runLater(() -> {
                                masterData.clear();
                                masterData.addAll(list);
                                handleSearch(null);
                            });
                        }
                    }
                };
                ClientSocket.getInstance().addMessageListener(pendingTransactionsListener);
            }

            ClientSocket.getInstance().send(new GetPendingTransactionRequest());

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
        try {
            com.uet.client.network.ClientSocket.getInstance().send("LOGOUT");
            com.uet.client.network.ClientSocket.getInstance().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        switchScene(event, "/view/login_view.fxml", "Đăng nhập hệ thống");
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
