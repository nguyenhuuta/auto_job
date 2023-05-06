package com.autojob.shopee;

import com.autojob.api.ApiManager;
import com.autojob.api.RequestQueue;
import com.autojob.model.entities.AccountBody;
import com.autojob.model.entities.AccountModel;
import com.autojob.model.entities.BaseResponse;
import com.autojob.task.BaseWebViewTask;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import retrofit2.Call;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Created by OpenYourEyes on 04/05/2023
 */
public class ShopeeTask extends BaseWebViewTask {
    static final String SHOPEE_ENDPOINT = "https://banhang.shopee.vn/";
    static final String SHOPEE_URL = SHOPEE_ENDPOINT + "webchat/conversations";
    static final String shopeeNotificaions = SHOPEE_ENDPOINT + "portal/notification/order-updates/";
    static final String SHOPEE_ORDER_COMPLETE = SHOPEE_ENDPOINT + "portal/sale/order?type=completed";
    public static final int JOB_SEND_THANK_YOU = 1;
    public static final int JOB_SEND_MESSAGE = 2;
    int currentJob = JOB_SEND_THANK_YOU;

    public ShopeeTask(AccountModel accountModel) {
        super(accountModel);
    }

    @Override
    public String jobName() {
        switch (currentJob) {
            case JOB_SEND_THANK_YOU:
                return "JOB_THANKS_YOU";
            case JOB_SEND_MESSAGE:
                return "JOB_SEND_MESSAGE";
        }
        return "Không có JOB";
    }


    @Override
    public synchronized void run() {
        if (status == RUNNING) {
            print("JOB đang chạy, đợi chạy xong hoặc FORCE STOP");
            return;
        }
        status = RUNNING;
        switch (currentJob) {
            case JOB_SEND_THANK_YOU:
                sendThankYouFromNotification();
                break;
            case JOB_SEND_MESSAGE:
                break;
        }
        status = IDLE;
    }

    private boolean nextPage(boolean isNext) {
        try {
            WebElement shopeePage = getElementByClassName("shopee-pager");
            List<WebElement> buttons = getElementsByTagName(shopeePage, "button");
            if (buttons.size() == 2) {
                if (isNext) {
                    WebElement buttonNext = buttons.get(1);
                    if (!buttonNext.isEnabled()) {
                        print("HẾT TRANG");
                        return false;
                    }
                    buttonNext.click();
                } else {
                    WebElement buttonPrev = buttons.get(0);
                    if (!buttonPrev.isEnabled()) {
                        print("HẾT TRANG");
                        return false;
                    }
                    buttonPrev.click();
                }
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
        AccountBody body = new AccountBody(orderId, accountModel.rowId);
        try {
            Call<BaseResponse<String>> call = ApiManager.GOOGLE_ENDPOINT.updateAccountShopee(ApiManager.URL_SHOPEE, body);
            String message = RequestQueue.getInstance().executeRequest(call);
            updateListView("Lưu " + orderId + " lên GoogleDriver " + message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private void hideMiniChat() {
        WebElement header = getElementByXpath("//*[@id=\"shopee-mini-chat-embedded\"]/div[1]/div[1]");
        List<WebElement> iTag = getElementsByTagName(header, "i");
        if (iTag != null && iTag.size() == 4) {
            iTag.get(2).click();
            delaySecond(3);
        }
    }

    void sendThankYouFromNotification() {
        String newOrderId = "";
        printGreen("START");
        try {
            load(shopeeNotificaions);
            boolean isNextPage;
            do {
                WebElement orderListBody = notificationLoadDone();
                getCurrentPage();
                delaySecond(5);
                List<WebElement> orderList = getElementsByClassName(orderListBody, "notification-item");
                if (orderList == null) {
                    printE("SendThankYouFromNotification null");
                    return;
                }
                int size = orderList.size();
                int count = 0;
                while (count < size) {
                    if (status == FORCE_STOP) {
                        print("DỪNG JOB HIỆN TẠI");
                        status = IDLE;
                        return;
                    }
                    WebElement item = orderList.get(count);
                    String title = getElementByClassName(item, "title").getText();
                    if (!title.equals("Giao kiện hàng thành công")) {
                        count++;
                        continue;
                    }
                    List<WebElement> orderIds = getElementsByTagName(item, "b");
                    if (orderIds == null || orderIds.size() != 2) {
                        printE("Lấy order Id lỗi");
                        count++;
                        continue;
                    }
                    String orderId = orderIds.get(1).getText();
                    String log = "OrderId: " + orderId + "/" + accountModel.lastOrderId;
                    print(log);
                    if (Objects.equals(orderId, accountModel.lastOrderId)) {
                        printGreen("Dừng JOB, chạy đến order cuối cùng");
                        status = IDLE;
                        return;
                    }
                    WebElement element = getElementByClassName(item, "container-item");
                    boolean clickable;
                    int height = item.getSize().getHeight();
                    do {
                        try {
                            element.click();
                            clickable = true;
                        } catch (Exception ex) {
                            clickable = false;
                            scrollBy(height);
                            delayMilliSecond(500);
                        }
                    } while (!clickable);
                    ArrayList<String> tabs = new ArrayList<>(webDriver.getWindowHandles());
                    webDriver.switchTo().window(tabs.get(1));
                    WebElement miniChat = checkDoneByClass("mini-chat");
                    // Đánh giá người mua
                    WebElement btns = getElementByClassName("btns");
                    if (btns != null) {
                        btns.click();
                        delaySecond(2);
                        List<WebElement> starts = getElementsByClassName("shopee-rate-star__back");
                        if (starts != null && starts.size() == 5) {
                            starts.get(4).click();
                            delaySecond(2);
                        } else {
                            printE("Starts IS NULL OR size != 5");
                        }
                        WebElement buttonRate = getElementByCssSelector("div.shopee-modal__footer > div > button.shopee-button.shopee-button--primary.shopee-button--normal");
                        if (buttonRate != null) {
                            buttonRate.click();
                            delaySecond(2);
                        }
                    }
                    /// Xin đánh giá 5*
                    if (miniChat != null) {
                        tryClick(miniChat, 100);
                        delaySecond(4);
                        shopeeMiniChatEmbedded();
                    }
                    if (newOrderId.isEmpty()) {
                        newOrderId = orderId;
                        accountModel.lastOrderId = orderId;
                        updateAccountShopee(newOrderId);
                    }
                    delay5to10s();
                    webDriver.close();
                    webDriver.switchTo().window(tabs.get(0));
                    delaySecond(4);
                    count++;
                }
                isNextPage = nextPage(true);
            } while (isNextPage);
        } catch (Exception e) {
            printE("SendThankYouFromNotification " + e);
        }
    }

    private boolean shopeeMiniChatEmbedded() {
        try {
            WebElement messageSection = checkDoneById("messageSection"); // đợi hiển thị lịch sử chat
            WebElement miniChatEmbedded = getElementById("shopee-mini-chat-embedded");
            WebElement element = getElementByClassName("username");
            String hello = "Xin chào ";
            if (element != null) {
                hello += element.getText() + ",";
            }
            String shopName = getShopName();
            String[] array = new String[]{
                    hello,
                    "",
                    "Cảm ơn bạn đã mua hàng tại cửa hàng của chúng tôi! Sự ủng hộ của bạn là động lực lớn để chúng tôi tiếp tục nỗ lực cung cấp những sản phẩm và dịch vụ tốt nhất cho khách hàng.",
                    "",
                    "Nếu bạn hài lòng với sản phẩm và dịch vụ của chúng tôi, xin vui lòng đánh giá 5* hoặc có bất kỳ câu hỏi hay ý kiến đóng góp nào, xin vui lòng liên hệ với chúng tôi. Chúng tôi luôn sẵn sàng hỗ trợ bạn.",
                    "",
                    "Xin lần nữa cảm ơn bạn rất nhiều!",
                    "Trân trọng,",
                    shopName.toUpperCase(),
            };
            WebElement textArea = getElementByTagName(miniChatEmbedded, "textarea");
            for (String value : array) {
                textArea.sendKeys(value);
                textArea.sendKeys(Keys.SHIFT, Keys.ENTER);
                delayMilliSecond(400);
            }
            textArea.sendKeys(Keys.ENTER);
            return true;
        } catch (Exception ex) {
            printE("Exception " + ex);
        }
        return false;
    }

    private boolean sendChatToMiniChatEmbedded(String message) {
        try {
            WebElement miniChatEmbedded = getElementById("shopee-mini-chat-embedded");
            WebElement textArea = getElementByTagName(miniChatEmbedded, "textarea");
            textArea.sendKeys(message);
            textArea.sendKeys(Keys.ENTER);
            return true;
        } catch (Exception e) {
            printE("sendChatToMiniChatEmbedded Exception" + e);
        }
        return false;
    }

    /**
     * Gửi tin nhắn đến toàn bộ khách mua hàng thành công
     */
    private void sendChatAllUser(String message) {

        try {
            load(SHOPEE_ORDER_COMPLETE);
            orderLoadDone();
            //TODO cần check hiển thị dialog khuyến mãi...
            openLastPage();
            orderLoadDone();
            boolean isNextPage;
            do {
                WebElement orderListBody = orderLoadDone();
                delaySecond(2);
                List<WebElement> orderList = getElementsByClassName(orderListBody, "order-item");
                if (orderList == null) {
                    printE("orderList null");
                    return;
                }
                int size = orderList.size();
                int count = 0;
                while (count < size) {
                    if (status == FORCE_STOP) {
                        print("DỪNG JOB HIỆN TẠI");
                        status = IDLE;
                        return;
                    }
                    WebElement item = orderList.get(count);
                    String linkOrderDetail = item.getAttribute("href");
                    WebElement orderElement = getElementByClassName(item, "orderid");
                    String orderId = orderElement.getText();
                    String userName = getElementByClassName(item, "username").getText();
                    WebElement miniChat = getElementByClassName(item, "mini-chat");
                    if (miniChat != null) { // Truờng hợp user bị block sẽ bị ẩn minichat
                        boolean clickable;
                        do {
                            try {
                                miniChat.click();
                                clickable = true;
                            } catch (Exception ex) {
                                clickable = false;
                                scrollBy(item.getSize().getHeight());
                                delayMilliSecond(500);
                            }
                        } while (!clickable);
                        print("miniChat.isDisplayed() " + userName + " / " + miniChat.isDisplayed());
                        delaySecond(10);
                        sendChatToMiniChatEmbedded(message);
                        print("Gửi tin nhắn đến " + userName.toUpperCase() + "\n" + linkOrderDetail);
                    }
                    count++;
                }
                hideMiniChat();
                isNextPage = nextPage(false);
            } while (isNextPage);
        } catch (Exception e) {
            printE("SendChatAllUser Exception " + e);
        }


    }

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

    private WebElement orderLoadDone() {
        WebElement element;
        do {
            delaySecond(2);
            element = getElementByClassName("order-list-body");
            print("LOAD DONE: " + (element != null));
        } while (element == null);
        return element;
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

    private WebElement checkDoneByClass(String key) {
        WebElement element;
        do {
            delaySecond(2);
            element = getElementByClassName(key);
            print("CheckDone: " + (element != null));
        } while (element == null);
        return element;
    }

    private WebElement checkDoneById(String key) {
        WebElement element;
        do {
            delaySecond(2);
            element = getElementById(key);
            print("CheckDone: " + (element != null));
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
