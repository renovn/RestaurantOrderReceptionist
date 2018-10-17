package controller;

import database.Database;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import models.Order;
import models.OrderList;


public class Controller {
    @FXML
    private ListView<Order> orderWaitingLV;
    @FXML
    private ListView<Order> orderServedLV;
    @FXML
    private Button billBtn;
    private ClientPoint client;

    @FXML
    public void initialize() throws Exception {
        client = new ClientPoint("localhost", this);
        Database.instance().setUp();
        registerButtons();
        getExistingOrder();
        client.start();
    }

    private void registerButtons() {
        billBtn.setOnAction(event -> {
            Order selectedOrder = orderServedLV.getSelectionModel().getSelectedItem();
            Database.instance().billOrder(selectedOrder);
            orderServedLV.getItems().remove(selectedOrder);
            client.notifyServer();
        });

        orderServedLV.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Order>() {
            @Override
            public void changed(ObservableValue<? extends Order> observable, Order oldValue, Order newValue) {
                if (newValue == null)
                    billBtn.setDisable(true);
                else
                    billBtn.setDisable(false);
            }
        });
    }

    private void getExistingOrder() {
        orderWaitingLV.getItems().clear();
        orderServedLV.getItems().clear();
        orderWaitingLV.getItems().addAll(OrderList.instance().waitingOrders());
        orderServedLV.getItems().addAll(OrderList.instance().servedOrders());
    }

    public void updateOrders() {
        OrderList.instance().fetchOrders();
        Platform.runLater(() -> getExistingOrder());
    }

    public void stopClient() {
        client.setShouldStop(true);
        client.quitServer();
    }

}
