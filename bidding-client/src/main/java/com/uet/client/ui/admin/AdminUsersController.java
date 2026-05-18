package com.uet.client.ui.admin;

import com.uet.common.model.user.User;
import com.uet.common.model.user.Role;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("active"));

        loadMockData();

        roleFilter.setItems(FXCollections.observableArrayList("Tất cả", "ADMIN", "BIDDER", "SELLER"));
        statusFilter.setItems(FXCollections.observableArrayList("Tất cả", "Hoạt động", "Bị khóa"));
    }

    private void loadMockData() {
        masterData.add(new User("U001", "nguyenvana", "Nguyễn Văn A", "a@gmail.com", "0987654321", Role.BIDDER, new BigDecimal("0"), true));
        masterData.add(new User("U002", "admin_tuan", "Trần Anh Tuấn", "tuan@uet.vn", "0123456789", Role.ADMIN, new BigDecimal("0"), true));
        masterData.add(new User("U003", "bad_boy", "Lê Văn B", "b@yahoo.com", "0999999999", Role.SELLER, new BigDecimal("0"), false));

        userTable.setItems(masterData);
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase().trim();

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
            selected.setActive(false);
            userTable.refresh();
        } else {
            showWarning("Vui lòng chọn một người dùng để khóa!");
        }
    }

    @FXML
    private void handleUnlockUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.setActive(true);
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

    @FXML private void goDashboard() { System.out.println("Chuyển trang Thống kê"); }
    @FXML private void goProducts() { System.out.println("Chuyển trang Sản phẩm"); }
    @FXML private void goPendingProducts() { System.out.println("Chuyển trang Duyệt bài"); }
    @FXML private void goReports() { System.out.println("Chuyển trang Báo cáo"); }

    @FXML
    private void handleLogout() {
        System.exit(0);
    }

    @FXML
    private void handleViewDetail() {
    }
}