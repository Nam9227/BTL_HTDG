package com.uet.client.ui;

import com.uet.client.network.ClientSocket;
import com.uet.common.model.user.User;
import com.uet.common.network.ImageData;
import com.uet.common.network.Response;
import com.uet.common.network.AddProductRequest;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class InformationUploadController {
    private static final Logger logger = LoggerFactory.getLogger(InformationUploadController.class);

    // --- KHAI BÁO CÁC PHẦN TỬ MAP CHÍNH XÁC VỚI FXML MỚI ---
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
    private ImageData selectedProductImage; // Bộ nhớ đệm lưu mảng byte của duy nhất 1 ảnh

    public void setUser(User user) {
        this.currentUser = user;
    }

    @FXML
    public void initialize() {
        // 1. Nạp danh sách danh mục Tiếng Việt mượt mà
        if (ProductType != null) {
            ProductType.getItems().clear();
            ProductType.getItems().addAll(
                    "Điện tử",
                    "Thời trang",
                    "Xe cộ",
                    "Sách",
                    "Nghệ thuật",
                    "Nội thất",
                    "Trò chơi"
            );
            ProductType.getSelectionModel().selectFirst();
        }

        // 2. Cấu hình Spinner và mốc thời gian mặc định
        hourStartSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 12));
        hourEndSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 18));
        minuteStartSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));
        minuteEndSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));

        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(LocalDate.now().plusDays(1));

        // 3. HIỆU ỨNG DI CHUỘT: Tự động đổi màu Drop Zone khi kéo thả ảnh qua lại
        dropImageZone.setOnDragEntered(e -> {
            if (e.getDragboard().hasFiles()) {
                dropImageZone.setStyle("-fx-background-color: #f0f7ff; -fx-border-color: #2980b9; -fx-border-style: dashed; -fx-border-width: 2; -fx-background-radius: 12; -fx-border-radius: 12;");
            }
        });

        dropImageZone.setOnDragExited(e -> {
            dropImageZone.setStyle(null); // Quay về Class CSS mặc định trong file upload_style.css
        });
    }

    // --- LUỒNG XỬ LÝ MEDIA FILE (CLICK CHUỘT HOẶC KÉO THẢ) ---

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

    @FXML
    private void handleDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    @FXML
    private void handleDragDropped(DragEvent event) {
        boolean success = false;
        if (event.getDragboard().hasFiles()) {
            List<File> files = event.getDragboard().getFiles();
            if (!files.isEmpty()) {
                processAndPreviewImage(files.get(0)); // Chỉ bốc duy nhất file đầu tiên
                success = true;
            }
        }
        event.setDropCompleted(success);
        event.consume();
    }

    private void processAndPreviewImage(File file) {
        try {
            byte[] fileBytes = Files.readAllBytes(file.toPath());

            // Đóng gói dữ liệu ảnh vào Common Model
            this.selectedProductImage = new ImageData(
                    file.getName(),
                    Files.probeContentType(file.toPath()),
                    fileBytes
            );

            // Ẩn chữ gợi ý, đẩy ảnh lên khít khịt khung hình
            uploadPromptBox.setVisible(false);

            Image img = new Image(file.toURI().toString());
            productImageView.setImage(img);

            // Ép ảnh tự động co dãn căn giữa mượt mà trong Drop Zone
            productImageView.setPreserveRatio(true);
            productImageView.setSmooth(true);

            logger.info("📸 Đã đọc và render thành công mảng byte của ảnh: {}", file.getName());

        } catch (Exception e) {
            logger.error("Lỗi xử lý file ảnh: ", e);
            showAlert("Lỗi", "Không thể nạp dữ liệu file ảnh này!", Alert.AlertType.ERROR);
        }
    }

    // --- LUỒNG CHỐT DỮ LIỆU ĐĂNG BÁN LÊN SERVER ---

    @FXML
    private void handleConfirmUpload() {
        String name = productNameField.getText().trim();
        String desc = productDescriptionField.getText().trim();
        String priceText = startPriceField.getText().trim();
        String brand = brandField.getText().trim();

        // 1. Chặn validate rỗng lề
        if (name.isEmpty() || priceText.isEmpty() || selectedProductImage == null) {
            showAlert("Cảnh báo", "Vui lòng điền Tên sản phẩm, Giá khởi điểm và CHỌN 1 ẢNH MINH HỌA!", Alert.AlertType.WARNING);
            return;
        }

        try {
            double startPrice = Double.parseDouble(priceText);

            // 2. Đồng bộ gộp dữ liệu thời gian thành LocalDateTime hoàn chỉnh
            LocalDateTime startDateTime = LocalDateTime.of(
                    startDatePicker.getValue(),
                    LocalTime.of(hourStartSpinner.getValue(), minuteStartSpinner.getValue())
            );

            LocalDateTime endDateTime = LocalDateTime.of(
                    endDatePicker.getValue(),
                    LocalTime.of(hourEndSpinner.getValue(), minuteEndSpinner.getValue())
            );

            if (endDateTime.isBefore(startDateTime)) {
                showAlert("Cảnh báo", "Thời gian kết thúc phiên không được đặt trước thời gian bắt đầu!", Alert.AlertType.WARNING);
                return;
            }

            // 3. Mapping chuẩn ngôn ngữ quốc tế sang Server
            String selectedType = ProductType.getSelectionModel().getSelectedItem().trim();
            String itemType = switch (selectedType) {
                case "Điện tử" -> "Electronics";
                case "Thời trang" -> "Fashion";
                case "Xe cộ" -> "Vehicle";
                case "Sách" -> "Book";
                case "Nghệ thuật" -> "Art";
                case "Nội thất" -> "Furniture";
                case "Trò chơi" -> "Gaming";
                default -> "Item";
            };

            // 4. Đóng gói chuẩn chỉ 9 tham số gửi qua Socket lên Server
            AddProductRequest req = new AddProductRequest(
                    currentUser.getId(),
                    name,
                    desc,
                    startPrice,
                    selectedProductImage,
                    itemType,
                    brand,
                    startDateTime,
                    endDateTime   );

            // 5. Khởi tạo Listener bắt kết quả phản hồi của Server
            java.util.function.Consumer<Object> addProductListener = new java.util.function.Consumer<>() {
                @Override
                public void accept(Object response) {
                    if (response instanceof Response res) {
                        Platform.runLater(() -> {
                            if (res.isSuccess()) {
                                showAlert("Thành công", res.getMessage(), Alert.AlertType.INFORMATION);
                                handleCancel(); // Đăng thành công tự trả về màn hình trang chủ
                            } else {
                                showAlert("Thất bại", res.getMessage(), Alert.AlertType.ERROR);
                            }
                        });
                        ClientSocket.getInstance().removeMessageListener(this);
                    }
                }
            };

            ClientSocket.getInstance().addMessageListener(addProductListener);
            ClientSocket.getInstance().send(req); // Gửi Request

        } catch (NumberFormatException e) {
            showAlert("Cập nhật thất bại", "Giá khởi điểm đưa vào bắt buộc phải là một chuỗi ký tự số!", Alert.AlertType.ERROR);
        } catch (Exception e) {
            logger.error("Lỗi đẩy thông tin sản phẩm lên hệ thống: ", e);
            showAlert("Lỗi hệ thống", "Không thể kết nối đến máy chủ Server lúc này!", Alert.AlertType.ERROR);
        }
    }

    // NÚT HỦY: QUAY VỀ TRANG CHỦ GIỮ NGUYÊN SESSION TÀI KHOẢN
    @FXML
    private void handleCancel() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/home_view.fxml"));
            Parent root = loader.load();

            HomeController controller = loader.getController();
            controller.setUser(currentUser); // Bắn lại session user để giữ nguyên avatar/số dư ở Sidebar

            com.uet.client.util.TransitionUtils.applyFadeIn(root);

            Stage stage = (Stage) dropImageZone.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle("Trang chủ Đấu giá");
        } catch (Exception e) {
            logger.error("Lỗi chuyển scene quay về trang chủ: ", e);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}