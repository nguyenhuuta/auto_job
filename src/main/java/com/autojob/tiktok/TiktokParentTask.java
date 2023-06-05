package com.autojob.tiktok;

import com.autojob.base.WebDriverCallback;
import com.autojob.model.entities.AccountModel;
import com.autojob.model.entities.ChromeSetting;
import com.autojob.model.entities.MessageListView;
import com.autojob.task.BaseWebViewTask;
import com.autojob.utils.Logger;
import com.autojob.utils.TimeUtils;
import com.autojob.utils.WebDriverUtils;
import javafx.scene.paint.Color;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Calendar;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Created by OpenYourEyes on 04/05/2023
 */
public class TiktokParentTask extends TimerTask {
    static final String ENDPOINT = "https://seller-vn.tiktok.com/";
    static final String URL_LOGIN = TiktokParentTask.ENDPOINT + "account/login";
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    BaseWebViewTask orderDetailTask;
    BaseWebViewTask feedbackRating;


    private final AccountModel accountModel;
    private final WebDriverCallback webDriverCallback;

    public TiktokParentTask(AccountModel accountModel, WebDriverCallback callback) {
        this.accountModel = accountModel;
        this.webDriverCallback = callback;
        orderDetailTask = new TiktokOrderDetailTask(accountModel, webDriverCallback);
        feedbackRating = new TiktokFeedbackRateTask(accountModel, webDriverCallback);
    }

    @Override
    public void run() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        System.out.println("Giờ hiện tại " + hour);
        if (hour < 8 || hour > 22) {
            orderDetailTask.updateListView("Ngoài giờ hoạt động 8h -> 22h");
            return;
        }
//        orderDetailTask.run();
        feedbackRating.run();

        String text = "LẦN CHẠY TỚI VÀO: " + TimeUtils.addMinute(10);
        orderDetailTask.updateListView(text);
    }


    public void startWeb() {
        String profilePath = System.getProperty("user.dir") + "/data/chrome_profile/" + accountModel.shopName;
        ChromeSetting chromeSetting = new ChromeSetting(true, 0, 0, profilePath, true);
        WebDriver webDriver = WebDriverUtils.getInstance().createWebDriver(chromeSetting);
        orderDetailTask.print("Start CHROME");
        orderDetailTask.setWebDriver(webDriver);
        feedbackRating.setWebDriver(webDriver);
        checkLogin();
    }

    private void checkLogin() {
        orderDetailTask.load(URL_LOGIN);
        orderDetailTask.delaySecond(10);
        boolean needLogin = orderDetailTask.getElementById("sso_sdk") != null;
        if (needLogin) {
            webDriverCallback.triggerLogin(accountModel, true);
            orderDetailTask.printE(accountModel.shopName + " CHƯA LOGIN, YÊU CẦU LOGIN");
            do {
                orderDetailTask.print("đợi 60s");
                orderDetailTask.delaySecond(60);
                needLogin = orderDetailTask.getElementById("sso_sdk") != null;
            } while (needLogin);
            orderDetailTask.print("60s để thao tác 1 lượt gửi tin nhắn để tắt các popup");
            orderDetailTask.delaySecond(60);
        }
        webDriverCallback.triggerLogin(accountModel, false);
        executorService.scheduleWithFixedDelay(this, 0, 10, TimeUnit.MINUTES);
    }

    public void bringWebDriverToFront() {
        try {
            orderDetailTask.bringWebDriverToFront();
        } catch (Exception ignore) {
        }
    }

}
