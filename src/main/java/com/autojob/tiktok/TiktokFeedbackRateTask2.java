package com.autojob.tiktok;

import com.autojob.ScreenshotFullModel;
import com.autojob.base.WebDriverCallback;
import com.autojob.model.entities.AccountModel;
import com.autojob.utils.Utils;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by OpenYourEyes on 03/06/2023
 */
class TiktokFeedbackRateTask2 extends BaseTiktokTask {
    String URL_RATE = "https://seller-vn.tiktok.com/product/rating";
    public static String messageNotGood = "Chào bạn không biết là sản phẩm bên mình có vấn đề gì? Mong bạn phản hồi lại giúp shop trong phần tin nhắn để shop giải quyết cho mình nhé.";
    private TiktokOrderDetailTask orderDetailTask;


    public TiktokFeedbackRateTask2(AccountModel accountModel, WebDriverCallback webDriverCallback) {
        super(accountModel, webDriverCallback);
    }


    @Override
    public String jobName() {
        return "PHẢN HỒI";
    }


    @Override
    public void run() {
        try {
            load(URL_RATE);
            //Đang chờ trả lời
            WebElement waitingReply = checkDoneBy(By.xpath("//span[contains(text(), 'Đang chờ trả lời')]"), "Button 'Đang chờ trả lời'");
            if (waitingReply == null) {
                print("Đang chờ trả lời: NULL");
                return;
            }
            waitingReply.click();
            List<WebElement> listRating = checkDoneListBy(By.xpath("//div[contains(@class, 'ratingListItem')]"), "Rate");
            List<WebElement> listOrderId = getElementsByXpath( "//div[contains(@class, 'productItemInfoOrderIdText')]");
            List<WebElement> stars = getElementsByXpath( "//div[contains(@class, 'ratingStar')]");
            hidePopupReplyLate();
            int size = listRating.size();
            int sizeOrder = listOrderId.size();
            int sizeStar = stars.size();
            if(size != sizeOrder || size != sizeStar){
                throw new InterruptedException("Size List không bằng nhau");
            }
            print("Có: " + size + " đánh giá chưa phản hồi");
            int count = 0;
            while (count < size) {
                try {
                    WebElement itemRating = listRating.get(count);
                    WebElement containerStart = stars.get(count);
                    WebElement orderId = listOrderId.get(count);
                    List<WebElement> starts = getElementsByTagName(containerStart, "svg");
                    int countStart = 0;
                    for (WebElement start : starts) {
                        String text = start.getAttribute("class");
                        if (text.contains("activeStar")) {
                            countStart++;
                        } else {
                            break;
                        }
                    }
                    print("=======>Phản hồi " + countStart +" sao -" +orderId.getText() + "<=======");
                    WebElement buttonFeedback = getElementBy(itemRating, By.xpath("//div[contains(text(), 'Phản hồi')]"));
                    if (buttonFeedback == null) {
                        throw new InterruptedException("Button Feedback IS NULL");
                    }
                    sendFeedback(buttonFeedback, countStart);
                    print("Gửi phản hồi thứ " + (count + 1));
                    delayBetween(3, 5);
                    if (countStart <= 2) {
                        //TODO send push here
                        sendChatBuyerRateNotGood(orderId);
                    }
                } catch (Exception e) {
                    playSoundError();
                    printException(e);
                }
                count++;
            }
        } catch (Exception e) {
            printException(e);
        }
    }

    /**
     * Xử lý đánh giá 1,2*
     */
    private void sendChatBuyerRateNotGood(WebElement elementOrder) {
        try {
            String text = elementOrder.getText();
            String orderId = "";
            if (text.contains(":")) {
                orderId = text.split(":")[1];
                print("orderId " + orderId);
            }
            if (orderId.isEmpty()) {
                printE("Không lấy được orderId");
                return;
            }
            print("orderId " + orderId);
            openNewTab();
            if (orderDetailTask == null) {
                orderDetailTask = new TiktokOrderDetailTask(accountModel, webDriverCallback);
                orderDetailTask.setWebDriver(webDriver);
            }
            orderDetailTask.feedbackRateNotGood(orderId);
        } catch (Exception e) {
            printE("Gửi chat cho khách hàng đánh giá 1,2* lỗi " + e.toString());
            e.printStackTrace();
        }


    }


    private void sendFeedback(WebElement buttonFeedback, int start) {
        try {
            buttonFeedback.click();
            WebElement dialog = checkDoneBy(By.className("arco-modal-content"), "DialogFeedback");
            WebElement textArea = getElementByTagName(dialog, "textarea");
            WebElement buttonSend = getElementByXpath("//span[contains(text(), 'Gửi')]");
            if (textArea == null || buttonSend == null) {
                screenShotFull("SendFeedback");
                playSoundError();
                return;
            }
            delaySecond(1);
            String content;
            if (start <= 2) {
                content = messageNotGood;
            } else {
                content = randomFeedbackGood();
            }
            int random = Utils.randomInteger(0, 1);
            if (random == 0) {
                textArea.sendKeys(content);
            } else {
                simulateSendKeys(textArea, content);
            }
            delayBetween(2, 3);
            buttonSend.click();
        } catch (Exception e) {
            printException(e);
            WebElement buttonCancel = getElementByXpath("//span[contains(text(), 'Hủy')]");
            if (buttonCancel != null) {
                buttonCancel.click();
            }

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
