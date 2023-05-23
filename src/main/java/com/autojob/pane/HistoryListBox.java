package com.autojob.pane;

import com.autojob.base.BaseController;
import com.autojob.model.entities.AccountModel;
import com.autojob.model.entities.MessageListView;
import com.autojob.shopee.ShopeeTask;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Callback;
import rx.Observer;
import rx.subjects.BehaviorSubject;

/**
 * Created by OpenYourEyes on 25/04/2023
 */
public class HistoryListBox extends VBox {
    private final ListView<MessageListView> listView;
    private final HBox hbox;

    public HistoryListBox(BaseController controller) {
        listView = new ListView<>();
        hbox = new HBox();
        AccountModel accountModel = controller.accountModel;
        Text shopName = new Text(accountModel.shopName);
        getChildren().add(shopName);
        getChildren().add(listView);
        getChildren().add(hbox);
        VBox.setVgrow(listView, Priority.ALWAYS);
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
