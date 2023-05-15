package com.autojob.pane;

import com.autojob.base.BaseController;
import com.autojob.model.entities.AccountModel;
import com.autojob.model.entities.MessageListView;
import com.autojob.shopee.ShopeeTask;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Callback;
import rx.Observer;
import rx.subjects.BehaviorSubject;

/**
 * Created by OpenYourEyes on 25/04/2023
 */
public class HistoryTask extends VBox {
    private final ListView<MessageListView> listView;

    public HistoryTask(BaseController controller) {
        listView = new ListView<>();
        AccountModel accountModel = controller.accountModel;
        Text shopName = new Text(accountModel.shopName);
        boolean disable = accountModel.lastOrderId.isEmpty();
        String textButton = disable ? "Chưa có lastOrderId" : "Gửi cảm ơn";
        Button button = new Button(textButton);
        button.setDisable(disable);
        getChildren().add(shopName);
        getChildren().add(listView);
        getChildren().add(button);
        VBox.setVgrow(listView, Priority.ALWAYS);
        button.setOnAction(event -> {
            String text = button.getText();
            if (text.equals("Gửi cảm ơn")) {
                button.setText("Đang chạy gửi cảm ơn");
                controller.startJob(ShopeeTask.JOB_SEND_THANK_YOU);
            } else {
                controller.stopJob();
                button.setText("Dừng");
            }
        });
        listenerDataChange(controller.triggerCurrentHistory());
    }

    private void listenerDataChange(BehaviorSubject<MessageListView> receiverData) {
        listView.setCellFactory(new Callback<ListView<MessageListView>, ListCell<MessageListView>>() {
            @Override
            public ListCell<MessageListView> call(ListView<MessageListView> param) {
                return new ListCell<MessageListView>() {
                    @Override
                    protected void updateItem(MessageListView item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null) {
                            setText(null);
                            setTextFill(null);
                        } else {
                            setText(item.message);
                            setTextFill(item.color);
                        }
                    }
                };
            }
        });
        receiverData
                .subscribe(new Observer<MessageListView>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }

                    @Override
                    public void onNext(MessageListView s) {
                        Platform.runLater(() -> listView.getItems().add(0, s));
                    }
                });
    }
}
