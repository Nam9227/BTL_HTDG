package com.uet.client.ui.admin;

import com.uet.common.model.user.User;
import com.uet.common.model.user.Role;
import com.uet.common.network.GetAllUsersRequest;
import com.uet.common.network.GetAllUsersResponse;
import javafx.application.Platform;
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
import java.util.List;
import java.util.function.Consumer;
import com.uet.common.network.Response;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.uet.client.network.ClientSocket;
import com.uet.client.util.ThreadPoolManager;
import com.uet.client.util.TransitionUtils;
import com.uet.common.network.DeleteUserRequest;
import com.uet.common.network.UpdateUserStatusRequest;

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

    
    @FXML private TableColumn<User, LocalDateTime> lastLoginColumn;

    private final ObservableList<User> masterData = FXCollections.observableArrayList();
    private FilteredList<User> filteredData;

    private Consumer<Object> serverMessageListener;

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
                    setStyle("");
                } else {
                    setText(item ? "Hoạt động" : "Bị khóa");
                    setStyle(item ? "-fx-text-fill: #2e7d32; -fx-font-weight: bold;" : "-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
                }
            }
        });

        
        lastLoginColumn.setCellValueFactory(new PropertyValueFactory<>("lastLoginAt"));
        lastLoginColumn.setCellFactory(column -> new TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Chưa từng đăng nhập");
                    setStyle("-fx-text-fill: #888888; -fx-font-style: italic;"); 
                } else {
                    setText(item.format(formatter));
                    
                    setStyle("-fx-text-fill: #1e293b; -fx-font-weight: bold; -fx-font-style: normal;");
                }
            }
        });

        
        filteredData = new FilteredList<>(masterData, p -> true);
        userTable.setItems(filteredData);

        roleFilter.setItems(FXCollections.observableArrayList("Tất cả", "ADMIN", "BIDDER", "SELLER"));
        statusFilter.setItems(FXCollections.observableArrayList("Tất cả", "Hoạt động", "Bị khóa"));

        roleFilter.getSelectionModel().selectFirst();
        statusFilter.getSelectionModel().selectFirst();

        searchField.textProperty().addListener((observable, oldValue, newValue) -> handleSearch());
        roleFilter.valueProperty().addListener((observable, oldValue, newValue) -> handleSearch());
        statusFilter.valueProperty().addListener((observable, oldValue, newValue) -> handleSearch());

        
        setupSocketListener();
        fetchUsersFromServer();
    }

    private void setupSocketListener() {
        serverMessageListener = message -> {
            System.out.println("[Client] Nhận gói tin từ Server: " + message.getClass().getSimpleName());

            if (message instanceof GetAllUsersResponse response) {
                List<User> userList = response.getUsers();
                Platform.runLater(() -> {
                    masterData.clear();
                    masterData.addAll(userList);
                    handleSearch();
                });
            }
            else if (message instanceof Response response) {
                Platform.runLater(() -> {
                    if (response.isSuccess()) {
                        System.out.println("[Client] Server báo lệnh thực thi thành công!");
                        fetchUsersFromServer();
                    } else {
                        showWarning(response.getMessage());
                    }
                });
            }
        };
        ClientSocket.getInstance().addMessageListener(serverMessageListener);
    }

    private void fetchUsersFromServer() {
        ThreadPoolManager.execute(() -> {
            try {
                ClientSocket.getInstance().send(new GetAllUsersRequest());
            } catch (IOException e) {
                e.printStackTrace();
                Platform.runLater(() -> showWarning("Không thể gửi yêu cầu lấy dữ liệu đến Server!"));
            }
        });
    }

    @FXML
    private void handleSearch() {
        String searchText = (searchField.getText() == null) ? "" : searchField.getText().toLowerCase().trim();
        String selectedRole = roleFilter.getValue();
        String selectedStatus = statusFilter.getValue();

        filteredData.setPredicate(user -> {
            boolean matchesText = searchText.isEmpty()
                    || (user.getUsername() != null && user.getUsername().toLowerCase().contains(searchText))
                    || (user.getFullName() != null && user.getFullName().toLowerCase().contains(searchText))
                    || (user.getEmail() != null && user.getEmail().toLowerCase().contains(searchText));

            boolean matchesRole = selectedRole == null || selectedRole.equals("Tất cả")
                    || (user.getRole() != null && user.getRole().name().equalsIgnoreCase(selectedRole));

            boolean matchesStatus = selectedStatus == null || selectedStatus.equals("Tất cả")
                    || (selectedStatus.equals("Hoạt động") && Boolean.TRUE.equals(user.getActive()))
                    || (selectedStatus.equals("Bị khóa") && Boolean.FALSE.equals(user.getActive()));

            return matchesText && matchesRole && matchesStatus;
        });
    }

    @FXML
    private void handleLockUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (selected.getRole() == Role.ADMIN) {
                showWarning("Không thể khóa tài khoản Admin!");
                return;
            }
            if (!selected.getActive()) {
                showWarning("Tài khoản này đã bị khóa từ trước!");
                return;
            }
            ThreadPoolManager.execute(() -> {
                try {
                    ClientSocket.getInstance().send(
                            new UpdateUserStatusRequest(selected.getId(), false)
                    );
                    System.out.println("[Client] Đã gửi yêu cầu KHÓA user ID: " + selected.getId());
                } catch (IOException e) {
                    e.printStackTrace();
                    Platform.runLater(() -> showWarning("Không thể kết nối đến server để khóa tài khoản!"));
                }
            });
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
            ThreadPoolManager.execute(() -> {
                try {
                    ClientSocket.getInstance().send(new UpdateUserStatusRequest(selected.getId(), true));
                    System.out.println("[Client] Đã gửi yêu cầu MỞ KHÓA user ID: " + selected.getId());
                } catch (IOException e) {
                    e.printStackTrace();
                    Platform.runLater(() -> showWarning("Không thể kết nối đến server để mở khóa tài khoản!"));
                }
            });
        } else {
            showWarning("Vui lòng chọn một người dùng để mở khóa!");
        }
    }

    @FXML
    private void handleDeleteUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (selected.getRole() == Role.ADMIN) {
                showWarning("Không thể xóa tài khoản Admin!");
                return;
            }

            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Xác nhận xóa");
            confirmAlert.setHeaderText(null);
            confirmAlert.setContentText("Bạn có chắc chắn muốn xóa người dùng " + selected.getUsername() + " không?");

            if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                ThreadPoolManager.execute(() -> {
                    try {
                        ClientSocket.getInstance().send(new DeleteUserRequest(selected.getId()));
                        System.out.println("[Client] Đã gửi yêu cầu XÓA user ID: " + selected.getId());
                        fetchUsersFromServer();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Platform.runLater(() -> showWarning("Không thể kết nối đến server để xóa tài khoản!"));
                    }
                });
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
        fetchUsersFromServer();
    }

    @FXML
    private void handleViewDetail() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert detailAlert = new Alert(Alert.AlertType.INFORMATION);
            detailAlert.setTitle("Chi tiết người dùng");
            detailAlert.setHeaderText("Thông tin tài khoản: " + selected.getUsername());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
            String lastLoginStr = selected.getLastLoginAt() != null ? selected.getLastLoginAt().format(formatter) : "Chưa từng đăng nhập";

            String details = String.format(
                    "ID: %s\n" +
                            "Họ tên: %s\n" +
                            "Email: %s\n" +
                            "Số điện thoại: %s\n" +
                            "Vai trò: %s\n" +
                            "Số dư ví: %s VND\n" +
                            "Trạng thái: %s\n" +
                            "Đăng nhập gần nhất: %s",
                    selected.getId(),
                    selected.getFullName(),
                    selected.getEmail(),
                    selected.getPhone(),
                    selected.getRole(),
                    selected.getBalance() != null ? selected.getBalance().toString() : "0",
                    selected.getActive() ? "Hoạt động" : "Bị khóa",
                    lastLoginStr
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
            if (serverMessageListener != null) {
                ClientSocket.getInstance().removeMessageListener(serverMessageListener);
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);

            if (fxmlPath.contains("login_view.fxml")) {
                TransitionUtils.applyFadeIn(root);
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

    @FXML private void goDashboard(ActionEvent event) { switchScene(event, "/view/admin/admin_dashboard.fxml", "Dashboard"); }
    @FXML private void goProducts(ActionEvent event) { switchScene(event, "/view/admin/admin_wallet.fxml", "Quản lý sản phẩm"); }
    @FXML private void goApprove(ActionEvent event) { switchScene(event, "/view/admin/admin_approve.fxml", "Duyệt sản phẩm"); }
    @FXML private void goReports(ActionEvent event) { switchScene(event, "/view/admin/admin_reports.fxml", "Thống kê báo cáo"); }

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