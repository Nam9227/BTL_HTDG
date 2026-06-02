package com.uet.client.ui;

import com.uet.common.network.AutoBidRequest;
import com.uet.common.network.AutoBidResponse;
import com.uet.common.network.GetAutoBidRequest;
import com.uet.common.network.GetAutoBidResponse;
import com.uet.client.network.ClientSocket;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class AutoBidDialogController {

    @FXML private TextField maxPriceField;
    @FXML private TextField stepPriceField;
    @FXML private Label errorLabel;

    private String auctionId;
    private String userId;
    private ClientSocket network;
    private Consumer<Object> listener;

    public void initData(String auctionId, String userId, ClientSocket network) {
        this.auctionId = auctionId;
        this.userId = userId;
        this.network = network;

        
        listener = new Consumer<>() {
            @Override
            public void accept(Object message) {
                if (message instanceof GetAutoBidResponse response) {
                    javafx.application.Platform.runLater(() -> {
                        if (response.isSuccess() && response.getAutoBid() != null) {
                            maxPriceField.setText(String.format("%.0f", response.getAutoBid().getMaxPrice()));
                            stepPriceField.setText(String.format("%.0f", response.getAutoBid().getStepPrice()));
                        }
                    });
                    network.removeMessageListener(this);
                }
            }
        };
        network.addMessageListener(listener);
        try {
            network.send(new GetAutoBidRequest(auctionId, userId));
        } catch (Exception e) {}
    }

    @FXML
    void handleTurnOn(ActionEvent event) {
        try {
            double maxPrice = Double.parseDouble(maxPriceField.getText().replaceAll("[,.]", ""));
            double stepPrice = Double.parseDouble(stepPriceField.getText().replaceAll("[,.]", ""));

            if (maxPrice <= 0 || stepPrice <= 0) {
                errorLabel.setText("Giá tiền phải lớn hơn 0");
                return;
            }
            if (stepPrice >= maxPrice) {
                errorLabel.setText("Bước giá phải nhỏ hơn Giá tối đa");
                return;
            }

            AutoBidRequest request = new AutoBidRequest(auctionId, userId, maxPrice, stepPrice, true);
            sendRequest(request);

        } catch (NumberFormatException e) {
            errorLabel.setText("Vui lòng nhập số hợp lệ");
        }
    }

    @FXML
    void handleTurnOff(ActionEvent event) {
        AutoBidRequest request = new AutoBidRequest(auctionId, userId, 0, 0, false);
        sendRequest(request);
    }

    private void sendRequest(AutoBidRequest request) {
        Consumer<Object> responseListener = new Consumer<>() {
            @Override
            public void accept(Object message) {
                if (message instanceof AutoBidResponse response) {
                    javafx.application.Platform.runLater(() -> {
                        if (response.isSuccess()) {
                            closeWindow();
                        } else {
                            errorLabel.setText(response.getMessage());
                        }
                    });
                    network.removeMessageListener(this);
                }
            }
        };
        network.addMessageListener(responseListener);
        try {
            network.send(request);
        } catch (Exception e) {
            errorLabel.setText("Lỗi kết nối");
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) maxPriceField.getScene().getWindow();
        stage.close();
    }
}
