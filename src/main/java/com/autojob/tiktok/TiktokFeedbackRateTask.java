package com.autojob.tiktok;

import com.autojob.base.WebDriverCallback;
import com.autojob.model.entities.AccountModel;
import com.autojob.utils.Utils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Created by OpenYourEyes on 03/06/2023
 */
class TiktokFeedbackRateTask extends BaseTiktokTask {
    String URL_RATE = "https://seller-vn.tiktok.com/product/rating";
    public static String messageNotGood = "Chào bạn không biết là sản phẩm bên mình có vấn đề gì? Mong bạn phản hồi lại giúp shop trong phần tin nhắn để shop giải quyết cho mình nhé.";
    private TiktokOrderDetailTask orderDetailTask;


    public TiktokFeedbackRateTask(AccountModel accountModel, WebDriverCallback webDriverCallback) {
        super(accountModel, webDriverCallback);
    }


    @Override
    public String jobName() {
        return "PHẢN HỒI";
    }


    @Override
    public void run() {
        try {
            List<WebElement> listRate = openUrlByList(URL_RATE, By.className("arco-tag-checkable"), "STAR LIST", false);
            if (listRate.size() != 7) {
                throw new InterruptedException("Size != 7");
            }
            WebElement start3 = listRate.get(2);
            start3.click();
            delayMilliSecond(400);
            WebElement start4 = listRate.get(3);
            start4.click();
            delayMilliSecond(400);
            WebElement start5 = listRate.get(4);
            start5.click();
            delayMilliSecond(400);
            WebElement waitingReply = listRate.get(5);
            waitingReply.click();
            delayMilliSecond(400);
            List<WebElement> listRating = checkDoneListBy(By.xpath("//div[contains(@class, 'ratingListItem')]"), "Rate");
            List<WebElement> listOrderId = getElementsByXpath("//div[contains(@class, 'productItemInfoOrderIdText')]");
            List<WebElement> stars = getElementsByXpath("//div[contains(@class, 'ratingStar')]");
            hidePopupReplyLate();
            int size = listRating.size();
            int sizeOrder = listOrderId.size();
            int sizeStar = stars.size();
            if (size != sizeOrder || size != sizeStar) {
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
                    String message = String.format("%s. Phản hồi %s sao - %s", (count + 1), countStart, orderId.getText());
                    print(message);
                    WebElement buttonFeedback = getElementBy(itemRating, By.xpath("//div[contains(text(), 'Phản hồi')]"));
                    if (buttonFeedback == null) {
                        throw new InterruptedException("Button Feedback IS NULL");
                    }
                    sendFeedback(buttonFeedback, countStart);
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
            List<WebElement> textAreas = getElementsByTagName(dialog, "textarea");
            WebElement buttonSend = getElementByXpath("//span[contains(text(), 'Gửi')]");
            if (buttonSend == null) {
                screenShotFull("SendFeedback");
                playSoundError();
                return;
            }
            delaySecond(1);
            String content;
            int size = textAreas.size();
            print("Nhập nội dung phản hồi");
            if (size == 1) {
                if (start <= 2) {
                    content = messageNotGood;
                } else {
                    content = randomFeedbackGood();
                }
                int random = Utils.randomInteger(0, 1);
                WebElement textArea = textAreas.get(0);
                if (random == 0) {
                    textArea.sendKeys(content);
                } else {
                    simulateSendKeys(textArea, content);
                }
            } else {
                print("Đơn hàng có " + size + " đánh giá");
                int count = 0;
                while (count < size) {
                    content = feedbackContent(count);
                    WebElement textArea = textAreas.get(count);
                    int random = Utils.randomInteger(0, 1);
                    if (random == 0) {
                        textArea.sendKeys(content);
                    } else {
                        simulateSendKeys(textArea, content);
                    }
                    count++;
                }
            }
            delayBetween(2, 3);
            buttonSend.click();
            print("Gửi phản hồi thành công");
            delayBetween(2, 4);
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
        return feedbackContent(random);
    }

    private String feedbackContent(int random) {
        if (random == 0) {
            return "Shop cảm ơn sự ủng hộ của bạn ạ. Mong là bạn sẽ tiếp tục theo dõi và ủng hộ shop nha ^^";
        } else if (random == 1) {
            return "Shop cảm ơn đánh giá của bạn. Hy vọng bạn sẽ giới thiệu bạn bè đến với shop nữa nha ^^";
        } else if (random == 2) {
            return "Shop cảm ơn phản hồi của bạn. Chúc bạn ngày làm việc vui vẻ =))";
        } else {
            return random + ".Shop cảm ơn bạn nha ^^";
        }
    }


}
