package com.autojob.tiktok;

import com.autojob.api.ApiManager;
import com.autojob.api.RequestQueue;
import com.autojob.base.WebDriverCallback;
import com.autojob.model.entities.*;
import com.autojob.task.BaseWebViewTask;
import com.autojob.utils.Logger;
import com.autojob.utils.TimeUtils;
import com.autojob.utils.WebDriverUtils;
import javafx.scene.paint.Color;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import retrofit2.Call;

import java.util.*;
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
    BaseWebViewTask sendThanksTask;
    BaseWebViewTask getPhoneTask;


    private final AccountModel accountModel;
    private final WebDriverCallback webDriverCallback;
    private WebDriver webDriver;
    private WebDriverWait webDriverWait;

    public TiktokParentTask(AccountModel accountModel, WebDriverCallback callback) {
        this.accountModel = accountModel;
        this.webDriverCallback = callback;
    }


    private MessageListView formatMessage(String message, Color color) {
        String time = TimeUtils.getCurrentDate(TimeUtils.formatDate1);
        String content = String.format("%s - [%s] => %s", time, accountModel.shopName, message);
        return new MessageListView(content, color);
    }

    public void updateListView(String message, Color color) {
        MessageListView item = formatMessage(message, color);
        webDriverCallback.updateListView(accountModel.type, item);
    }

    public void updateListView(String message) {
        updateListView(message, null);
    }

    @Override
    public void run() {
        try {
            int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            System.out.println("Giờ hiện tại " + hour);
            if (hour < 8 || hour > 22) {
                updateListView("Ngoài giờ hoạt động 8h -> 22h");
                return;
            }
            if (getPhoneTask == null) {
                getPhoneTask = new TiktokOrderDetailTask(accountModel, webDriverCallback);
                getPhoneTask.webDriver = webDriver;
                getPhoneTask.webDriverWait = webDriverWait;
            }
            getPhoneTask.run();
        } catch (Exception e) {
            Logger.error(e.toString());
            updateListView(e.toString());
            e.printStackTrace();
        } finally {
            String text = "Lần chạy tới vào lúc " + TimeUtils.addMinute(10);
            updateListView("DONE - " + text);
        }
    }

    public void delaySecond(long time) {
        try {
            updateListView("Đợi " + time + "s");
            TimeUnit.SECONDS.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void startWeb() {
        String profilePath = System.getProperty("user.dir") + "/data/chrome_profile/" + accountModel.shopName;
        ChromeSetting chromeSetting = new ChromeSetting(true, 0, 0, profilePath, true);
        webDriver = WebDriverUtils.getInstance().createWebDriver(chromeSetting);
        webDriverWait = new WebDriverWait(webDriver, 10);
        updateListView("Start CHROME");
        checkLogin();
    }

    WebElement findElementById(String xpath) {
        try {
            return webDriver.findElement(By.id(xpath));
        } catch (Exception e) {
            Logger.warning("TiktokParentTask", "Not found element : " + xpath);
            return null;
        }
    }

    private void checkLogin() {
        webDriver.get(URL_LOGIN);
        delaySecond(10);
        boolean needLogin = findElementById("sso_sdk") != null;
        if (needLogin) {
            webDriverCallback.triggerLogin(accountModel, true);
            updateListView(accountModel.shopName + " CHƯA LOGIN, YÊU CẦU LOGIN", Color.RED);
            do {
                updateListView("đợi 60s");
                delaySecond(60);
                needLogin = findElementById("sso_sdk") != null;
            } while (needLogin);
            updateListView("60s để thao tác 1 lượt gửi tin nhắn để tắt các popup");
            delaySecond(60);
        }
        webDriverCallback.triggerLogin(accountModel, false);
        executorService.scheduleWithFixedDelay(this, 0, 10, TimeUnit.MINUTES);
    }

    public void bringWebDriverToFront() {
        try {
            webDriver.switchTo().window(webDriver.getWindowHandle());
        } catch (Exception ignore) {
        }
    }

}
