package com.uet.client.ui.admin;

import com.uet.client.network.ClientSocket;
import com.uet.common.model.auction.AuctionItem;
import com.uet.common.network.ForceEndRequest;
import com.uet.common.network.Response;
import com.uet.common.network.GetPendingAuctionsRequest;
import com.uet.common.network.ApproveAuctionRequest;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.List;

public class AdminAuctionsController {
    private static final Logger logger = LoggerFactory.getLogger(AdminAuctionsController.class);

    @FXML private TableView<AuctionItem> approvalTable;
    @FXML private TableColumn<AuctionItem, String> idColumn;
    @FXML private TableColumn<AuctionItem, String> nameColumn;
    @FXML private TableColumn<AuctionItem, String> sellerColumn;
    @FXML private TableColumn<AuctionItem, String> priceColumn;
    @FXML private TableColumn<AuctionItem, String> statusColumn;
    @FXML private TableColumn<AuctionItem, String> descriptionColumn;
    @FXML private TableColumn<AuctionItem, Void> actionColumn;

    @FXML private Label statusMessageLabel;

    @FXML private javafx.scene.control.TextField searchField;
    @FXML private javafx.scene.control.ComboBox<String> statusFilter;

    private final javafx.collections.ObservableList<AuctionItem> masterData = javafx.collections.FXCollections.observableArrayList();
    private javafx.collections.transformation.FilteredList<AuctionItem> filteredData;

    private final DecimalFormat moneyFormat = new DecimalFormat("#,###đ");

    @FXML
    public void initialize() {
        if (statusFilter != null) {
            statusFilter.getItems().addAll("Tất cả", "PENDING", "ACTIVE", "RUNNING", "FINISHED", "REJECTED");
            statusFilter.getSelectionModel().selectFirst();
        }
        
        idColumn.setCellValueFactory(new PropertyValueFactory<>("auctionId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        sellerColumn.setCellValueFactory(new PropertyValueFactory<>("sellerId"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        priceColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(moneyFormat.format(cellData.getValue().getStartPrice()))
        );

        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    String display = item;
                    String color = "#111827"; 
                    if ("PENDING".equalsIgnoreCase(item)) {
                        display = "Chờ duyệt";
                        color = "#F59E0B"; 
                    } else if ("ACTIVE".equalsIgnoreCase(item)) {
                        display = "Chờ chạy";
                        color = "#2563EB"; 
                    } else if ("RUNNING".equalsIgnoreCase(item)) {
                        display = "Đang chạy";
                        color = "#16A34A"; 
                    } else if ("FINISHED".equalsIgnoreCase(item)) {
                        display = "Đã đóng";
                        color = "#6B7280"; 
                    } else if ("REJECTED".equalsIgnoreCase(item)) {
                        display = "Từ chối";
                        color = "#DC2626"; 
                    }
                    setText(display);
                    setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
                }
            }
        });

        
        setupActionColumn();

        filteredData = new javafx.collections.transformation.FilteredList<>(masterData, p -> true);
        approvalTable.setItems(filteredData);

        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> handleSearch());
        }
        if (statusFilter != null) {
            statusFilter.valueProperty().addListener((observable, oldValue, newValue) -> handleSearch());
        }

        
        loadPendingAuctions();
    }

    @FXML
    private void handleSearch() {
        String searchText = (searchField == null || searchField.getText() == null) ? "" : searchField.getText().toLowerCase().trim();
        String selectedStatus = (statusFilter == null) ? "Tất cả" : statusFilter.getValue();

        if (filteredData != null) {
            filteredData.setPredicate(item -> {
                boolean matchesText = searchText.isEmpty()
                        || String.valueOf(item.getAuctionId()).contains(searchText)
                        || (item.getProductName() != null && item.getProductName().toLowerCase().contains(searchText))
                        || (item.getSellerId() != null && item.getSellerId().toLowerCase().contains(searchText));

                boolean matchesStatus = selectedStatus == null || selectedStatus.equals("Tất cả")
                        || (item.getStatus() != null && item.getStatus().equalsIgnoreCase(selectedStatus));

                return matchesText && matchesStatus;
            });
        }
    }

    @FXML
    private void handleRefresh(javafx.event.ActionEvent event) {
        if (searchField != null) searchField.clear();
        if (statusFilter != null) statusFilter.getSelectionModel().selectFirst();
        loadPendingAuctions();
    }

    private void loadPendingAuctions() {
        try {
            GetPendingAuctionsRequest request = new GetPendingAuctionsRequest();

            java.util.function.Consumer<Object> listener = new java.util.function.Consumer<>() {
                @Override
                public void accept(Object response) {
                    if (response instanceof Response res && res.isSuccess()) {
                        List<AuctionItem> pendingList = (List<AuctionItem>) res.getData();

                        Platform.runLater(() -> {
                            masterData.clear();
                            masterData.addAll(pendingList);
                            handleSearch();
                        });
                        ClientSocket.getInstance().removeMessageListener(this);
                    }
                }
            };

            ClientSocket.getInstance().addMessageListener(listener);
            
            new Thread(() -> {
                try {
                    ClientSocket.getInstance().send(request);
                } catch (Exception e) {
                    logger.error("Lỗi khi gửi yêu cầu danh sách đấu giá lên Server: ", e);
                    Platform.runLater(() -> showStatus("Không thể gửi yêu cầu lấy danh sách sản phẩm.", false));
                }
            }).start();

        } catch (Exception e) {
            logger.error("Lỗi chuẩn bị tải danh sách đấu giá: ", e);
            showStatus("Không thể kết nối lấy danh sách sản phẩm từ Server.", false);
        }
    }

    private void setupActionColumn() {
        Callback<TableColumn<AuctionItem, Void>, TableCell<AuctionItem, Void>> cellFactory = new Callback<> () {
            @Override
            public TableCell<AuctionItem, Void> call(final TableColumn<AuctionItem, Void> param) {
                return new TableCell<>() {
                    private final Button btnApprove = new Button("Duyệt");
                    private final Button btnReject = new Button("Từ chối");
                    private final Button btnForceEnd = new Button("Kết thúc");
                    private final Label lblFinished = new Label("Đã kết thúc");

                    {
                        
                        btnApprove.setStyle("-fx-background-color: #16A34A; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand;");
                        btnReject.setStyle("-fx-background-color: #DC2626; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand;");
                        btnForceEnd.setStyle("-fx-background-color: #F59E0B; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand;");
                        lblFinished.setStyle("-fx-text-fill: #9CA3AF; -fx-font-style: italic;");
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            AuctionItem currentItem = getTableView().getItems().get(getIndex());
                            String status = currentItem.getStatus() != null ? currentItem.getStatus() : "PENDING";

                            HBox pane = new HBox(8);
                            pane.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                            
                            if ("PENDING".equalsIgnoreCase(status)) {
                                btnApprove.setOnAction(e -> handleProcessApproval(currentItem.getAuctionId(), true));
                                btnReject.setOnAction(e -> handleProcessApproval(currentItem.getAuctionId(), false));
                                pane.getChildren().addAll(btnApprove, btnReject);

                            }
                            
                            else if ("ACTIVE".equalsIgnoreCase(status)) {
                                btnForceEnd.setText("Hủy phiên"); 
                                btnForceEnd.setOnAction(e -> handleForceEnd(currentItem.getAuctionId()));
                                pane.getChildren().add(btnForceEnd);
                            }
                            
                            else if ("RUNNING".equalsIgnoreCase(status)) {
                                btnForceEnd.setText("Kết thúc sớm");
                                btnForceEnd.setStyle("-fx-background-color: #F59E0B; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand;");
                                btnForceEnd.setOnAction(e -> handleForceEnd(currentItem.getAuctionId()));
                                pane.getChildren().add(btnForceEnd);
                            }
                            
                            else {
                                lblFinished.setText("Đã đóng");
                                lblFinished.setStyle("-fx-text-fill: #9CA3AF; -fx-font-style: italic;");
                                pane.getChildren().add(lblFinished);
                            }

                            setGraphic(pane);
                        }
                    }
                };
            }
        };
        actionColumn.setCellFactory(cellFactory);
    }

    private void handleProcessApproval(String auctionId, boolean isApproved) {
        try {
            ApproveAuctionRequest request = new ApproveAuctionRequest(auctionId, isApproved);

            java.util.function.Consumer<Object> responseListener = new java.util.function.Consumer<>() {
                @Override
                public void accept(Object msg) {
                    if (msg instanceof Response res) {
                        Platform.runLater(() -> {
                            showStatus(res.getMessage(), res.isSuccess());
                            if (res.isSuccess()) {
                                loadPendingAuctions(); 
                            }
                        });
                        ClientSocket.getInstance().removeMessageListener(this);
                    }
                }
            };

            ClientSocket.getInstance().addMessageListener(responseListener);
            ClientSocket.getInstance().send(request);

        } catch (Exception e) {
            logger.error("Lỗi gửi lệnh duyệt lên Server: ", e);
            showStatus("Lỗi gửi lệnh duyệt lên Server.", false);
        }
    }

    private void showStatus(String message, boolean success) {
        statusMessageLabel.setText(message);
        if (success) {
            statusMessageLabel.setStyle("-fx-text-fill: #16A34A; -fx-font-weight: bold;");
        } else {
            statusMessageLabel.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold;");
        }
    }

    
    @FXML private void goDashboard() { switchScene("/view/admin/admin_dashboard.fxml"); }
    @FXML private void goUsers() { switchScene("/view/admin/admin_users.fxml"); }
    @FXML private void goProducts() { switchScene("/view/admin/admin_wallet.fxml"); }

    @FXML
    private void handleLogout() {
        try {
            com.uet.client.network.ClientSocket.getInstance().send("LOGOUT");
            com.uet.client.network.ClientSocket.getInstance().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        switchScene("/view/login_view.fxml");
    }

    private void switchScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) approvalTable.getScene().getWindow();
            stage.getScene().setRoot(root);

            if (fxmlPath.contains("login_view.fxml")) {
                com.uet.client.util.TransitionUtils.applyFadeIn(root);
                stage.setTitle("Đăng nhập hệ thống");
                stage.setMaximized(false);
                stage.setWidth(850);
                stage.setHeight(500);
                stage.centerOnScreen();
            }
        } catch (Exception e) {
            logger.error("Lỗi chuyển trang: " + fxmlPath, e);
        }
    }

    private void handleForceEnd(String auctionId) {
        try {
            
            ForceEndRequest request = new ForceEndRequest(auctionId);

            
            java.util.function.Consumer<Object> listener = new java.util.function.Consumer<>() {
                @Override
                public void accept(Object response) {
                    if (response instanceof Response res) {
                        Platform.runLater(() -> {
                            if (res.isSuccess()) {
                                
                                loadPendingAuctions();
                            } else {
                                System.out.println("Lỗi từ Server: " + res.getMessage());
                            }
                        });
                        
                        ClientSocket.getInstance().removeMessageListener(this);
                    }
                }
            };

            
            ClientSocket.getInstance().addMessageListener(listener);
            ClientSocket.getInstance().send(request);

        } catch (Exception e) {
            logger.error("Lỗi khi ép kết thúc phiên đấu giá sớm: ", e);
        }
    }
}