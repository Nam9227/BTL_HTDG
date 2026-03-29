package com.uet.client.ui;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.util.Duration;

public class HomeController {

    @FXML private VBox sideContent;
    @FXML private Button toggleBtn;

    private boolean isExpanded = true;

    @FXML
    public void handleToggleSidebar() {
        TranslateTransition transition = new TranslateTransition(Duration.millis(300), sideContent);

        if (isExpanded) {
            // Trượt sang phải để ẩn đi (200 là chiều rộng của sideContent)
            transition.setToX(200);
            toggleBtn.setText("<"); // Đổi icon nút thành mũi tên chỉ ra

            // Xử lý để sàn đấu giá nở ra (tùy chọn)
            transition.setOnFinished(e -> {
                sideContent.setManaged(false); // Ngừng chiếm diện tích trong BorderPane
            });

            isExpanded = false;
        } else {
            // Hiện lại
            sideContent.setManaged(true);
            transition.setToX(0);
            toggleBtn.setText(">");
            isExpanded = true;
        }
        transition.play();
    }
}