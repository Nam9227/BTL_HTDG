package com.uet.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // Chú ý đường dẫn: /view/ vì file nằm trong resources/view/
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main_view.fxml"));
        stage.setScene(new Scene(loader.load(), 300, 200));
        stage.setTitle("UET Auction");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}