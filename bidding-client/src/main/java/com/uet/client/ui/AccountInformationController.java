package com.uet.client.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;

import javax.swing.*;

public class AccountInformationController {
    @FXML private Button editBtn;
    @FXML private HBox editMode;
    @FXML private TextField FullNameField;
    @FXML private TextField EmailField;
    @FXML private TextField NumberField;
    @FXML private TextField AddressField;
    @FXML private TextField UsernameField;
    @FXML private Button avatarEditBtn;
    @FXML private ImageView avatarImage;
    @FXML private Label pathLabel;
    @FXML public void initialize() {
        editMode.managedProperty().bind(editMode.visibleProperty());
        switchToViewMode();
        Circle clip = new Circle(50, 50, 50); // Tâm x, tâm y, bán kính
        avatarImage.setClip(clip);
        String path = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        pathLabel.setText(path);
    }
    private void switchToViewMode() {
        editBtn.setVisible(true);
        editBtn.setManaged(true);

        editMode.setVisible(false);
        editMode.setManaged(false);

        FullNameField.setEditable(false);
        EmailField.setEditable(false);
        NumberField.setEditable(false);
        AddressField.setEditable(false);
        UsernameField.setEditable(false);
    }
    private void switchToEditMode(){
        editBtn.setVisible(false);
        editBtn.setManaged(false);

        editMode.setVisible(true);
        editMode.setManaged(true);

        FullNameField.setEditable(true);
        EmailField.setEditable(true);
        NumberField.setEditable(true);
        AddressField.setEditable(true);
        UsernameField.setEditable(true);
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
