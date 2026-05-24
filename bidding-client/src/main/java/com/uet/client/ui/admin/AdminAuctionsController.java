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

import java.text.DecimalFormat;
import java.util.List;

public class AdminAuctionsController {

    @FXML private TableView<AuctionItem> approvalTable;
    @FXML private TableColumn<AuctionItem, String> idColumn;
    @FXML private TableColumn<AuctionItem, String> nameColumn;
    @FXML private TableColumn<AuctionItem, String> sellerColumn;
    @FXML private TableColumn<AuctionItem, String> priceColumn;
    @FXML private TableColumn<AuctionItem, String> descriptionColumn;
    @FXML private TableColumn<AuctionItem, Void> actionColumn;

    @FXML private Label statusMessageLabel;

    private final DecimalFormat moneyFormat = new DecimalFormat("#,###đ");

    @FXML
    public void initialize() {
        // Cấu hình các cột thông tin cơ bản
        idColumn.setCellValueFactory(new PropertyValueFactory<>("auctionId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        sellerColumn.setCellValueFactory(new PropertyValueFactory<>("sellerId"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        priceColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(moneyFormat.format(cellData.getValue().getStartPrice()))
        );

        // Sinh cặp nút bấm Duyệt / Từ chối động trên từng dòng
        setupActionColumn();

        // Tải dữ liệu từ server
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
                            approvalTable.getItems().clear();
                            approvalTable.getItems().addAll(pendingList);
                        });
                        ClientSocket.getInstance().removeMessageListener(this);
                    }
                }
            };

            ClientSocket.getInstance().addMessageListener(listener);
            ClientSocket.getInstance().send(request);

        } catch (Exception e) {
            e.printStackTrace();
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
                        // Style các nút
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

                            // 1. Nếu là CHỜ DUYỆT
                            if ("PENDING".equalsIgnoreCase(status)) {
                                btnApprove.setOnAction(e -> handleProcessApproval(currentItem.getAuctionId(), true));
                                btnReject.setOnAction(e -> handleProcessApproval(currentItem.getAuctionId(), false));
                                pane.getChildren().addAll(btnApprove, btnReject);

                            }
                            // 2. Nếu là ĐÃ DUYỆT (Chờ đến giờ chạy)
                            else if ("ACTIVE".equalsIgnoreCase(status)) {
                                btnForceEnd.setText("Hủy phiên"); // Admin có thể hủy nếu muốn
                                btnForceEnd.setOnAction(e -> handleForceEnd(currentItem.getAuctionId()));
                                pane.getChildren().add(btnForceEnd);
                            }
                            // 3. 🌟 NẾU ĐANG CHẠY ĐẤU GIÁ (RUNNING) -> HIỆN NÚT ÉP KẾT THÚC SỚM
                            else if ("RUNNING".equalsIgnoreCase(status)) {
                                btnForceEnd.setText("Kết thúc sớm");
                                btnForceEnd.setStyle("-fx-background-color: #F59E0B; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand;");
                                btnForceEnd.setOnAction(e -> handleForceEnd(currentItem.getAuctionId()));
                                pane.getChildren().add(btnForceEnd);
                            }
                            // 4. Nếu là ĐÃ KẾT THÚC THẬT SỰ (FINISHED / COMPLETED)
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
                                loadPendingAuctions(); // Refresh lại bảng sau khi duyệt thành công
                            }
                        });
                        ClientSocket.getInstance().removeMessageListener(this);
                    }
                }
            };

            ClientSocket.getInstance().addMessageListener(responseListener);
            ClientSocket.getInstance().send(request);

        } catch (Exception e) {
            e.printStackTrace();
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

    // --- Các hàm chuyển màn hình Sidebar có sẵn của Nam ---
    @FXML private void goDashboard() { switchScene("/view/admin/admin_dashboard.fxml"); }
    @FXML private void goUsers() { switchScene("/view/admin/admin_users.fxml"); }
    @FXML private void goProducts() { switchScene("/view/admin/admin_wallet.fxml"); }

    @FXML
    private void handleLogout() {
        switchScene("/view/login_view.fxml");
    }

    private void switchScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) approvalTable.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleForceEnd(String auctionId) {
        try {
            // 1. Tạo gói tin yêu cầu kết thúc sớm
            ForceEndRequest request = new ForceEndRequest(auctionId);

            // 2. Tạo Listener để hứng kết quả phản hồi từ Server trả về
            java.util.function.Consumer<Object> listener = new java.util.function.Consumer<>() {
                @Override
                public void accept(Object response) {
                    if (response instanceof Response res) {
                        Platform.runLater(() -> {
                            if (res.isSuccess()) {
                                // Nếu thành công, load lại bảng để cập nhật giao diện lập tức
                                loadPendingAuctions();
                            } else {
                                System.out.println("Lỗi từ Server: " + res.getMessage());
                            }
                        });
                        // Nhận xong thì gỡ Listener ra cho đỡ rác bộ nhớ
                        ClientSocket.getInstance().removeMessageListener(this);
                    }
                }
            };

            // 3. Đăng ký nhận tin và bắn gói Request lên Server
            ClientSocket.getInstance().addMessageListener(listener);
            ClientSocket.getInstance().send(request);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}