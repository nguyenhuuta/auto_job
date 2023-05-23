package com.autojob;

import com.autojob.api.ApiManager;
import com.autojob.api.RequestQueue;
import com.autojob.base.BaseController;
import com.autojob.base.WebDriverCallback;
import com.autojob.model.entities.AccountModel;
import com.autojob.model.entities.BaseResponse;
import com.autojob.model.entities.MessageListView;
import com.autojob.pane.HistoryListBox;
import com.autojob.shopee.ShopeeController;
import com.autojob.tiktok.TiktokController;
import com.autojob.utils.Logger;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import retrofit2.Call;
import rx.Observable;
import rx.Observer;
import rx.schedulers.Schedulers;

import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by OpenYourEyes on 25/04/2023
 */
public class HomeController implements Initializable, WebDriverCallback {
    public TabPane tabEcommerce;
    public HBox containerShopee;


    public VBox containerTiktok;
    public ListView<MessageListView> accountTiktok;
    public HBox actionTiktok;
    public ListView<String> needLogin;
    private final Map<String, BaseController> controllers = new HashMap<>();
    private final Map<String, Button> buttonMapChrome = new HashMap<>();
    List<AccountModel> shopees = new ArrayList<>();
    List<AccountModel> tiktoks = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        getAccounts();
    }


    void getAccounts() {
        Call<BaseResponse<List<AccountModel>>> call = ApiManager.GOOGLE_ENDPOINT.getAccountShopee(ApiManager.URL_GOOGLE_SHEET);
        RequestQueue.getInstance().enqueueRequest(call, new RequestQueue.IRequestCallback<List<AccountModel>>() {
            @Override
            public void onSuccess(List<AccountModel> response) {
                if (response == null) {
                    Logger.info("Account NULL");
                    return;
                }
                Logger.info("Account: " + response.size());
                shopees = response.stream().filter(e -> e.type == 1).collect(Collectors.toList());
                tiktoks = response.stream().filter(e -> e.type == 2).collect(Collectors.toList());
                createViewShopee();
                createViewTiktok();
            }

            @Override
            public void onFailure(String message) {
                Logger.d("DKS", message);
            }
        });
    }

    private void createViewShopee() {
        Logger.info("createViewShopee");
        for (AccountModel account : shopees) {
            HistoryListBox child = new HistoryListBox(new ShopeeController(account));
            containerShopee.getChildren().add(child);
            HBox.setHgrow(child, Priority.ALWAYS);
        }
    }

    private void createViewTiktok() {
        accountTiktok.setCellFactory(new Callback<ListView<MessageListView>, ListCell<MessageListView>>() {
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
        int numberAccounts = tiktoks.size();
        Observable.interval(0, 10, TimeUnit.SECONDS)
                .takeWhile(number -> number < numberAccounts)
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onCompleted() {
                        Logger.info("Khởi tạo hoàn tất");
                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }

                    @Override
                    public void onNext(Long aLong) {
                        int index = Math.toIntExact(aLong);
                        AccountModel account = tiktoks.get(index);
                        BaseController controller = new TiktokController(account, HomeController.this);
                        controllers.put(account.shopName, controller);
                        Button button = new Button(account.shopName);
                        button.setUserData(account.shopName);
                        button.setOnAction(event -> controllers.get(account.shopName).bringDriverToFront());
                        buttonMapChrome.put(account.rowId, button);
                        actionTiktok.getChildren().add(button);
                    }
                });
    }


    public void runTiktok(ActionEvent event) {
        int numberAccounts = tiktoks.size();
        Observable.interval(0, 5, TimeUnit.SECONDS)
                .takeWhile(number -> number < numberAccounts)
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onCompleted() {
                        Logger.info("Khởi tạo hoàn tất");
                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }

                    @Override
                    public void onNext(Long aLong) {
                        try {
                            int index = Math.toIntExact(aLong);
                            AccountModel account = tiktoks.get(index);
                            BaseController controller = new TiktokController(account, HomeController.this);
                            controller.runNow();
                            controllers.put(account.shopName, controller);
                            Button button = new Button("Login " + account.shopName);
                            button.setUserData(account.shopName);
                            button.setOnAction(event -> controllers.get(account.shopName).bringDriverToFront());
                            buttonMapChrome.put(account.rowId, button);
                            Platform.runLater(() -> {
                                actionTiktok.getChildren().add(button);
                            });
                        } catch (Exception ig) {
                            ig.printStackTrace();
                        }
                    }
                });


//        Logger.info("RUNTOK");
//        for (Map.Entry<String, BaseController> entry : controllers.entrySet()) {
//            entry.getValue().runNow();
//        }
    }

    @Override
    public void updateListView(int type, MessageListView message) {
        Platform.runLater(() -> {
            if (type == 1) { // Shopee

            } else if (type == 2) { //Tiktok
                accountTiktok.getItems().add(0, message);
            }
        });

    }

    @Override
    public void triggerLogin(AccountModel shop, boolean needLogin) {
        Platform.runLater(() -> {
            int type = shop.type;
            if (type == 1) { // Shopee

            } else if (type == 2) { //Tiktok
                Button button = buttonMapChrome.get(shop.rowId);
                if (button == null) {
                    Logger.error("Button is NULL");
                    return;
                }
                if (needLogin) {
                    button.setText("Cần login");
                    button.setStyle("-fx-text-fill: red");
                } else {
                    button.setText(shop.shopName);
                    button.setStyle("-fx-text-fill: black");
                }
            }
        });

    }
}
