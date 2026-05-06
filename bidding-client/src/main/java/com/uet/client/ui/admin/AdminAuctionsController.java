package com.uet.client.ui.admin;

import com.uet.common.model.user.User;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AdminAuctionsController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;

    @FXML private TableView<AuctionRow> auctionTable;
    @FXML private TableColumn<AuctionRow, Integer> idColumn;
    @FXML private TableColumn<AuctionRow, String> productColumn;
    @FXML private TableColumn<AuctionRow, String> currentPriceColumn;
    @FXML private TableColumn<AuctionRow, String> leaderColumn;
    @FXML private TableColumn<AuctionRow, String> endTimeColumn;
    @FXML private TableColumn<AuctionRow, String> statusColumn;

    private User currentUser;

    public void setUser(User user) {
        this.currentUser = user;
        System.out.println("Admin login: " + user.getUsername());
    }

    @FXML
    private void initialize() {
        statusFilter.setItems(FXCollections.observableArrayList(
                "Tất cả", "Đang diễn ra", "Đã kết thúc", "Đã hủy"
        ));
        statusFilter.setValue("Tất cả");

        idColumn.setCellValueFactory(data -> data.getValue().idProperty().asObject());
        productColumn.setCellValueFactory(data -> data.getValue().productProperty());
        currentPriceColumn.setCellValueFactory(data -> data.getValue().currentPriceProperty());
        leaderColumn.setCellValueFactory(data -> data.getValue().leaderProperty());
        endTimeColumn.setCellValueFactory(data -> data.getValue().endTimeProperty());
        statusColumn.setCellValueFactory(data -> data.getValue().statusProperty());

        auctionTable.setItems(FXCollections.observableArrayList(
                new AuctionRow(1, "iPhone 15 Pro", "12,000,000 đ", "nam123", "2026-05-08 20:00", "Đang diễn ra"),
                new AuctionRow(2, "Laptop Dell", "8,500,000 đ", "minh456", "2026-05-07 22:00", "Đang diễn ra"),
                new AuctionRow(3, "Tai nghe Sony", "1,200,000 đ", "chưa có", "2026-05-06 18:00", "Đã kết thúc")
        ));
    }

    @FXML
    private void goDashboard() {
        showInfo("Dashboard", "Chức năng đang làm sau.");
    }

    @FXML
    private void goUsers() {
        showInfo("Users", "Chức năng quản lý người dùng đang làm sau.");
    }

    @FXML
    private void goProducts() {
        showInfo("Products", "Chức năng quản lý sản phẩm đang làm sau.");
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText();
        String status = statusFilter.getValue();

        showInfo("Tìm kiếm", "Từ khóa: " + keyword + "\nTrạng thái: " + status);
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        statusFilter.setValue("Tất cả");
        showInfo("Làm mới", "Đã làm mới dữ liệu tạm.");
    }

    @FXML
    private void handleViewDetail() {
        AuctionRow selected = auctionTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Lỗi", "Vui lòng chọn một phiên đấu giá.");
            return;
        }

        showInfo("Chi tiết", "Sản phẩm: " + selected.getProduct()
                + "\nGiá hiện tại: " + selected.getCurrentPrice()
                + "\nNgười dẫn đầu: " + selected.getLeader());
    }

    @FXML
    private void handleEndAuction() {
        AuctionRow selected = auctionTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Lỗi", "Vui lòng chọn phiên cần kết thúc.");
            return;
        }

        selected.setStatus("Đã kết thúc");
        auctionTable.refresh();
    }

    @FXML
    private void handleCancelAuction() {
        AuctionRow selected = auctionTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Lỗi", "Vui lòng chọn phiên cần hủy.");
            return;
        }

        selected.setStatus("Đã hủy");
        auctionTable.refresh();
    }

    @FXML
    private void handleLogout() {
        Stage stage = (Stage) auctionTable.getScene().getWindow();
        stage.close();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}