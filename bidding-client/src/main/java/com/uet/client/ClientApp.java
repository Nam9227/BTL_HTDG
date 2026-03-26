package com.uet.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.io.IOException;

public class ClientApp extends Application {

    @Override
    public void start(Stage stage) {
        try {
            Image icon = new Image(getClass().getResourceAsStream("/photo/icon_app.png"));
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login_view.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.getIcons().add(icon);
            stage.setTitle("Hệ thống Đấu giá UET - Đăng nhập");
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            System.err.println("Lỗi: Không thể nạp file FXML. Hãy kiểm tra lại đường dẫn!");
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        launch(args);
    }
}