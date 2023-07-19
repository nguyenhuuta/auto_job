package com.autojob.shopee;

import com.autojob.base.BaseController;
import com.autojob.base.WebDriverCallback;
import com.autojob.model.entities.AccountModel;
import com.autojob.model.entities.ChromeSetting;
import com.autojob.model.entities.MessageListView;
import com.autojob.task.BaseWebViewTask;
import com.autojob.tiktok.*;
import com.autojob.utils.TimeUtils;
import com.autojob.utils.WebDriverUtils;
import javafx.scene.paint.Color;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import rx.subjects.BehaviorSubject;

import java.util.Calendar;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by OpenYourEyes on 17/04/2023
 */
public class ShopeeController extends BaseController {
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    BaseShopeeTask sendThankTask;

    public ShopeeController(AccountModel model, WebDriverCallback webDriverCallback) {
        super(model);
        sendThankTask = new ShopeeSendThanksTask(model, webDriverCallback);
    }

    @Override
    public void runNow() {
        new Thread(this::startWeb).start();

    }

    @Override
    public void changeJob(int jobId) {
    }

    @Override
    public void stopJob() {
    }

    @Override
    public void bringDriverToFront() {
        sendThankTask.bringWebDriverToFront();
    }

    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            int minute = Calendar.getInstance().get(Calendar.MINUTE);

            System.out.println("Giờ hiện tại " + hour);
            if (hour < 8 || hour > 21) {
                sendThankTask.updateListView("Ngoài giờ hoạt động 8h -> 22h");
                return;
            }
            if (accountModel.expired == null) {
                Set<Cookie> cookieSet = sendThankTask.webDriver.manage().getCookies();
                for (Cookie cookie : cookieSet) {
                    String code = cookie.getName();
                    if ("sso_uid_tt_ss_ads".equals(code)) {
                        accountModel.expired = cookie.getExpiry();
                        break;
                    }
                }
            }
            sendThankTask.webDriverCallback.expiredCookie(accountModel);
            sendThankTask.run();
            String text = "LẦN CHẠY TỚI VÀO: " + TimeUtils.addMinute(10);
            sendThankTask.printColor(text, Color.DARKVIOLET);
        }
    };


    public void startWeb() {
        String profilePath = System.getProperty("user.dir") + "/data/chrome_profile/" + accountModel.shopName;
        ChromeSetting chromeSetting = new ChromeSetting(true, 0, 0, profilePath, true);
        WebDriver webDriver = WebDriverUtils.getInstance().createWebDriver(chromeSetting);
        sendThankTask.print("Start CHROME");
        sendThankTask.setWebDriver(webDriver);
        checkLogin();
    }

    private void checkLogin() {
        System.out.println("CheckLogin");
        sendThankTask.load(ShopeeSendThanksTask.SHOPEE_ORDER_COMPLETE);
        sendThankTask.delaySecond(5);
        String currentUrl = sendThankTask.webDriver.getCurrentUrl();
        System.out.println(currentUrl);
        boolean needLogin = currentUrl.contains("login");
        WebDriverCallback webDriverCallback = sendThankTask.webDriverCallback;
        if (needLogin) {
            webDriverCallback.triggerLogin(accountModel, true);
            sendThankTask.printE(accountModel.shopName + " CHƯA LOGIN, YÊU CẦU LOGIN");
            do {
                sendThankTask.print("đợi 60s");
                sendThankTask.delaySecond(20);
                currentUrl = sendThankTask.webDriver.getCurrentUrl();
                System.out.println(currentUrl);
                needLogin = currentUrl.contains("login");
            } while (needLogin);
            sendThankTask.delaySecond(10);
        }
        webDriverCallback.triggerLogin(accountModel, false);
        executorService.scheduleWithFixedDelay(timerTask, 0, 10, TimeUnit.MINUTES);
    }

}



