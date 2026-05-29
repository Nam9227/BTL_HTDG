package com.uet.client.util;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.util.Duration;

public class TransitionUtils {
    /**
     * Áp dụng hiệu ứng mượt mà (Fade-in kết hợp Slide-up nhẹ) khi chuyển trang.
     * @param node Đối tượng giao diện (thường là root pane) cần tạo hiệu ứng.
     */
    public static void applyFadeIn(Node node) {
        if (node == null) return;
        
        
        node.setOpacity(0.0);
        node.setTranslateY(15);
        
        
        FadeTransition fade = new FadeTransition(Duration.millis(350), node);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        
        
        TranslateTransition translate = new TranslateTransition(Duration.millis(350), node);
        translate.setFromY(15);
        translate.setToY(0);
        
        
        ParallelTransition parallel = new ParallelTransition(fade, translate);
        parallel.play();
    }
}
