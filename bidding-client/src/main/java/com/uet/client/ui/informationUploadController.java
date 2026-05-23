package com.uet.client.ui;

import com.uet.client.network.ClientSocket;
import com.uet.common.model.user.User;
import com.uet.common.network.ImageData;
import com.uet.common.network.Response;
import com.uet.common.network.AddProductRequest; // Nam nhớ tạo file Request này ở Common nhé
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class informationUploadController {

    @FXML private TextField productNameField;
    @FXML private TextArea productDescriptionField;
    @FXML private ComboBox<String> ProductType;
    @FXML private TextField brandField;

    @FXML private StackPane dropImageZone;
    @FXML private VBox uploadPromptBox;
    @FXML private ImageView productImageView;
    @FXML private ProgressBar uploadProgressBar;

    @FXML private TextField startPriceField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;

    @FXML private Spinner<Integer> hourStartSpinner;
    @FXML private Spinner<Integer> minuteStartSpinner;
    @FXML private Spinner<Integer> hourEndSpinner;
    @FXML private Spinner<Integer> minuteEndSpinner;

    private User currentUser;
    private ImageData selectedProductImage; // Lưu duy nhất 1 ảnh sản phẩm

    public void setUser(User user) {
        this.currentUser = user;
    }

    @FXML
    public void initialize() {
        if (ProductType != null) {
            // Xóa sạch dữ liệu cũ
            ProductType.getItems().clear();

            // Nạp danh sách dựa trên các Class danh mục của Nam
            ProductType.getItems().addAll(
                    "Điện tử ",
                    "Thời trang ",
                    "Xe cộ ",
                    "Sách ",
                    "Nghệ thuật ",
                    "Nội thất ",
                    "Trò chơi "
            );

            // Mặc định chọn phần tử đầu tiên
            ProductType.getSelectionModel().selectFirst();
        }

        // Đống cấu hình Spinner và DatePicker phía dưới của Nam giữ nguyên 100% nhé...
        hourStartSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 12));
        hourEndSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 18));
        minuteStartSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));
        minuteEndSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));
        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(LocalDate.now().plusDays(1));
    }

    // 1. CLICK CHUỘT ĐỂ CHỌN 1 FILE ẢNH
    @FXML
    private void handleSelectFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn một ảnh sản phẩm duy nhất");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Hình ảnh", "*.png", "*.jpg", "*.jpeg", "*.webp")
        );

        Stage stage = (Stage) dropImageZone.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            processAndPreviewImage(file);
        }
    }

    // 2. KÉO FILE VÀO VÙNG DROP
    @FXML
    private void handleDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    // 3. THẢ FILE VÀO VÙNG DROP
    @FXML
    private void handleDragDropped(DragEvent event) {
        boolean success = false;
        if (event.getDragboard().hasFiles()) {
            List<File> files = event.getDragboard().getFiles();
            if (!files.isEmpty()) {
                // CHỈ LẤY ĐÚNG FILE ĐẦU TIÊN
                processAndPreviewImage(files.get(0));
                success = true;
            }
        }
        event.setDropCompleted(success);
        event.consume();
    }

    // Xử lý đọc Byte file ảnh đưa vào bộ nhớ đệm
    private void processAndPreviewImage(File file) {
        try {
            byte[] fileBytes = Files.readAllBytes(file.toPath());

            // Đóng gói mảng byte ảnh đơn
            this.selectedProductImage = new ImageData(
                    file.getName(),
                    Files.probeContentType(file.toPath()),
                    fileBytes
            );

            // Ẩn lớp thông báo chữ, đẩy ảnh lên preview đè khít khịt
            uploadPromptBox.setVisible(false);
            productImageView.setImage(new Image(file.toURI().toString()));

            System.out.println("📸 Client: Đã nạp mảng byte của 1 ảnh duy nhất thành công!");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể đọc dữ liệu file ảnh này!", Alert.AlertType.ERROR);
        }
    }

    // XỬ LÝ KHI BẤM NÚT XÁC NHẬN (GỬI LÊN SERVER)
    @FXML
    private void handleConfirmUpload() {
        String name = productNameField.getText().trim();
        String desc = productDescriptionField.getText().trim();
        String priceText = startPriceField.getText().trim();
        String brand = brandField.getText().trim();

        // 1. Kiểm tra rỗng bắt buộc phải có ảnh sản phẩm
        if (name.isEmpty() || priceText.isEmpty() || selectedProductImage == null) {
            showAlert("Cảnh báo", "Vui lòng điền tên, giá và CHỌN 1 ẢNH SẢN PHẨM!", Alert.AlertType.WARNING);
            return;
        }

        try {
            double startPrice = Double.parseDouble(priceText);

            // 2. Gom ngày và giờ từ DatePicker + Spinner thành LocalDateTime hoàn chỉnh
            LocalDateTime startDateTime = LocalDateTime.of(
                    startDatePicker.getValue(),
                    LocalTime.of(hourStartSpinner.getValue(), minuteStartSpinner.getValue())
            );

            LocalDateTime endDateTime = LocalDateTime.of(
                    endDatePicker.getValue(),
                    LocalTime.of(hourEndSpinner.getValue(), minuteEndSpinner.getValue())
            );

            if (endDateTime.isBefore(startDateTime)) {
                showAlert("Cảnh báo", "Thời gian kết thúc không được trước thời gian bắt đầu!", Alert.AlertType.WARNING);
                return;
            }

            String selectedType = ProductType.getSelectionModel().getSelectedItem().trim();
            String itemType;
            switch (selectedType) {
                case "Điện tử":
                    itemType = "Electronics";
                    break;
                case "Thời trang":
                    itemType = "Fashion";
                    break;
                case "Xe cộ":
                    itemType = "Vehicle";
                    break;
                case "Sách":
                    itemType = "Book";
                    break;
                case "Nghệ thuật":
                    itemType = "Art";
                    break;
                case "Nội thất":
                    itemType = "Furniture";
                    break;
                case "Trò chơi":
                    itemType = "Gaming";
                    break;
                default:
                    itemType = "Item";
                    break;
            }

            // 🌟 4. ĐÓNG GÓI ĐỦ 9 THAM SỐ KHỚP KHÍT FILE COMMON MỚI SỬA
            AddProductRequest req = new AddProductRequest(
                    currentUser.getId(),
                    name,
                    desc,
                    startPrice,
                    selectedProductImage,
                    itemType,     // Thuộc tính category
                    brand,        // Thuộc tính extra_1
                    startDateTime, // Thuộc tính start_time
                    endDateTime   // Thuộc tính end_time
            );

            // 4. Thiết lập Listener hứng kết quả trả về từ Server
            java.util.function.Consumer<Object> addProductListener = new java.util.function.Consumer<>() {
                @Override
                public void accept(Object response) {
                    if (response instanceof Response res) {
                        Platform.runLater(() -> {
                            if (res.isSuccess()) {
                                showAlert("Thành công", res.getMessage(), Alert.AlertType.INFORMATION);
                                handleCancel(); // Quay về trang chủ
                            } else {
                                showAlert("Thất bại", res.getMessage(), Alert.AlertType.ERROR);
                            }
                        });
                        ClientSocket.getInstance().removeMessageListener(this);
                    }
                }
            };

            ClientSocket.getInstance().addMessageListener(addProductListener);
            ClientSocket.getInstance().send(req); // Bắn lệnh lên Server

        } catch (NumberFormatException e) {
            showAlert("Lỗi", "Giá khởi điểm nhập vào không hợp lệ!", Alert.AlertType.ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể kết nối đến hệ thống Server!", Alert.AlertType.ERROR);
        }
    }

    // NÚT HỦY: QUAY VỀ TRANG CHỦ MƯỢT MÀ KHÔNG CHỚP GIẬT
    @FXML
    private void handleCancel() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/home_view.fxml"));
            Parent root = loader.load();

            HomeController controller = loader.getController();
            controller.setUser(currentUser);

            Stage stage = (Stage) dropImageZone.getScene().getWindow();
            stage.getScene().setRoot(root); // Thay thế ruột scene cực mượt
            stage.setTitle("Trang chủ Đấu giá");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML private void handleDragExited() {} // Để trống để khớp với FXML cũ nếu có gọi

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}