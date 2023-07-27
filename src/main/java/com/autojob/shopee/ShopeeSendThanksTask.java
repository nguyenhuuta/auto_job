package com.autojob.shopee;

import com.autojob.api.ApiManager;
import com.autojob.api.RequestQueue;
import com.autojob.base.WebDriverCallback;
import com.autojob.model.entities.AccountBody;
import com.autojob.model.entities.AccountModel;
import com.autojob.model.entities.BaseResponse;
import com.autojob.task.BaseWebViewTask;
import javafx.scene.paint.Color;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.Colors;
import retrofit2.Call;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Created by OpenYourEyes on 04/05/2023
 */
public class ShopeeSendThanksTask extends BaseShopeeTask {
    //    static final String SHOPEE_URL = SHOPEE_ENDPOINT + "webchat/conversations";
//    static final String shopeeNotificaions = SHOPEE_ENDPOINT + "portal/notification/order-updates/";
    static final String SHOPEE_ORDER_COMPLETE = ShopeeController.SHOPEE_ENDPOINT + "portal/sale/order?type=completed";

    public ShopeeSendThanksTask(AccountModel accountModel, WebDriverCallback callback) {
        super(accountModel, callback);
    }

    @Override
    public String jobName() {
        return "Gửi cảm ơn";
    }


    @Override
    public synchronized void run() {
        sendChatOrderComplete();
    }


    /**
     * Gửi tin nhắn đến toàn bộ khách mua hàng thành công
     */
    private void sendChatOrderComplete() {
        try {
            //TODO cần check hiển thị dialog khuyến mãi...
            boolean isNextPage;
            do {
                List<WebElement> orderList = openUrlByList(SHOPEE_ORDER_COMPLETE, By.className("order-item"), "List Order");
                if (orderList.isEmpty()) {
                    printE("orderList Empty");
                    return;
                }
                int size = orderList.size();
                int count = 0;
                while (count < size) {
                    WebElement item = orderList.get(count);
                    scrollToElement(item);
                    WebElement orderElement = getElementByClassName(item, "orderid");
                    String orderId = orderElement.getText();
//                    if (accountModel.lastOrderId.equals(orderId)) {
//                        print("ĐẾN LAST ORDER");
//                        return;
//                    }
                    String userName = getElementByClassName(item, "username").getText();
                    String format = String.format("============ %s| %s ============", (count + 1), userName + "|" + orderId);
                    print(format);
//                    sendRate(item);
                    WebElement miniChat = getElementByClassName(item, "mini-chat");
                    print("Gửi cảm ơn đến " + userName);
                    sendChat(miniChat);
                    if (count == 0) {
                        updateAccountShopee(orderId);
                    }
                    delay5to10s();
                    count++;
                }
                hideMiniChat();
                isNextPage = nextPage();
            } while (isNextPage);
        } catch (Exception e) {
            printE("SendChatAllUser Exception " + e);
        }
    }

    private void sendRate(WebElement item) {
        try {
            WebElement rate = item.findElement(By.xpath("//div[contains(@class, 'item-action')]/button"));
            rate.click();
            delaySecond(2);

            WebElement dialog = getElementByClassName("shopee-modal__content--large");
            if (dialog == null) {
                printE("Dialog rate null");
                return;
            }

            List<WebElement> starts = getElementsByClassName(dialog, "shopee-rate-star__back");
            if (starts != null && starts.size() == 5) {
                starts.get(4).click();
                delaySecond(2);
            } else {
                printE("Starts IS NULL OR size != 5");
            }
            List<WebElement> buttonSend = getElementsByTagName(dialog, "button");
            if (buttonSend != null && buttonSend.size() == 2) {
                buttonSend.get(1).click();
                delaySecond(2);
            } else {
                printE("buttonSend IS NULL OR size != 2");
            }
        } catch (Exception ignore) {

        }
    }


    private void sendChat(WebElement miniChat) throws InterruptedException {
        if (miniChat != null && miniChat.isDisplayed()) { // Truờng hợp user bị block sẽ bị ẩn minichat
            miniChat.click();
            delaySecond(10);
            WebElement miniChatEmbedded = getElementById("shopee-mini-chat-embedded");
            WebElement textArea = getElementByTagName(miniChatEmbedded, "textarea");
            if (textArea == null) {
                miniChat.click();
                delaySecond(2);
                textArea = getElementByTagName(miniChatEmbedded, "textarea");
                if (textArea == null) {
                    throw new InterruptedException("Input Chat không hiển thị");
                }
            }
            String[] array = message();
            for (String value : array) {
                textArea.sendKeys(value);
                textArea.sendKeys(Keys.SHIFT, Keys.ENTER);
            }
            textArea.sendKeys(Keys.ENTER);
            print("Gửi cảm ơn thành công");
        } else {
            // user bị bloc, bỏ qua
            delaySecond(5);
        }
    }

    private String[] message() {
        return new String[]{
                "Shop thấy bạn đã nhận hàng",
                "Không biết sản phẩm bên mình bạn có hài lòng không ạ?",
                "Hàng bên mình được đổi trả trong 3 ngày.",
                "Nếu bạn hài lòng hãy ĐÁNH GIÁ cho shop 5* nhé ^^.",
                "ĐỪNG VỘI ĐÁNH GIÁ XẤU nếu sản phẩm có vấn đề, hãy nhắn tin hoặc liên hệ: 0342.092.686 để shop xử lý ngay ạ."
        };
    }


    private boolean nextPage() {
        try {
            WebElement shopeePage = getElementByClassName("shopee-pager");
            List<WebElement> buttons = getElementsByTagName(shopeePage, "button");
            if (buttons.size() == 2) {
                WebElement buttonNext = buttons.get(1);
                if (!buttonNext.isEnabled()) {
                    print("HẾT TRANG");
                    return false;
                }
                buttonNext.click();
                return true;
            } else {
                printE("NextPage button != 2");
            }
        } catch (Exception exception) {
            printE("nextPage " + exception);
        }
        return false;
    }

    private int getCurrentPage() {
        WebElement shopeePage = getElementByClassName("shopee-pager");
        List<WebElement> listPage = getElementsByTagName(shopeePage, "li");
        for (WebElement page : listPage) {
            String attribute = page.getAttribute("class");
            if (attribute.contains("active")) {
                String pageNumber = page.getText();
                print("CURRENT PAGE: " + pageNumber);
                try {
                    return Integer.parseInt(pageNumber);
                } catch (Exception e) {
                    printE("GetCurrentPage " + e);
                }
            }
        }
        return 0;
    }


    void updateAccountShopee(String orderId) {
//        AccountBody body = new AccountBody(orderId, accountModel.rowId);
//        try {
//            Call<BaseResponse<String>> call = ApiManager.GOOGLE_ENDPOINT.updateAccountShopee(ApiManager.URL_GOOGLE_SHEET, body);
//            String message = RequestQueue.getInstance().executeRequest(call);
//            printColor("Lưu " + orderId + " lên GoogleDriver " + message, Color.GREEN);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }


    private void hideMiniChat() {
        WebElement header = getElementByXpath("//*[@id=\"shopee-mini-chat-embedded\"]/div[1]/div[1]");
        List<WebElement> iTag = getElementsByTagName(header, "i");
        if (iTag != null && iTag.size() == 4) {
            iTag.get(2).click();
            delaySecond(3);
        }
    }

    //    void sendThankYouFromNotification() {
//        String newOrderId = "";
//        printGreen("START");
//        try {
//            load(shopeeNotificaions);
//            boolean isNextPage;
//            do {
//                WebElement orderListBody = notificationLoadDone();
//                getCurrentPage();
//                delaySecond(5);
//                List<WebElement> orderList = getElementsByClassName(orderListBody, "notification-item");
//                if (orderList == null) {
//                    printE("SendThankYouFromNotification null");
//                    return;
//                }
//                int size = orderList.size();
//                int count = 0;
//                while (count < size) {
//                    WebElement item = orderList.get(count);
//                    String title = getElementByClassName(item, "title").getText();
//                    if (!title.equals("Giao kiện hàng thành công")) {
//                        count++;
//                        continue;
//                    }
//                    List<WebElement> orderIds = getElementsByTagName(item, "b");
//                    if (orderIds == null || orderIds.size() != 2) {
//                        printE("Lấy order Id lỗi");
//                        count++;
//                        continue;
//                    }
//                    String orderId = orderIds.get(1).getText();
//                    String log = "OrderId: " + orderId + "/" + accountModel.lastOrderId;
//                    print(log);
//                    if (Objects.equals(orderId, accountModel.lastOrderId)) {
//                        printGreen("Dừng JOB, chạy đến order cuối cùng");
//                        return;
//                    }
//                    WebElement element = getElementByClassName(item, "container-item");
//                    boolean clickable;
//                    int height = item.getSize().getHeight();
//                    do {
//                        try {
//                            element.click();
//                            clickable = true;
//                        } catch (Exception ex) {
//                            clickable = false;
//                            scrollBy(height);
//                            delayMilliSecond(500);
//                        }
//                    } while (!clickable);
//                    ArrayList<String> tabs = new ArrayList<>(webDriver.getWindowHandles());
//                    webDriver.switchTo().window(tabs.get(1));
//                    WebElement miniChat = checkDoneBy(By.className("mini-chat"), "Mini Chat");
//                    // Đánh giá người mua
//                    WebElement btns = getElementByClassName("btns");
//                    if (btns != null) {
//                        btns.click();
//                        delaySecond(2);
//                        List<WebElement> starts = getElementsByClassName("shopee-rate-star__back");
//                        if (starts != null && starts.size() == 5) {
//                            starts.get(4).click();
//                            delaySecond(2);
//                        } else {
//                            printE("Starts IS NULL OR size != 5");
//                        }
//                        WebElement buttonRate = getElementByCssSelector("div.shopee-modal__footer > div > button.shopee-button.shopee-button--primary.shopee-button--normal");
//                        if (buttonRate != null) {
//                            buttonRate.click();
//                            delaySecond(2);
//                        }
//                    }
//                    /// Xin đánh giá 5*
//                    if (miniChat != null) {
//                        tryClick(miniChat, 100);
//                        delaySecond(4);
//                        shopeeMiniChatEmbedded();
//                    }
//                    if (newOrderId.isEmpty()) {
//                        newOrderId = orderId;
//                        accountModel.lastOrderId = orderId;
//                        updateAccountShopee(newOrderId);
//                    }
//                    delay5to10s();
//                    webDriver.close();
//                    webDriver.switchTo().window(tabs.get(0));
//                    delaySecond(4);
//                    count++;
//                }
//                isNextPage = nextPage(true);
//            } while (isNextPage);
//        } catch (Exception e) {
//            printE("SendThankYouFromNotification " + e);
//        }
//    }
//
//    private boolean shopeeMiniChatEmbedded() {
//        try {
//            WebElement messageSection = checkDoneBy(By.id("messageSection"), "Lịch sử chat"); // đợi hiển thị lịch sử chat
//            WebElement miniChatEmbedded = getElementById("shopee-mini-chat-embedded");
//            WebElement element = getElementByClassName("username");
//            String hello = "Xin chào ";
//            if (element != null) {
//                hello += element.getText() + ",";
//            }
//            String shopName = getShopName();
//            String[] array = new String[]{
//                    hello,
//                    "",
//                    "Cảm ơn bạn đã mua hàng tại cửa hàng của chúng tôi! Sự ủng hộ của bạn là động lực lớn để chúng tôi tiếp tục nỗ lực cung cấp những sản phẩm và dịch vụ tốt nhất cho khách hàng.",
//                    "",
//                    "Nếu bạn hài lòng với sản phẩm và dịch vụ của chúng tôi, xin vui lòng đánh giá 5* hoặc có bất kỳ câu hỏi hay ý kiến đóng góp nào, xin vui lòng liên hệ với chúng tôi. Chúng tôi luôn sẵn sàng hỗ trợ bạn.",
//                    "",
//                    "Xin lần nữa cảm ơn bạn rất nhiều!",
//                    "Trân trọng,",
//                    shopName.toUpperCase(),
//            };
//            WebElement textArea = getElementByTagName(miniChatEmbedded, "textarea");
//            for (String value : array) {
//                textArea.sendKeys(value);
//                textArea.sendKeys(Keys.SHIFT, Keys.ENTER);
//                delayMilliSecond(400);
//            }
//            textArea.sendKeys(Keys.ENTER);
//            return true;
//        } catch (Exception ex) {
//            printE("Exception " + ex);
//        }
//        return false;
//    }
//
//    private boolean sendChatToMiniChatEmbedded(String message) {
//        try {
//            WebElement miniChatEmbedded = getElementById("shopee-mini-chat-embedded");
//            WebElement textArea = getElementByTagName(miniChatEmbedded, "textarea");
//            textArea.sendKeys(message);
//            textArea.sendKeys(Keys.ENTER);
//            return true;
//        } catch (Exception e) {
//            printE("sendChatToMiniChatEmbedded Exception" + e);
//        }
//        return false;
//    }
//
//
    //    private void openPageAt(int page) {
//        WebElement shopeePage = getElementByClassName("shopee-pager");
//        List<WebElement> listPage = getElementsByTagName(shopeePage, "li");
//        if (listPage == null) {
//            printE("openLastPage listPage null");
//            return;
//        }
//        for (WebElement webElement : listPage) {
//            try {
//                int _page = Integer.parseInt(webElement.getText());
//                if (_page == page) {
//                    webElement.click();
//                    orderLoadDone();
//                    return;
//                }
//            } catch (Exception ignore) {
//
//            }
//        }
//        boolean isNextPage = nextPage(false);
//        if (isNextPage) {
//            openPageAt(page);
//        }
//    }
    private void openLastPage() {
        WebElement shopeePage = getElementByClassName("shopee-pager");
        List<WebElement> listPage = getElementsByTagName(shopeePage, "li");
        if (listPage == null) {
            printE("openLastPage listPage null");
            return;
        }
        int size = listPage.size() - 1;
        listPage.get(size).click();
        print("Click page cuối cùng");
    }


    private WebElement notificationLoadDone() {
        WebElement element;
        do {
            delaySecond(2);
            element = getElementByClassName("notification-list");
            print("LOAD DONE: " + (element != null));
        } while (element == null);
        return element;
    }

    private String getShopName() {
        try {
            return getElementByClassName("account-name").getText();
        } catch (Exception ignore) {
            return "";
        }
    }
}
