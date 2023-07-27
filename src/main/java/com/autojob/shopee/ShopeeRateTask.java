package com.autojob.shopee;

import com.autojob.base.WebDriverCallback;
import com.autojob.model.entities.AccountModel;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * Created by OpenYourEyes on 19/07/2023
 */
class ShopeeRateTask extends BaseShopeeTask {
    static String rateURL = "portal/settings/shop/rating?category=%s&replied=1";

    public ShopeeRateTask(AccountModel accountModel, WebDriverCallback webDriverCallback) {
        super(accountModel, webDriverCallback);
    }

    @Override
    public String jobName() {
        return "Rating";
    }

    @Override
    public void run() {
        LinkedList linkedList = new LinkedList(Arrays.asList(5, 4, 3));
    }
}
