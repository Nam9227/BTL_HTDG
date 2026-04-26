package com.uet.client.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

import javax.swing.*;

public class AccountInformationController {
    @FXML
    private Button editBtn;
    @FXML
    private HBox editMode;
    @FXML
    private TextField FullNameField;
    @FXML
    private TextField EmailField;
    @FXML
    private TextField NumberField;
    @FXML
    private TextField AddressField;
    @FXML
    private Button avatarPic;
    @FXML
    public void initialize() {
        editMode.managedProperty().bind(editMode.visibleProperty());
        switchToViewMode();
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
        avatarPic.setManaged(false);
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
        avatarPic.setManaged(true);
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
}
