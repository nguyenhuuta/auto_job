package com.autojob.shopee;

import com.autojob.base.WebDriverCallback;
import com.autojob.model.entities.AccountModel;
import com.autojob.task.BaseWebViewTask;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by OpenYourEyes on 15/07/2023
 */
abstract class BaseShopeeTask extends BaseWebViewTask {
    public BaseShopeeTask(AccountModel accountModel, WebDriverCallback webDriverCallback) {
        super(accountModel, webDriverCallback);
    }

    List<WebElement> openUrlByList(String url, By by, String tag) {
        load(url);
        List<WebElement> elements;
        int count = 1;
        do {
            if (count == numberRetry) {
                printE("CheckDoneListBy: " + tag + " Hết số lần thử");
                return new ArrayList<>();
            }
            delaySecond(5);
            try {
                elements = webDriver.findElements(by);
            } catch (Exception ignore) {
                elements = null;
            }
            count++;
        } while (elements == null);
        print(tag + " load done");
        return elements;
    }
}
