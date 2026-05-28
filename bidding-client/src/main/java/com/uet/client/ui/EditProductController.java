package com.uet.client.ui;

import com.uet.client.network.ClientSocket;
import com.uet.common.model.auction.AuctionItem;
import com.uet.common.model.user.User;
import com.uet.common.network.Response;
import javafx.application.Platform;
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.Consumer;

public class EditProductController {
    private static final Logger logger = LoggerFactory.getLogger(EditProductController.class);

    @FXML private TextField productNameField;
    @FXML private TextArea productDescriptionField;
    @FXML private ComboBox<String> ProductType;
    @FXML private TextField brandField;

    @FXML private StackPane dropImageZone;
    @FXML private VBox uploadPromptBox;
    @FXML private ImageView productImageView;

    @FXML private TextField startPriceField;
    @FXML private DatePicker endDatePicker;
    @FXML private Spinner<Integer> hourEndSpinner;
    @FXML private Spinner<Integer> minuteEndSpinner;

    private User currentUser;
    private AuctionItem targetItem;
    private byte[] updatedImageBytes;
    private Consumer<Object> myListenerInstance;

    public void setInitData(User user, AuctionItem item) {
        this.currentUser = user;
        this.targetItem = item;

        // 1. Nạp danh sách ComboBox Danh mục
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
            if (item.getCategory() != null && !item.getCategory().isBlank()) {
                String dbCategory = item.getCategory().trim();
                String displayName = switch (dbCategory) {
                    case "Electronics" -> "Điện tử";
                    case "Fashion" -> "Thời trang";
                    case "Vehicle" -> "Xe cộ";
                    case "Book" -> "Sách";
                    case "Art" -> "Nghệ thuật";
                    case "Furniture" -> "Nội thất";
                    case "Gaming" -> "Trò chơi";
                    default -> dbCategory; // Giữ nguyên nếu là danh mục tự do
                };

                boolean found = false;
                for (String cat : ProductType.getItems()) {
                    if (cat.equalsIgnoreCase(displayName)) {
                        ProductType.getSelectionModel().select(cat);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    ProductType.getItems().add(displayName);
                    ProductType.getSelectionModel().select(displayName);
                }
            } else {
                ProductType.getSelectionModel().selectFirst();
            }
        }

        // 2. Đổ toàn bộ dữ liệu cũ của phiên lên Form
        productNameField.setText(item.getProductName());
        productDescriptionField.setText(item.getDescription());
        startPriceField.setText(String.valueOf((long) item.getStartPrice()));

        // Gán dữ liệu hãng sản xuất
        if (brandField != null) {
            brandField.setText(item.getBrand() != null ? item.getBrand() : "");
        }

        if (item.getEndTime() != null) {
            endDatePicker.setValue(item.getEndTime().toLocalDate());
            hourEndSpinner.getValueFactory().setValue(item.getEndTime().getHour());
            minuteEndSpinner.getValueFactory().setValue(item.getEndTime().getMinute());
        }

        // Đổ ảnh cũ lên khung Preview
        if (item.getProductImageBytes() != null && item.getProductImageBytes().length > 0) {
            try (ByteArrayInputStream bais = new ByteArrayInputStream(item.getProductImageBytes())) {
                productImageView.setImage(new Image(bais));
                uploadPromptBox.setVisible(false);
            } catch (Exception e) {
                logger.error("Error rendering old product image: ", e);
            }
        } else {
            uploadPromptBox.setVisible(true);
        }
    }

    @FXML
    public void initialize() {
        hourEndSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 18));
        minuteEndSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));

        // Hiệu ứng Hover kéo thả mượt mà cho vùng ảnh
        dropImageZone.setOnDragEntered(e -> {
            if (e.getDragboard().hasFiles()) {
                dropImageZone.setStyle("-fx-background-color: #f0f7ff; -fx-border-color: #2980b9;");
            }
        });
        dropImageZone.setOnDragExited(e -> dropImageZone.setStyle(null));
    }

    @FXML
    private void handleConfirmUpdate() {
        String name = productNameField.getText().trim();
        String desc = productDescriptionField.getText().trim();
        String priceText = startPriceField.getText().trim();

        if (name.isEmpty() || priceText.isEmpty()) {
            showAlert("Cảnh báo", "Vui lòng nhập đầy đủ Tên sản phẩm và Giá khởi điểm!", Alert.AlertType.WARNING);
            return;
        }

        try {
            double startPrice = Double.parseDouble(priceText);
            LocalDateTime endDateTime = LocalDateTime.of(endDatePicker.getValue(),
                    LocalTime.of(hourEndSpinner.getValue(), minuteEndSpinner.getValue()));

            if (endDateTime.isBefore(LocalDateTime.now())) {
                showAlert("Cảnh báo", "Thời gian kết thúc phiên mới không được đặt ở quá khứ!", Alert.AlertType.WARNING);
                return;
            }

            // Gán dữ liệu sửa đổi vào gói tin gốc AuctionItem gửi đi
            targetItem.setProductName(name);
            targetItem.setDescription(desc);
            targetItem.setStartPrice(startPrice);
            targetItem.setEndTime(endDateTime);

            if (ProductType != null) {
                String selectedType = ProductType.getValue();
                String dbCategory = switch (selectedType) {
                    case "Điện tử" -> "Electronics";
                    case "Thời trang" -> "Fashion";
                    case "Xe cộ" -> "Vehicle";
                    case "Sách" -> "Book";
                    case "Nghệ thuật" -> "Art";
                    case "Nội thất" -> "Furniture";
                    case "Trò chơi" -> "Gaming";
                    case "Khác" -> "Other";
                    default -> selectedType; // Fallback
                };
                targetItem.setCategory(dbCategory);
            }
            if (brandField != null) {
                targetItem.setBrand(brandField.getText().trim());
            }

            if (updatedImageBytes != null) {
                targetItem.setProductImageBytes(updatedImageBytes);
            }

            // Hứng gói tin phản hồi cập nhật từ Server
            myListenerInstance = response -> {
                if (response instanceof Response res) {
                    Platform.runLater(() -> {
                        if (res.isSuccess()) {
                            showAlert("Thành công", "Đại cập nhật thông tin sản phẩm lên sàn!", Alert.AlertType.INFORMATION);
                            handleCancel();
                        } else {
                            showAlert("Thất bại", res.getMessage(), Alert.AlertType.ERROR);
                        }
                    });
                    ClientSocket.getInstance().removeMessageListener(this.myListenerInstance);
                }
            };

            ClientSocket.getInstance().addMessageListener(myListenerInstance);
            ClientSocket.getInstance().send(targetItem); // Tiến hành gửi

        } catch (NumberFormatException e) {
            showAlert("Lỗi dữ liệu", "Giá khởi điểm nhập vào bắt buộc phải là ký tự số!", Alert.AlertType.ERROR);
        } catch (Exception e) {
            logger.error("Error updating auction item: ", e);
        }
    }

    @FXML
    private void handleCancel() {
        try {
            if (myListenerInstance != null) {
                ClientSocket.getInstance().removeMessageListener(myListenerInstance);
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/my_products.fxml"));
            Parent root = loader.load();
            MyProductsController controller = loader.getController();
            controller.setUser(currentUser);
            Stage stage = (Stage) productNameField.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            logger.error("Navigation error back to MyProducts: ", e);
        }
    }

    // --- LUỒNG DUYỆT ẢNH MỚI BẰNG CHUỘT HOẶC KÉO THẢ ---
    @FXML
    private void handleSelectFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh sản phẩm mới");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Hình ảnh", "*.png", "*.jpg", "*.jpeg"));
        Stage stage = (Stage) dropImageZone.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) { processImage(file); }
    }

    @FXML private void handleDragOver(DragEvent event) { if (event.getDragboard().hasFiles()) event.acceptTransferModes(TransferMode.COPY); event.consume(); }
    @FXML private void handleDragDropped(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            processImage(event.getDragboard().getFiles().get(0));
            event.setDropCompleted(true);
        }
        event.consume();
    }

    private void processImage(File file) {
        try {
            this.updatedImageBytes = Files.readAllBytes(file.toPath());
            productImageView.setImage(new Image(file.toURI().toString()));
            uploadPromptBox.setVisible(false);
        } catch (Exception e) {
            logger.error("Error processing image file: ", e);
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