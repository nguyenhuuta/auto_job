package com.webseleniumdriver;

import com.webseleniumdriver.api.ApiManager;
import com.webseleniumdriver.api.RequestQueue;
import com.webseleniumdriver.base.BaseController;
import com.webseleniumdriver.model.entities.AccountModel;
import com.webseleniumdriver.model.entities.BaseResponse;
import com.webseleniumdriver.pane.HistoryTask;
import com.webseleniumdriver.shopee.ShopeeController;
import com.webseleniumdriver.utils.Logger;
import javafx.fxml.Initializable;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import retrofit2.Call;
import rx.subjects.PublishSubject;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by OpenYourEyes on 25/04/2023
 */
public class HomeController extends BaseController implements Initializable {
    public TabPane tabEcommerce;
    public HBox containerShopee;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        getAccountShopee();
    }


    void getAccountShopee() {
        Call<BaseResponse<List<AccountModel>>> call = ApiManager.GOOGLE_ENDPOINT.getAccountShopee(ApiManager.URL_SHOPEE);
        RequestQueue.getInstance().enqueueRequest(call, new RequestQueue.IRequestCallback<List<AccountModel>>() {
            @Override
            public void onSuccess(List<AccountModel> response) {
                createView(response);
            }

            @Override
            public void onFailure(String message) {
                Logger.d("DKS", message);
            }
        });
    }

    private void createView(List<AccountModel> listAccount) {
        for (AccountModel account : listAccount) {
            HistoryTask child = new HistoryTask(new ShopeeController(account));
            containerShopee.getChildren().add(child);
            HBox.setHgrow(child, Priority.ALWAYS);
        }
    }
}
