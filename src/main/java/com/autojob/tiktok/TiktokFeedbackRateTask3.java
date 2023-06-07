//package com.autojob.tiktok;
//
//import com.autojob.ScreenshotFullModel;
//import com.autojob.base.WebDriverCallback;
//import com.autojob.model.entities.AccountModel;
//import com.autojob.task.BaseWebViewTask;
//import com.autojob.utils.Utils;
//import org.openqa.selenium.By;
//import org.openqa.selenium.WebElement;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Created by OpenYourEyes on 03/06/2023
// */
//class TiktokFeedbackRateTask extends BaseTiktokTask {
//    String URL_RATE = "https://seller-vn.tiktok.com/product/rating";
//    public static String messageNotGood = "Chào bạn không biết là sản phẩm bên mình có vấn đề gì? Mong bạn phản hồi lại giúp shop trong phần tin nhắn để shop giải quyết cho mình nhé.";
//    private TiktokOrderDetailTask orderDetailTask;
//    private int currentStar = 0;
//
//    public TiktokFeedbackRateTask(AccountModel accountModel, WebDriverCallback webDriverCallback) {
//        super(accountModel, webDriverCallback);
//    }
//
//
//    @Override
//    public String jobName() {
//        String text = "PHẢN HỒI";
//        if (currentStar > 0) {
//            return text + " " + currentStar + " SAO";
//        }
//        return text;
//    }
//
//
//    @Override
//    public void run() {
//        try {
//            delaySecond(3);
//            List<WebElement> listRate = openUrlByList(URL_RATE, By.className("arco-tag-checkable"), "Rate", false);
////            List<WebElement> listRate = checkDoneListBy(By.className("arco-tag-checkable"), "Rate", false);
//            int size = listRate.size();
//            print("Action Star: " + size);
//            if (size == 7) {
//                // 1-5: star
//                // 6: đang chờ trả lời, 7 đã trả lời
//                int starIndex = 0;
//                WebElement waitingFeedback = listRate.get(5);
//                waitingFeedback.click();
//                delayBetween(3, 5);
//                while (starIndex < 5) {
//                    if (starIndex > 0) {
//                        listRate.get(starIndex - 1).click();
//                        delayBetween(3, 5);
//                        print("Bỏ chọn " + (starIndex) + " sao");
//                    }
//                    WebElement elementStar = listRate.get(starIndex);
//                    elementStar.click();
//                    currentStar = (starIndex + 1);
//                    print("Chọn " + currentStar + " sao");
//                    delayBetween(3, 5);
//                    checkListRate(currentStar);
//                    starIndex++;
//                    print("starIndex " + starIndex);
//                }
//                print("HOÀN THÀNH PHẢN HỒI");
//            }
//        } catch (Exception e) {
//            printException(e);
//        }
//    }
//
//
//    private void checkListRate(int start) {
//        List<WebElement> feedbackElements = null;
//        List<WebElement> ordersDetail = new ArrayList<>();
//        String content = "";
//        int size = 0;
//        try {
//            feedbackElements = checkDoneListBy(By.xpath("//div[contains(text(), 'Phản hồi')]"), "Rate List");
//            size = feedbackElements.size();
//            if (start <= 2) {
//                content = messageNotGood;
//                ordersDetail = checkDoneListBy(By.xpath("//div[contains(@class, 'productItemInfoOrderIdText')]"), "CopyOrderId");
//                if (ordersDetail.size() != size) {
//                    ScreenshotFullModel.screenShotFull(webDriver, "FeedbackNotEqualOrder");
//                    throw new InterruptedException("List phản hồi không bằng list đơn hàng");
//                }
//            }
//            print(size + " đơn hàng chưa phản hồi");
//        } catch (Exception e) {
//            printException(e);
//            ScreenshotFullModel.screenShotFull(webDriver, "CheckListRate");
//        }
//        if (feedbackElements == null || feedbackElements.isEmpty()) {
//            print("FeedbackElements is Empty");
//            return;
//        }
//        int count = 0;
//        while (count < size) {
//            try {
//                if (start <= 2) {
//                    print("Gửi chat cho khách hàng đánh giá " + start + " sao");
//                    WebElement orderDetail = ordersDetail.get(count);
//                    sendChatBuyerRateNotGood(orderDetail);
//                }
//
//                WebElement feedbackAt = feedbackElements.get(count);
//                feedbackAt.click();
//                WebElement dialog = checkDoneBy(By.className("arco-modal-content"), "DialogFeedback");
//                WebElement textArea = getElementByTagName(dialog, "textarea");
//                WebElement buttonSend = getElementByXpath("//span[contains(text(), 'Gửi')]");
////                    WebElement buttonCancel = getElementByXpath("//span[contains(text(), 'Hủy')]");
//                delaySecond(1);
//                if (start >= 3) {
//                    content = randomFeedbackGood();
//                }
//                int random = Utils.randomInteger(0, 1);
//                if (random == 0) {
//                    textArea.sendKeys(content);
//                } else {
//                    simulateSendKeys(textArea, content);
//                }
//                print("Gửi chat thứ " + (count + 1));
//                delayBetween(2, 3);
//                buttonSend.click();
//                delayBetween(3, 5);
//            } catch (Exception e) {
//                printException(e);
//            }
//            count++;
//        }
//        if (count > 0) {
//            scrollToTop();
//            delaySecond(2);
//        }
//
//    }
//
//    private String randomFeedbackGood() {
//        int random = Utils.randomInteger(0, 2);
//        if (random == 0) {
//            return "Shop cảm ơn sự ủng hộ của bạn ạ. Mong là bạn sẽ tiếp tục theo dõi và ủng hộ shop nha ^^";
//        } else if (random == 1) {
//            return "Shop cảm ơn đánh giá của bạn. Hy vọng bạn sẽ giới thiệu bạn bè đến với shop nữa nha ^^";
//        } else {
//            return "Shop cảm ơn phản hồi của bạn. Chúc bạn ngày làm việc vui vẻ =))";
//        }
//    }
//
//
//    private void sendChatBuyerRateNotGood(WebElement elementOrder) {
//        String text = elementOrder.getText();
//        String orderId = "";
//        if (text.contains(":")) {
//            orderId = text.split(":")[1];
//        }
//        if (orderId.isEmpty()) {
//            printE("Không lấy đuợc orderId");
//            return;
//        }
//        elementOrder.click();
//        delayBetween(1, 3);
//        ArrayList<String> tabs = new ArrayList<>(webDriver.getWindowHandles());
//        if (tabs.size() == 1) {
//            printE("Mở tab chat lỗi");
//            return;
//        }
//        webDriver.switchTo().window(tabs.get(1));
//        if (orderDetailTask == null) {
//            orderDetailTask = new TiktokOrderDetailTask(accountModel, webDriverCallback);
//            orderDetailTask.setWebDriver(webDriver);
//        }
//        orderDetailTask.feedbackRateNotGood(orderId);
//    }
//
//
//}
