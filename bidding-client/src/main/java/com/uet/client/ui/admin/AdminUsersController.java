package com.uet.client.ui.admin;

import com.uet.common.model.user.User;
import com.uet.common.model.user.Role;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.io.IOException;
import java.math.BigDecimal;

public class AdminUsersController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> roleFilter;
    @FXML private ComboBox<String> statusFilter;

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> idColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> fullNameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> phoneColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, Boolean> statusColumn;

    private ObservableList<User> masterData = FXCollections.observableArrayList();
    private FilteredList<User> filteredData;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));

        statusColumn.setCellValueFactory(new PropertyValueFactory<>("active"));
        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "Hoạt động" : "Bị khóa");
                    setStyle(item ? "-fx-text-fill: #2e7d32; -fx-font-weight: bold;" : "-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
                }
            }
        });

        loadMockData();

        filteredData = new FilteredList<>(masterData, p -> true);
        userTable.setItems(filteredData);

        roleFilter.setItems(FXCollections.observableArrayList("Tất cả", "ADMIN", "BIDDER", "SELLER"));
        statusFilter.setItems(FXCollections.observableArrayList("Tất cả", "Hoạt động", "Bị khóa"));

        roleFilter.getSelectionModel().selectFirst();
        statusFilter.getSelectionModel().selectFirst();

        searchField.textProperty().addListener((observable, oldValue, newValue) -> handleSearch());
        roleFilter.valueProperty().addListener((observable, oldValue, newValue) -> handleSearch());
        statusFilter.valueProperty().addListener((observable, oldValue, newValue) -> handleSearch());
    }

    private void loadMockData() {
        masterData.add(new User("U001", "nguyenvana", "Nguyễn Văn A", "a@gmail.com", "0987654321", Role.BIDDER, new BigDecimal("0"), true));
        masterData.add(new User("U002", "admin_tuan", "Trần Anh Tuấn", "tuan@uet.vn", "0123456789", Role.ADMIN, new BigDecimal("0"), true));
        masterData.add(new User("U003", "bad_boy", "Lê Văn B", "b@yahoo.com", "0999999999", Role.SELLER, new BigDecimal("0"), false));
    }

    @FXML
    private void handleSearch() {
        String searchText = (searchField.getText() == null) ? "" : searchField.getText().toLowerCase().trim();
        String selectedRole = roleFilter.getValue();
        String selectedStatus = statusFilter.getValue();

        filteredData.setPredicate(user -> {
            boolean matchesText = searchText.isEmpty()
                    || user.getUsername().toLowerCase().contains(searchText)
                    || user.getFullName().toLowerCase().contains(searchText)
                    || user.getEmail().toLowerCase().contains(searchText);

            boolean matchesRole = selectedRole == null || selectedRole.equals("Tất cả")
                    || user.getRole().name().equalsIgnoreCase(selectedRole);

            boolean matchesStatus = selectedStatus == null || selectedStatus.equals("Tất cả")
                    || (selectedStatus.equals("Hoạt động") && user.getActive())
                    || (selectedStatus.equals("Bị khóa") && !user.getActive());

            return matchesText && matchesRole && matchesStatus;
        });
    }

    @FXML
    private void handleLockUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (!selected.getActive()) {
                showWarning("Tài khoản này đã bị khóa từ trước!");
                return;
            }
            selected.setActive(false);
            userTable.refresh();
            handleSearch();
        } else {
            showWarning("Vui lòng chọn một người dùng để khóa!");
        }
    }

    @FXML
    private void handleUnlockUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (selected.getActive()) {
                showWarning("Tài khoản này đang ở trạng thái hoạt động!");
                return;
            }
            selected.setActive(true);
            userTable.refresh();
            handleSearch();
        } else {
            showWarning("Vui lòng chọn một người dùng để mở khóa!");
        }
    }

    @FXML
    private void handleDeleteUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Xác nhận xóa");
            confirmAlert.setHeaderText(null);
            confirmAlert.setContentText("Bạn có chắc chắn muốn xóa người dùng " + selected.getUsername() + " không?");

            if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                masterData.remove(selected);
            }
        } else {
            showWarning("Vui lòng chọn một người dùng để xóa!");
        }
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        roleFilter.getSelectionModel().selectFirst();
        statusFilter.getSelectionModel().selectFirst();
        handleSearch();
    }

    @FXML
    private void handleViewDetail() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert detailAlert = new Alert(Alert.AlertType.INFORMATION);
            detailAlert.setTitle("Chi tiết người dùng");
            detailAlert.setHeaderText("Thông tin tài khoản: " + selected.getUsername());

            String details = String.format(
                    "ID: %s\n" +
                            "Họ tên: %s\n" +
                            "Email: %s\n" +
                            "Số điện thoại: %s\n" +
                            "Vai trò: %s\n" +
                            "Số dư ví: %s VND\n" +
                            "Trạng thái: %s",
                    selected.getId(),
                    selected.getFullName(),
                    selected.getEmail(),
                    selected.getPhone(),
                    selected.getRole(),
                    selected.getBalance() != null ? selected.getBalance().toString() : "0",
                    selected.getActive() ? "Hoạt động" : "Bị khóa"
            );

            detailAlert.setContentText(details);
            detailAlert.showAndWait();
        } else {
            showWarning("Vui lòng chọn một người dùng để xem chi tiết!");
        }
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Cảnh báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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

    @FXML private void goDashboard(ActionEvent event) { switchScene(event, "/view/admin/admin_dashboard.fxml", "Dashboard"); }
    @FXML private void goProducts(ActionEvent event) { switchScene(event, "/view/admin/admin_wallet.fxml", "Quản lý sản phẩm"); }
    @FXML private void goPendingProducts(ActionEvent event) { switchScene(event, "/view/admin/admin_pending.fxml", "Duyệt sản phẩm"); }
    @FXML private void goReports(ActionEvent event) { switchScene(event, "/view/admin/admin_reports.fxml", "Thống kê báo cáo"); }

    @FXML
    private void handleLogout(ActionEvent event) {
        switchScene(event,"/view/login_view.fxml","Đang đăng xuất...");
    }
}