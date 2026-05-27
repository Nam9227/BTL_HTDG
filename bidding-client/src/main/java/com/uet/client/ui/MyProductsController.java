package com.uet.client.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class MyProductsController {

    @FXML private HBox sidebarContainer;
    @FXML private Button btnTabWon;
    @FXML private Button btnTabMine;
    @FXML private FlowPane productContainer;

    @FXML
    public void initialize() {
        // Tự động nạp danh sách sản phẩm trúng giải khi vừa mở giao diện
        loadWonProductsDemo();
    }

    @FXML
    void handleSwitchToWon(ActionEvent event) {
        // Đổi trạng thái hiển thị class CSS của nút chuyển mục
        btnTabWon.getStyleClass().clear();
        btnTabWon.getStyleClass().add("custom-tab-button-active");

        btnTabMine.getStyleClass().clear();
        btnTabMine.getStyleClass().add("custom-tab-button-normal");

        loadWonProductsDemo();
    }

    @FXML
    void handleSwitchToMine(ActionEvent event) {
        // Đổi trạng thái hiển thị class CSS của nút chuyển mục
        btnTabMine.getStyleClass().clear();
        btnTabMine.getStyleClass().add("custom-tab-button-active");

        btnTabWon.getStyleClass().clear();
        btnTabWon.getStyleClass().add("custom-tab-button-normal");

        loadMyProductsDemo();
    }

    private void loadWonProductsDemo() {
        productContainer.getChildren().clear();
        // Sinh sản phẩm mẫu cho Tab Đấu giá thắng (Chỉ có nút Xóa)
        renderDynamicProductCard("Laptop Asus ROG Strix", "32,500,000đ", true);
        renderDynamicProductCard("Bàn phím cơ AKKO Keycap", "1,850,000đ", true);
    }

    private void loadMyProductsDemo() {
        productContainer.getChildren().clear();
        // Sinh sản phẩm mẫu cho Tab Tự đăng bán (Có đủ nút Sửa + Xóa)
        renderDynamicProductCard("iPhone 15 Pro Max Black", "25,200,000đ", false);
        renderDynamicProductCard("Sony WH-1000XM5 Headphone", "6,500,000đ", false);
        renderDynamicProductCard("Chuột chơi game đệm khí", "1,200,000đ", false);
    }

    /**
     * Hàm kiến thiết Node Card Sản phẩm bằng Code Java thuần & Mapping Style Class từ tệp CSS
     */
    private void renderDynamicProductCard(String name, String price, boolean isWonTab) {
        // Khởi tạo container cho thẻ Card mẫu
        VBox card = new VBox(10);
        card.getStyleClass().add("product-card-node");
        card.setPrefWidth(210);
        card.setPadding(new Insets(15));

        // Nạp và xử lý cấu hình hiển thị ảnh sản phẩm
        ImageView imgView = new ImageView();
        try {
            imgView.setImage(new Image(getClass().getResourceAsStream("/com/uet/client/photo/avatar.png")));
        } catch (Exception e) {
            System.out.println("Không tìm thấy ảnh placeholder, sử dụng ảnh mặc định");
        }
        imgView.setFitWidth(180);
        imgView.setFitHeight(130);
        imgView.setPreserveRatio(true);
        imgView.getStyleClass().add("product-image-container");

        // Nhãn tiêu đề tên sản phẩm
        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("product-title-label");
        nameLabel.setWrapText(true);

        // Nhãn hiển thị giá giao dịch
        Label priceLabel = new Label(price);
        priceLabel.getStyleClass().add("product-price-label");

        // Thiết lập bộ nút điều hướng chức năng
        HBox actionBox = new HBox(8);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        actionBox.setPrefHeight(35);

        Button deleteBtn = new Button("🗑️ Xóa");
        deleteBtn.getStyleClass().add("action-button-delete");
        deleteBtn.setOnAction(e -> {
            System.out.println("Đã click hủy/xóa sản phẩm: " + name);
            productContainer.getChildren().remove(card); // Xóa trực tiếp card khỏi luồng hiển thị
        });

        // Phân cấp luồng: Nếu không thuộc tab Thắng cuộc thì chèn thêm tính năng Sửa thông tin
        if (!isWonTab) {
            Button editBtn = new Button("✏️ Sửa");
            editBtn.getStyleClass().add("action-button-edit");
            editBtn.setOnAction(e -> {
                System.out.println("Đã mở lệnh sửa đổi sản phẩm: " + name);
                // Triển khai form gọi dữ liệu sửa tại đây
            });
            actionBox.getChildren().add(editBtn);
        }

        actionBox.getChildren().add(deleteBtn);

        // Kết hợp toàn bộ cấu trúc phân cấp vào thẻ Card chính
        card.getChildren().addAll(imgView, nameLabel, priceLabel, actionBox);

        // Đẩy sản phẩm vào lưới flowpane của trang chính
        productContainer.getChildren().add(card);
    }

    // --- CÁC HÀM DI CHUYỂN SIDEBAR ---
    @FXML void handleOpenSidebar(ActionEvent event) { sidebarContainer.setVisible(true); }
    @FXML void handleCloseSidebar(ActionEvent event) { sidebarContainer.setVisible(false); }
    @FXML void handleOpenHome(ActionEvent event) { System.out.println("Quay lại màn hình chính..."); }
    @FXML void handleOpenProfile(ActionEvent event) { System.out.println("Di chuyển tới hồ sơ..."); }
    @FXML void handleOpenAddProduct(ActionEvent event) { System.out.println("Chuyển sang trang tạo sản phẩm..."); }
    @FXML void handleLogout(ActionEvent event) { System.out.println("Hệ thống đăng xuất tài khoản..."); }
}