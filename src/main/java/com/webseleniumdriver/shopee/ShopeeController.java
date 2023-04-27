package com.webseleniumdriver.shopee;

import com.webseleniumdriver.api.ApiManager;
import com.webseleniumdriver.api.RequestQueue;
import com.webseleniumdriver.model.entities.AccountBody;
import com.webseleniumdriver.model.entities.AccountModel;
import com.webseleniumdriver.model.entities.BaseResponse;
import com.webseleniumdriver.task.BaseWebViewTask;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import retrofit2.Call;
import rx.Observer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by OpenYourEyes on 17/04/2023
 */
public class ShopeeController extends BaseWebViewTask {
    static final String SHOPEE_ENDPOINT = "https://banhang.shopee.vn/";
    static final String SHOPEE_URL = SHOPEE_ENDPOINT + "webchat/conversations";
    static final String shopeeNotificaions = SHOPEE_ENDPOINT + "portal/notification/order-updates/";
    static final String SHOPEE_ORDER_COMPLETE = SHOPEE_ENDPOINT + "portal/sale/order?type=completed";
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();


    public static final int JOB_SEND_THANK_YOU = 1;
    public static final int JOB_AUTO_REPLY_MESSAGE = 2;

    int currentJob = 0;

    @Override
    public String getTag() {
        return "ShopeeController";
    }

    public ShopeeController(AccountModel model) {
        super(model);
        init();
        sendThankYouFromNotification();
    }

    void init() {
        triggerTaskId.subscribe(new Observer<Integer>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onNext(Integer integer) {
                print("triggerTaskId " + integer);
                if (status != IDLE) {
                    updateListView("JOB đang chạy, vui lòng đợi...");
                    return;
                }
                currentJob = integer;
                executorService.execute(() -> {
                    switch (integer) {
                        case JOB_SEND_THANK_YOU:
                            sendThankYouFromNotification();
                            break;
                        case JOB_AUTO_REPLY_MESSAGE:
                            break;
                    }
                });
            }
        });
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
                orderLoadDone();
                getCurrentPage();
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

    @Override
    public String jobName() {
        switch (currentJob) {
            case JOB_SEND_THANK_YOU:
                return "JOB_SEND_THANK_YOU";
            case JOB_AUTO_REPLY_MESSAGE:
                return "JOB_AUTO_REPLY_MESSAGE";
        }
        return "Không có JOB";
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

    /**
     * Chạy vào lúc 8h tối mỗi ngày với những đơn giao thành công
     */
    void sendThankYou() {
        try {
            String lastOrderId = accountModel.lastOrderId;
            load(SHOPEE_ORDER_COMPLETE);
            delaySecond(5);
            List<WebElement> orderList = getElementsByClassName("order-item");
            int size = orderList.size();
            String shopName = getElementByClassName("account-name").getText();
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
                if (orderId.equals(lastOrderId)) {
                    print("Đến điểm dừng");
                    break;
                }
                String userName = getElementByClassName(item, "username").getText();
                WebElement miniChat = getElementByClassName(item, "mini-chat");
                new Actions(webDriver).moveToElement(item).moveToElement(miniChat).perform();
                delaySecond(2);
                miniChat.click();
                delaySecond(2);
                WebElement miniChatEmbedded = getElementById("shopee-mini-chat-embedded");

                String hello = String.format("Xin chào %s,", userName.toUpperCase());
                String[] array = new String[]{
                        hello,
                        "",
                        "Chúng tôi xin cảm ơn bạn đã mua hàng tại cửa hàng của chúng tôi! Sự ủng hộ của bạn là động lực lớn để chúng tôi tiếp tục nỗ lực cung cấp những sản phẩm và dịch vụ tốt nhất cho khách hàng.",
                        "",
                        "Nếu bạn hài lòng với sản phẩm và dịch vụ của chúng tôi, xin vui lòng đánh giá 5* hoặc có bất kỳ câu hỏi hay ý kiến đóng góp nào, xin vui lòng liên hệ với chúng tôi. Chúng tôi luôn sẵn sàng hỗ trợ bạn.",
                        "",
                        "Xin lần nữa cảm ơn bạn rất nhiều!",
                        "Trân trọng,",
                        shopName.toUpperCase(),
                };
                boolean isSendChat = shopeeMiniChatEmbedded(miniChatEmbedded, array);
                if (isSendChat) {
                    String message = "Gửi tin nhắn đến " + userName.toUpperCase() + "\n" + linkOrderDetail;
                    print(message);
                    updateAccountShopee(orderId);
                    delaySecond(10);
                }
                count++;
            }
        } catch (Exception e) {
            e.printStackTrace();
            printE("sendThankYou " + e);
        }
    }

    void sendThankYouFromNotification() {
        try {
            load(shopeeNotificaions);
            boolean isNextPage;
            do {
                WebElement orderListBody = notificationLoadDone();
                delaySecond(5);
                List<WebElement> orderList = getElementsByClassName(orderListBody, "notification-item");
                if (orderList == null) {
                    printE("SendThankYouFromNotification null");
                    return;
                }
                int size = orderList.size();
                print(size + "");
                int count = 0;
                String shopName = getElementByClassName("account-name").getText();
                while (count < size) {
                    if (status == FORCE_STOP) {
                        print("DỪNG JOB HIỆN TẠI");
                        status = IDLE;
                        return;
                    }
                    WebElement item = orderList.get(count);
                    print(item.getText());
                    int height = item.getSize().getHeight();
                    String title = getElementByClassName(item, "title").getText();
                    WebElement element = getElementByClassName(item, "container-item");
                    boolean clickable;
                    do {
                        try {
                            element.click();
                            clickable = true;
                        } catch (Exception ex) {
                            clickable = false;
                            scrollBy(0, height);
                            delayMilliSecond(500);
                        }
                    } while (!clickable);
                    ArrayList<String> tabs = new ArrayList<>(webDriver.getWindowHandles());
                    webDriver.switchTo().window(tabs.get(1));
                    delaySecond(4);
                    webDriver.close();
                    webDriver.switchTo().window(tabs.get(0));
                    delaySecond(2);
                    count++;
                }
                isNextPage = nextPage(false);
            } while (isNextPage);

        } catch (Exception e) {
            e.printStackTrace();
            printE("sendThankYou " + e);
        }
    }

    private boolean shopeeMiniChatEmbedded(WebElement miniChatEmbedded, String[] array) {
        try {
            List<WebElement> chatList = getElementsByCssSelector(miniChatEmbedded, "#\\#message-virtualized-list > div > div");
            if (chatList == null) {
                throw new NullPointerException("chatList");
            }
            int size = chatList.size();
            if (size == 0) {
                print("Lịch sử chat rỗng");
                return false;
            }
            WebElement lastItem = chatList.get(size - 1);
            String text = lastItem.getText();
            print(text);
            if (text.contains("Bạn đang trao đổi với Người mua về đơn hàng này")) {
                WebElement textArea = getElementByTagName(miniChatEmbedded, "textarea");
                if (array.length == 1) {
                    textArea.sendKeys(array[0]);
                } else {
                    for (String value : array) {
                        textArea.sendKeys(value);
                        textArea.sendKeys(Keys.SHIFT, Keys.ENTER);
                        delayMilliSecond(400);
                    }
                }
                textArea.sendKeys(Keys.ENTER);
                return true;
            }
        } catch (Exception ex) {
            printE("Exception " + ex);
        }
        return false;
    }

    private boolean sendChatToMiniChatEmbedded(WebElement miniChatEmbedded, String message) {
        try {
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
                                scrollBy(0, item.getSize().getHeight());
                                delayMilliSecond(500);
                            }
                        } while (!clickable);
                        print("miniChat.isDisplayed() " + userName + " / " + miniChat.isDisplayed());
                        delaySecond(10);
                        WebElement miniChatEmbedded = getElementById("shopee-mini-chat-embedded");
                        sendChatToMiniChatEmbedded(miniChatEmbedded, message);
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

    private void openPageAt(int page) {
        WebElement shopeePage = getElementByClassName("shopee-pager");
        List<WebElement> listPage = getElementsByTagName(shopeePage, "li");
        if (listPage == null) {
            printE("openLastPage listPage null");
            return;
        }
        for (WebElement webElement : listPage) {
            try {
                int _page = Integer.parseInt(webElement.getText());
                if (_page == page) {
                    webElement.click();
                    orderLoadDone();
                    return;
                }
            } catch (Exception ignore) {

            }
        }
        boolean isNextPage = nextPage(false);
        if (isNextPage) {
            openPageAt(page);
        }
    }

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
}

