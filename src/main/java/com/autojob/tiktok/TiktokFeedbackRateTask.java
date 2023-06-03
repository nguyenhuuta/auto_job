package com.autojob.tiktok;

import com.autojob.base.WebDriverCallback;
import com.autojob.model.entities.AccountModel;
import com.autojob.task.BaseWebViewTask;
import org.openqa.selenium.WebElement;

/**
 * Created by OpenYourEyes on 03/06/2023
 */
class TiktokFeedbackRateTask extends BaseWebViewTask {
    String URL_RATE = "https://seller-vn.tiktok.com/product/rating";

    public TiktokFeedbackRateTask(AccountModel accountModel, WebDriverCallback webDriverCallback) {
        super(accountModel, webDriverCallback);
    }


    @Override
    public String jobName() {
        return "FEEDBACK RATE";
    }


    @Override
    public void run() {
        delaySecond(3);
        load(URL_RATE);
    }
}
