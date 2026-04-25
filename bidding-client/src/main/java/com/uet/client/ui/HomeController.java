package com.uet.client.ui;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.util.Duration;

// Chưa commit từ đây xuống
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

//code gốc ban đầu
/*public class HomeController {

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
}*/

//code thêm vào ể chạy thử testhomcontroller
public class HomeController{

        @FXML private VBox sideContent; // Sidebar màu xanh
        @FXML private Button openBtn;   // Nút 3 gạch (nằm ngoài sidebar)
        @FXML private Button closeBtn;  // Nút X (nằm trong sidebar)

        public void initialize() {
            // Ban đầu ẩn Sidebar và nút X đi
            // Giả sử chiều rộng sidebar là 300
            sideContent.setTranslateX(300);
            sideContent.setVisible(false);
            sideContent.setManaged(false);
            closeBtn.setVisible(false);
        }

        @FXML
        public void handleOpenSidebar() {
            sideContent.setVisible(true);
            sideContent.setManaged(true);
            closeBtn.setVisible(true);

            // Hiệu ứng đẩy Sidebar vào từ phải sang trái (hoặc trái sang phải tùy layout của bạn)
            TranslateTransition menuSlide = new TranslateTransition(Duration.millis(300), sideContent);
            menuSlide.setToX(0);

            // Khi mở ra thì ẩn nút Menu đi ngay lập tức hoặc sau animation
            openBtn.setVisible(false);

            menuSlide.play();
        }

        @FXML
        public void handleCloseSidebar() {
            double width = sideContent.getWidth();
            TranslateTransition menuSlide = new TranslateTransition(Duration.millis(300), sideContent);

            // Đẩy sidebar ra ngoài (ví dụ sang phải)
            menuSlide.setToX(width);

            menuSlide.setOnFinished(e -> {
                sideContent.setVisible(false);
                sideContent.setManaged(false);
                closeBtn.setVisible(false);
                // Hiện lại nút Menu ban đầu
                openBtn.setVisible(true);
            });

            menuSlide.play();
        }
}
