package com.uet.client.ui;

import com.uet.common.model.user.User;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class informationUploadController {
    @FXML private ComboBox<String> ProductType;
    @FXML private VBox dropImageZone;
    @FXML private HBox imageContainer;
    // Danh sách lưu trữ các file thực tế để sau này bạn gửi lên Server xử lý (Task 3)
    private final List<File> selectedFilesList = new ArrayList<>();
    private final List<String> VALID_EXTENSIONS = List.of(".jpg", ".jpeg", ".png", ".mp4");
    private final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    private final int MAX_FILES_ALLOWED = 10;
    private User currentUser;

    public void setUser(User user) {
        this.currentUser = user;
        System.out.println("Màn hình Thêm sản phẩm đã nhận diện Người bán: " + currentUser.getUsername());
    }

    @FXML
    public void initialize() {
        if (ProductType != null) {
            ProductType.getItems().addAll(
                    "Thiết bị điện tử & Máy tính",
                    "Điện thoại & Phụ kiện",
                    "Thiết bị gia dụng",
                    "Sách & Tài liệu học tập",
                    "Thời trang & Phụ kiện",
                    "Đồ sưu tầm & Giới hạn",
                    "Khác"
            );
        }
    }
    //Kéo file lướt qua vùng dropImageZone -> Đổi màu nền để báo hiệu có thể thả
    @FXML
    private void handleDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            // Highlight vùng chọn: Đổi màu viền và màu nền tối nhẹ hơn một chút
            dropImageZone.setStyle("-fx-border-color: #3f88c5; -fx-border-style: dashed; -fx-border-width: 2; -fx-background-color: #1e2530; -fx-border-radius: 5; -fx-background-radius: 5;");
            event.acceptTransferModes(TransferMode.ANY);
        }
        event.consume();
    }

    //Chuột kéo file ra khỏi vùng dropImageZone (nhưng không thả) -> Trả lại màu giao diện gốc
    @FXML
    private void handleDragExited(DragEvent event) {
        dropImageZone.setStyle("-fx-border-color: #4f5b66; -fx-border-style: dashed; -fx-border-width: 2; -fx-background-color: transparent; -fx-border-radius: 5; -fx-background-radius: 5;");
        event.consume();
    }

    //Người dùng thả chuột (Drop file) vào vùng dropImageZone
    @FXML
    private void handleDragDropped(DragEvent event) {
        var dragboard = event.getDragboard();
        boolean success = false;

        if (dragboard.hasFiles()) {
            processFiles(dragboard.getFiles());
            success = true;
        }

        event.setDropCompleted(success);
        event.consume();

        // Reset giao diện về trạng thái ban đầu sau khi hoàn tất thả
        handleDragExited(event);
    }

    //Click chuột trực tiếp vào vùng dropImageZone để mở FileChooser của hệ điều hành
    @FXML
    private void handleSelectFile(MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn Hình Ảnh / Video Đấu Giá");

        // Tạo bộ lọc định dạng file tránh người dùng chọn nhầm file zip, pdf...
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Hình ảnh & Video (*.png, *.jpg, *.mp4)", "*.png", "*.jpg", "*.jpeg", "*.mp4")
        );

        // Lấy Stage hiện tại để làm Dialog Owner
        Stage stage = (Stage) dropImageZone.getScene().getWindow();
        List<File> files = fileChooser.showOpenMultipleDialog(stage);

        if (files != null) {
            processFiles(files);
        }
    }

    private void processFiles(List<File> files) {
        for (File file : files) {
            // Kiểm tra nếu vượt quá số lượng 10 file cho phép thì dừng lại
            if (selectedFilesList.size() >= MAX_FILES_ALLOWED) {
                System.out.println("Cảnh báo: Đã đạt giới hạn tối đa 10 files.");
                break;
            }

            // Kiểm tra định dạng đuôi và dung lượng < 50MB
            if (isValidFile(file)) {
                selectedFilesList.add(file);
                System.out.println("Đã thêm file: " + file.getName());

                // Vẽ ảnh động lên giao diện
                renderImage(file);
            } else {
                System.out.println("File không hợp lệ hoặc kích thước vượt quá 50MB: " + file.getName());
            }
        }
    }


    //Validate đuôi file và kích thước

    private boolean isValidFile(File file) {
        String name = file.getName().toLowerCase();
        boolean hasValidExt = VALID_EXTENSIONS.stream().anyMatch(name::endsWith);
        boolean isUnderSize = file.length() <= MAX_FILE_SIZE;
        return hasValidExt && isUnderSize;
    }

    private void renderImage(File file) {
        try {
            // Tạo ImageView hiển thị ảnh thu nhỏ
            Image image = new Image(file.toURI().toString(), 80, 60, true, true);
            ImageView imageView = new ImageView(image);

            // Bọc ImageView vào một StackPane để dễ quản lý border bo góc giống UI mẫu
            StackPane imageWrapper = new StackPane(imageView);
            imageWrapper.setStyle("-fx-border-color: #4f5b66; -fx-border-width: 1; -fx-border-radius: 4; -fx-padding: 3; -fx-background-color: #1a222d;");

            // Thêm hiệu ứng click vào thumbnail để xóa file nếu người dùng chọn nhầm
            imageWrapper.setOnMouseClicked(e -> {
                imageContainer.getChildren().remove(imageWrapper);
                selectedFilesList.remove(file);
                System.out.println("Đã xóa file: " + file.getName());
            });

            // Đẩy vào thanh ngang chứa ảnh preview dưới vùng drop
            imageContainer.getChildren().add(imageWrapper);

        } catch (Exception e) {
            System.out.println("Không thể hiển thị thumbnail cho file: " + file.getName());
        }
    }
}
