package com.autojob.tiktok;

import com.autojob.model.entities.ChromeSetting;
import com.autojob.open_cv.OpenCVResolveCaptcha;
import com.autojob.task.BaseWebViewTask;
import com.autojob.utils.TimeUtils;
import com.autojob.utils.Utils;
import com.autojob.utils.WebDriverUtils;
import org.opencv.core.Point;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by OpenYourEyes on 17/04/2023
 */
public class TiktokController extends BaseWebViewTask {
    static final String TIKTOK_URL = "https://www.tiktok.com/vi-VN";
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    public String getTag() {
        return "TiktokController";
    }

    @Override
    public String jobName() {
        return null;
    }

    public static TiktokController newInstance(String profileName) {
        String profilePath = System.getProperty("user.dir") + "/data/chrome_profile/" + profileName;
        ChromeSetting chromeSetting = new ChromeSetting(true, 0, 0, profilePath, true);
        WebDriver webDriver = WebDriverUtils.getInstance().createWebDriver(chromeSetting);
        return new TiktokController(webDriver);
    }

    private TiktokController(WebDriver webDrive) {
        super(null);
        onSearchKey();
    }

    void onSearchKey() {
        executorService.execute(() -> {
            try {
                load(TIKTOK_URL);
                delay5to10s();
                WebElement searchInput = getElementByXpath("//input[@type='search']");
                String key = String.format("Thời trang %s", TimeUtils.getCurrentDate(TimeUtils.ddMM));
                searchInput.sendKeys(key);
                sendAction(Keys.ENTER);
                waitLoadDone();
                delay5to10s();
                Set<WebElement> list = new HashSet<>();
                Set<String> listLink = new HashSet<>();
                for (int index = 0; index < 20; index++) {
                    String idVideo = "search_top-item-user-link-" + index;
                    WebElement element = null;
                    try {
                        element = getElementById(idVideo);
                    } catch (Exception e) {

                    }
                    if (element == null) {
                        break;
                    }
                    String link = element.getAttribute("href");
                    if (link == null || link.isEmpty()) {
                        break;
                    }
                    boolean isAdd = listLink.add(link);
                    if (isAdd) {
                        list.add(element);
                    }
                }
                if (list.isEmpty()) {
                    throw new NotFoundException("Khong tim thay");
                }
                onNextVideo(listLink.iterator().next());
//
//                ((JavascriptExecutor) webDriver).executeScript
//                        ("window.open('http://google.com/','_blank');");
//                ArrayList<String> tabs = new ArrayList<String>(webDriver.getWindowHandles());
//                webDriver.switchTo().window(tabs.get(1));
//                webDriver.get("https://www.tiktok.com/@thoitrang18386/video/7195835162742082843");
//                onNextVideo(listLink.iterator().next());

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void onNextVideo(String linkShop) {
        try {
            load(linkShop);
            waitLoadDone();
            delaySecond(5);
            WebElement element = getElementByXpath("//*[@id=\"main-content-others_homepage\"]/div/div[2]/div[2]/div/div[1]/div[1]/div/div/a");
            element.click();
            waitLoadDone();
            delaySecond(Utils.randomInteger(5, 10));
            Actions actionObject = new Actions(webDriver);
            int countVideo = 1;
            boolean buttonNextEnable;
            do {
                while (status == CAPTCHA) {
                    delaySecond(5);
                    return;
                }
                actionObject.sendKeys(Keys.ARROW_DOWN);
                actionObject.perform();
                int time = Utils.randomInteger(5, 10);
                delaySecond(time);
                WebElement buttonNext = getElementByXpath("//button[@data-e2e='arrow-right']");
                buttonNextEnable = buttonNext != null && buttonNext.isDisplayed();
                System.out.println("delay " + time + " giay, VIDEO thu " + countVideo);
                countVideo++;
            } while (countVideo != 15 && buttonNextEnable);
            WebElement close = getElementByXpath("//button[@data-e2e='browse-close']");
            close.click();
        } catch (Exception e) {
            print("onNextVideo " + e);
        }
    }


    void captchaShowing(WebElement captcha) {
        System.out.println("captchaShowing");
        status = CAPTCHA;
        try {
            delaySecond(2);
            String bg = getElementById("captcha-verify-image").getAttribute("src");
            WebElement imageSlide = getElementByClassName("captcha_verify_img_slide");
            String img2 = imageSlide.getAttribute("src");
            print("BG " + getElementById("captcha-verify-image").getLocation());
            print("img " + getElementByClassName("captcha_verify_img_slide").getLocation());
            print("START DOWNLOAD");
            downloadImage(bg, "bg.jpeg");
            downloadImage(img2, "img.png");
            print("DOWNLOAD DONE");
            Point point = OpenCVResolveCaptcha.getPoint();
            WebElement element = captcha.findElement(By.className("secsdk-captcha-drag-icon"));
            Actions actions = new Actions(webDriver);
            print("captcha " + captcha.getLocation());
            print("FROM " + element.getLocation().toString());
            print("TO " + point);
            int x = (int) point.x;
            System.out.println("X = " + x);
            int space = 20;
            actions.clickAndHold(element)
                    .moveByOffset(x - 20, 0);
            int current = 0;
            while (current < space) {
                int random = Utils.randomInteger(2, 6);
                int total = current + random;
                if (total > space) {
                    random = space - current;
                }
                actions.moveByOffset(random, 0);
                current += random;
            }
            actions.release(element)
                    .build()
                    .perform();

            print(element.getLocation().toString());
        } catch (Exception e) {
            print("Exception " + e);
            e.printStackTrace();
        }


    }

    public void downloadImage(String imageUrl, String fileName) throws Exception {
        String userDir = System.getProperty("user.dir") + "/image/" + fileName;
        URL url;
        url = new URL(imageUrl);
        InputStream is = url.openStream();
        OutputStream os = new FileOutputStream(userDir);

        byte[] b = new byte[2048];
        int length;

        while ((length = is.read(b)) != -1) {
            os.write(b, 0, length);
        }
        is.close();
        os.close();

    }

    void jobCheckShowCaptcha() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    WebElement captchaElement = checkCaptcha();
                    if (captchaElement != null && status == RUNNING) {
                        captchaShowing(captchaElement);
                    }
                } catch (Exception ex) {
                }
            }
        }, 5000, 5000);
    }

    WebElement checkCaptcha() {
        return webDriver.findElement(By.id("secsdk-captcha-drag-wrapper"));
    }

    @Override
    public void run() {

    }
}

