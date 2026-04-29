package com.uet.client.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;

public class ProfileController {
    @FXML private Button editButton;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private TextField usernameField;
    @FXML private Button avatarEditBtn;
    @FXML private ImageView avatarImage;
    @FXML public void initialize() {
        switchToViewMode();
    }
    private void switchToViewMode() {
        fullNameField.setEditable(false);
        emailField.setEditable(false);
        phoneField.setEditable(false);
        addressField.setEditable(false);
        usernameField.setEditable(false);
    }
    private void switchToEditMode(){
        fullNameField.setEditable(true);
        emailField.setEditable(true);
        phoneField.setEditable(true);
        addressField.setEditable(true);
        usernameField.setEditable(true);
    }
    @FXML
    void handleEdit(){
        switchToEditMode();
    }
    @FXML
    void handleSave(){
        System.out.println("đã lưu");
        switchToViewMode();
    }
    @FXML
    void handleCancel(){
        switchToViewMode();
    }
    @FXML
    void handleAvatarChange(){
        // 1. Tạo bộ chọn file
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh đại diện");
        // 2. Lọc định dạng ảnh
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png"));
        // 3. Hiển thị cửa sổ chọn file
        Stage stage = (Stage) avatarImage.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            // 4. Chuyển file thành Image và hiển thị
            Image image = new Image(selectedFile.toURI().toString());
            avatarImage.setImage(image);
        }
    }
    void handlePasswordChange(){}
}
