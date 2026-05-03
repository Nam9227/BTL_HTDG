package com.uet.client.ui.admin;

import com.uet.client.model.user.User; // Import đúng package bạn vừa tạo
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class AdminUsersController {

    // Khai báo các thành phần khớp với fx:id trong FXML
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
    @FXML private TableColumn<User, String> statusColumn;

    // Danh sách gốc chứa dữ liệu người dùng
    private ObservableList<User> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // 1. Kết nối các cột của TableView với các thuộc tính trong class User
        // Lưu ý: Tê n trong PropertyValueFactoryphải khớp chính xác với tên biến trong class User
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // 2. Nạp dữ liệu mẫu để kiểm tra giao diện
        loadMockData();

        // 3. Khởi tạo giá trị cho các bộ lọc (ComboBox)
        roleFilter.setItems(FXCollections.observableArrayList("Tất cả", "ADMIN", "USER"));
        statusFilter.setItems(FXCollections.observableArrayList("Tất cả", "Hoạt động", "Bị khóa"));
    }

    private void loadMockData() {
        // Dữ liệu giả lập khớp với constructor mới của bạn
        masterData.add(new User("U001", "nguyenvana", "Nguyễn Văn A", "a@gmail.com", "0987654321", "USER", "Hoạt động"));
        masterData.add(new User("U002", "admin_tuan", "Trần Anh Tuấn", "tuan@uet.vn", "0123456789", "ADMIN", "Hoạt động"));
        masterData.add(new User("U003", "bad_boy", "Lê Văn B", "b@yahoo.com", "0999999999", "USER", "Bị khóa"));

        userTable.setItems(masterData);
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase().trim();

        // Sử dụng FilteredList để tìm kiếm mà không mất dữ liệu gốc
        FilteredList<User> filteredData = new FilteredList<>(masterData, user -> {
            if (searchText.isEmpty()) return true;

            return user.getUsername().toLowerCase().contains(searchText)
                    || user.getFullName().toLowerCase().contains(searchText)
                    || user.getEmail().toLowerCase().contains(searchText);
        });

        userTable.setItems(filteredData);
    }

    @FXML
    private void handleLockUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.setStatus("Bị khóa");
            userTable.refresh(); // Cập nhật lại dòng hiển thị trên bảng
        } else {
            showWarning("Vui lòng chọn một người dùng để khóa!");
        }
    }

    @FXML
    private void handleUnlockUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.setStatus("Hoạt động");
            userTable.refresh();
        }
    }

    @FXML
    private void handleDeleteUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            masterData.remove(selected);
        }
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        roleFilter.getSelectionModel().clearSelection();
        statusFilter.getSelectionModel().clearSelection();
        userTable.setItems(masterData);
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Các hàm chuyển trang (Bạn sẽ gọi FXMLLoader tại đây sau này)
    @FXML private void goDashboard() { System.out.println("Chuyển trang Thống kê"); }
    @FXML private void goProducts() { System.out.println("Chuyển trang Sản phẩm"); }
    @FXML private void goPendingProducts() { System.out.println("Chuyển trang Duyệt bài"); }
    @FXML private void goReports() { System.out.println("Chuyển trang Báo cáo"); }

    @FXML
    private void handleLogout() {
        // Đóng ứng dụng hoặc quay lại màn hình Login
        System.exit(0);
    }

    @FXML
    private void handleViewDetail() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            System.out.println("Đang xem chi tiết: " + selected.getFullName());
        }
    }
}