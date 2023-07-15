package com.autojob;

import com.autojob.api.ApiManager;
import com.autojob.api.RequestQueue;
import com.autojob.base.BaseController;
import com.autojob.base.WebDriverCallback;
import com.autojob.model.entities.AccountModel;
import com.autojob.model.entities.BaseResponse;
import com.autojob.model.entities.MessageListView;
import com.autojob.tiktok.TiktokController;
import com.autojob.utils.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import retrofit2.Call;
import rx.Observable;
import rx.Observer;

import java.awt.*;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by OpenYourEyes on 25/04/2023
 */
public class HomeController implements Initializable, WebDriverCallback {
    public TabPane tabEcommerce;
    public HBox containerShopee;


    public VBox containerTiktok;
    //    public ListView<MessageListView> accountTiktok;
    public HBox tiktokListView;
    public HBox tiktokAccount;
    public ListView<String> needLogin;
    private final Map<String, BaseController> controllers = new HashMap<>();
    private final Map<String, Button> buttonMapChrome = new HashMap<>();
    private final Map<String, ListView<MessageListView>> mapListView = new HashMap<>();

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
                runTiktok(null);
            }

            @Override
            public void onFailure(String message) {
                Logger.d("GetAccounts Error", message);
            }
        });
    }

    private void createViewShopee() {
//        Logger.info("createViewShopee");
//        for (AccountModel account : shopees) {
//            HistoryListBox child = new HistoryListBox(new ShopeeController(account));
//            containerShopee.getChildren().add(child);
//            HBox.setHgrow(child, Priority.ALWAYS);
//        }
    }

    private void createViewTiktok() {
        for (AccountModel account : tiktoks) {
            ListView<MessageListView> listViewTiktok = new ListView<>();
            tiktokListView.getChildren().add(listViewTiktok);
            HBox.setHgrow(listViewTiktok, Priority.ALWAYS);
            mapListView.put(account.shopName, listViewTiktok);
            listViewTiktok.setCellFactory(new Callback<ListView<MessageListView>, ListCell<MessageListView>>() {
                @Override
                public ListCell<MessageListView> call(ListView<MessageListView> param) {
                    return new ListCell<MessageListView>() {
                        @Override
                        protected void updateItem(MessageListView item, boolean empty) {
                            super.updateItem(item, empty);
                            if (item == null) {
                                setText(null);
                                setTextFill(null);
                                setStyle(null);
                            } else {
                                setText(item.message);
                                setTextFill(item.color);
                                String test;
                                if (item.bgColor == null) {
                                    test = String.format("-fx-background-color: %s;font-family: Helvetica; -fx-font-size: 12px;", "white");
                                } else {
                                    test = String.format("-fx-background-color: %s;font-family: Helvetica; -fx-font-size: 12px;", "linear-gradient(to right, #fc466b, #3f5efb)");
                                }
                                setStyle(test);

                            }
                        }
                    };
                }
            });
        }
    }


    public void runTiktok(ActionEvent event) {
        int numberAccounts = tiktoks.size();
        Observable.interval(0, 5, TimeUnit.SECONDS)
                .takeWhile(number -> number < numberAccounts)
                .map(aLong -> {
                    int index = Math.toIntExact(aLong);
                    AccountModel account = tiktoks.get(index);
                    Button button = new Button(account.buttonName());
                    button.setUserData(account.shopName);
                    button.setOnAction(event1 -> controllers.get(account.shopName).bringDriverToFront());
                    buttonMapChrome.put(account.rowId, button);
                    Platform.runLater(() -> tiktokAccount.getChildren().add(button));
                    return account;
                })
                .subscribe(new Observer<AccountModel>() {
                    @Override
                    public void onCompleted() {
                        Logger.info("Khởi tạo hoàn tất");
                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }

                    @Override
                    public void onNext(AccountModel account) {
                        try {
//                            if (account.shopId == 6) {
//                                return;
//                            }
                            BaseController controller = new TiktokController(account, HomeController.this);
                            controllers.put(account.shopName, controller);
                            controller.runNow();


                        } catch (Exception ig) {
                            ig.printStackTrace();
                        }
                    }
                });
    }

    @Override
    public void updateListView(AccountModel accountModel, MessageListView message) {
        Platform.runLater(() -> {
            int type = accountModel.type;
            ListView<MessageListView> accountTiktok = mapListView.get(accountModel.shopName);
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
                    Logger.error("TriggerLogin", "Button is NULL " + shop.shopName);
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

    @Override
    public void expiredCookie(AccountModel shop) {
        Platform.runLater(() -> {
            Button button = buttonMapChrome.get(shop.rowId);
            Logger.info("ExpiredCookie", shop.shopName);
            if (button == null) {
                Logger.error("expiredCookie", "Button is NULL " + shop.shopName);
                return;
            }
            String text = shop.buttonName();
            Logger.info("buttonName", text);
            button.setText(text);
        });
    }
}
