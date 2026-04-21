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
public class HomeController extends Application {

    @FXML private VBox sideContent;
    @FXML private Button toggleBtn;

    private boolean isExpanded = true;

    /*@FXML
    public void handleToggleSidebar() {
        // Lấy chính xác chiều rộng hiện tại của thanh menu
        double width = sideContent.getWidth();
        TranslateTransition transition = new TranslateTransition(Duration.millis(300), sideContent);

        if (isExpanded) {
            // 1. Thực hiện trượt sang phải
            transition.setToX(width);
            toggleBtn.setText("<");

            // 2. Sau khi trượt xong mới biến mất hẳn để phần bên trái dãn ra
            transition.setOnFinished(e -> {
                sideContent.setVisible(false);
                sideContent.setManaged(false);
            });

            isExpanded = false;
        } else {
            // 1. Phải setManaged và Visible trước để nó có không gian trượt về
            sideContent.setManaged(true);
            sideContent.setVisible(true);

            // Đảm bảo nó bắt đầu từ vị trí đang ẩn để trượt vào mượt hơn
            sideContent.setTranslateX(width);

            transition.setToX(0);
            toggleBtn.setText(">");
            isExpanded = true;
        }
        transition.play();
    }*/
    @FXML
    public void handleToggleSidebar() {
        // 1. Lấy chiều rộng thực tế của thanh menu màu xanh
        double width = sideContent.getWidth();

        // 2. Tạo hiệu ứng di chuyển cho cả thanh menu và nút bấm
        TranslateTransition menuTransition = new TranslateTransition(Duration.millis(300), sideContent);
        TranslateTransition btnTransition = new TranslateTransition(Duration.millis(300), toggleBtn);

        // Thêm cái này để chuyển động mượt hơn (nhanh dần rồi chậm dần)
        menuTransition.setInterpolator(javafx.animation.Interpolator.EASE_BOTH);
        btnTransition.setInterpolator(javafx.animation.Interpolator.EASE_BOTH);

        if (isExpanded) {
            // TRẠNG THÁI: ĐANG MỞ -> THU VÀO
            menuTransition.setToX(width); // Đẩy menu sang phải
            btnTransition.setToX(width);  // Đẩy nút bấm đi theo menu

            toggleBtn.setText("<");

            // Sau khi chạy xong hiệu ứng mới cho ẩn hẳn
            menuTransition.setOnFinished(e -> {
                sideContent.setVisible(false);
                // sideContent.setManaged(false); // Nếu muốn phần bên trái dãn ra thì mở dòng này
            });

            isExpanded = false;
        } else {
            // TRẠNG THÁI: ĐANG ĐÓNG -> MỞ RA
            sideContent.setVisible(true);
            sideContent.setManaged(true);

            menuTransition.setToX(0); // Kéo menu về vị trí ban đầu
            btnTransition.setToX(0);  // Kéo nút bấm về vị trí ban đầu

            toggleBtn.setText(">");
            isExpanded = true;
        }

        // Chạy cả hai cùng lúc
        menuTransition.play();
        btnTransition.play();
    }
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Đảm bảo file .fxml nằm đúng đường dẫn (thường là trong folder resources)
        // Ví dụ: /com/uet/client/ui/home.fxml
        Parent root = FXMLLoader.load(getClass().getResource("/view/home_view.fxml"));
        primaryStage.setTitle("Test Sidebar");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}