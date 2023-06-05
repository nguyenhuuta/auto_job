package com.autojob.tiktok;

import com.autojob.ScreenshotFullModel;
import com.autojob.base.WebDriverCallback;
import com.autojob.model.entities.AccountModel;
import com.autojob.task.BaseWebViewTask;
import com.autojob.utils.Utils;
import com.sun.xml.internal.bind.v2.model.core.ID;
import org.apache.commons.exec.OS;
import org.apache.commons.lang3.SystemUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.lang.reflect.MalformedParametersException;
import java.util.ArrayList;
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
            if (listRate.size() == 7) {
                // 1-5: star
                // 6: đang chờ trả lời, 7 đã trả lời
                int starIndex = 4;
                WebElement waitingFeedback = listRate.get(5);
                waitingFeedback.click();
                delayBetween(3, 5);
                while (starIndex < 5) {
//                    if (starIndex > 0) {
//                        listRate.get(starIndex - 1).click();
//                        delayBetween(3, 5);
//                        print("Bỏ chọn " + (starIndex) + " sao");
//                    }
                    WebElement elementStar = listRate.get(starIndex);
                    elementStar.click();
                    print("Chọn " + (starIndex + 1) + " sao");
                    delayBetween(3, 5);
                    checkListRate(starIndex + 1);
                    starIndex++;
                }
            }

        } catch (InterruptedException e) {
            printException(e);
        }
    }


    private void checkListRate(int start) {
        try {
            List<WebElement> feedbackElements = checkDoneListBy(By.xpath("//div[contains(text(), 'Phản hồi')]"), "Rate List");
            List<WebElement> ordersDetail = new ArrayList<>();
            int size = feedbackElements.size();
            if (start <= 2) {
                ordersDetail = checkDoneListBy(By.xpath("//div[contains(@class, 'copyIcon')]"), "CopyOrderId");
                if (ordersDetail.size() != size) {
                    ScreenshotFullModel.screenShotFull(webDriver, "FeedbackNotEqualOrder");
                    throw new InterruptedException("List phản hồi không bằng list đơn hàng");
                }
            }
            print(size + " đơn hàng chưa phản hồi");
            int count = 0;
            String content = "";
            if (start <= 2) {
                content = "Chào bạn không biết là sản phẩm bên mình có vấn đề gì? Mong bạn phản hồi lại giúp shop trong phần tin nhắn để shop giải quyết cho mình nhé.";
            }
            while (count < size) {
                try {
                    if (start <= 2) {
                        print("Gửi chat cho khách hàng đánh giá " + start + " sao");
                        WebElement orderDetail = ordersDetail.get(count);
                        orderDetail.click();
                        if (SystemUtils.IS_OS_WINDOWS) {
                            webDriver.findElement(By.cssSelector("body")).sendKeys(Keys.CONTROL + "t");
                        } else {
                            webDriver.findElement(By.cssSelector("body")).sendKeys(Keys.COMMAND + "t");
                        }

                    }

                    WebElement feedbackAt = feedbackElements.get(count);
                    feedbackAt.click();
                    delaySecond(3);
                    WebElement dialog = getElementByClassName("arco-modal-content");
                    WebElement textArea = getElementByTagName(dialog, "textarea");
                    WebElement buttonSend = getElementByXpath("//span[contains(text(), 'Gửi')]");
                    if (start >= 3) {
                        content = randomFeedbackGood();
                    }
                    int random = Utils.randomInteger(0, 1);
                    if (random == 0) {
                        textArea.sendKeys(content);
                    } else {
                        simulateSendKeys(textArea, content);
                    }
                    print("Gửi chat thứ " + count + 1);
                    delayBetween(2, 3);
                    buttonSend.click();
                    delayBetween(3, 5);
                } catch (Exception e) {
                    printException(e);
                }
                count++;
            }
        } catch (InterruptedException e) {
            printException(e);
        }
    }

    private String randomFeedbackGood() {
        int random = Utils.randomInteger(0, 2);
        if (random == 0) {
            return "Shop cảm ơn sự ủng hộ của bạn ạ. Mong là bạn sẽ tiếp tục theo dõi và ủng hộ shop nha ^^";
        } else if (random == 1) {
            return "Shop cảm ơn đánh giá của bạn. Hy vọng bạn sẽ giới thiệu bạn bè đến với shop nữa nha ^^";
        } else {
            return "Shop cảm ơn phản hồi của bạn. Chúc bạn ngày làm việc vui vẻ =))";
        }
    }


}
