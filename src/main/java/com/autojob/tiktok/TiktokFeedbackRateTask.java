package com.autojob.tiktok;

import com.autojob.base.WebDriverCallback;
import com.autojob.model.entities.AccountModel;
import com.autojob.task.BaseWebViewTask;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.lang.reflect.MalformedParametersException;
import java.util.List;

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
        try {
            delaySecond(3);
            load(URL_RATE);
            List<WebElement> listRate = checkDoneListBy(By.className("arco-tag-checkable"), "Rate");
            if (listRate == null) {
                throw new InterruptedException("Không tìm thấy list đánh giá");
            }
            listRate.get(2).click();
        } catch (InterruptedException e) {
            printException(e);
        }
    }


}
