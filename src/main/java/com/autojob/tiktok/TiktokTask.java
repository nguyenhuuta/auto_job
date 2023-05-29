package com.autojob.tiktok;

import com.autojob.base.WebDriverCallback;
import com.autojob.model.entities.AccountModel;
import com.autojob.task.BaseWebViewTask;
import com.autojob.utils.Logger;
import com.autojob.utils.TimeUtils;
import com.autojob.utils.Utils;
import javafx.scene.paint.Color;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import java.util.*;


/**
 * Created by OpenYourEyes on 04/05/2023
 */
public class TiktokTask extends BaseWebViewTask {
    static final String ENDPOINT = "https://seller-vn.tiktok.com/";
    static final String URL_LOGIN = ENDPOINT + "account/login";
    private boolean isFirst = true;
    static final String _ORDER_DELIVERED = ENDPOINT + "order?order_status[]=310&selected_sort=4&tab=shipped"; // selected_sort=6&tab=completed
    String ORDER_DELIVERED;

    int startDay = 0;

    public TiktokTask(AccountModel accountModel, WebDriverCallback callback) {
        super(accountModel, callback);
    }

    @Override
    public String jobName() {
        return "GỬI CẢM ƠN";
    }


    @Override
    public void run() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        System.out.println("DKS " + hour);
        if (hour < 8 || hour > 22) {
            updateListView("Ngoai giờ hoạt động 8h -> 22h");
            return;
        }
        int start = TimeUtils.getStartOfDay();
        if (startDay != start) {
            int end = TimeUtils.getEndOfDay();
            ORDER_DELIVERED = _ORDER_DELIVERED + String.format("&time_paid[]=%s&time_paid[]=%s", start, end);
            startDay = start;
        }

        if (status == RUNNING) {
            updateListView("JOB đang chay", Color.BLUEVIOLET);
            return;
        }
        try {
            status = RUNNING;
            checkLogin();
            loadOrderDelivered();
        } catch (Exception exx) {
            updateListView("CO LOI XAY RA " + exx);
        } finally {
            print("Status to IDLE");
            status = IDLE;
            isFirst = false;
        }

    }

    private void checkLogin() {
        if (!isFirst) {
            return;
        }
        load(URL_LOGIN);
        delaySecond(3);
        boolean needLogin = getElementById("sso_sdk") != null;
        if (needLogin) {
            webDriverCallback.triggerLogin(accountModel, true);
            updateListView(accountModel.shopName + " CHƯA LOGIN, YÊU CẦU LOGIN", Color.RED);
            do {
                updateListView("đợi 60s");
                delaySecond(60);
                System.out.println(getElementById("sso_sdk"));
                needLogin = getElementById("sso_sdk") != null;
            } while (needLogin);
            updateListView("60s để thao tác 1 lượt gửi tin nhắn để tắt các popup");
            delaySecond(60);
        }
        webDriverCallback.triggerLogin(accountModel, false);
    }

    void loadOrderDelivered() {
        try {
            load(ORDER_DELIVERED);
            String newOrderId = "";
            boolean isNextPage;
            do {
                WebElement parent = checkDoneByClass("arco-spin-children");
                List<WebElement> contactBuyer;
                int limit = 0;
                do {
                    if (limit == 2) {
                        updateListView("DONE, List đơn hàng trống");
                        return;
                    }
                    delaySecond(10);
                    contactBuyer = getElementsByCssSelector(parent, "div[data-log_click_for='contact_buyer']");
                    limit++;

                } while (contactBuyer == null);
                List<WebElement> listOrderId = getElementsByCssSelector(parent, "a[data-log_click_for='order_id_link']");
                if (listOrderId == null) {
                    printE("listOrderId NULL");
                    return;
                }
                int size = contactBuyer.size();
                if (listOrderId.size() != size) {
                    printE("listOrderId.size() != size");
                    return;
                }
                int index = 0;
                WebElement shopNameElement = getElementByClassName("index__name--z2FyO");
                String shopName = "";
                if (shopNameElement != null) {
                    shopName = shopNameElement.getText();
                }
                while (index < size) {
                    boolean clickable;
                    String orderId = listOrderId.get(index).getText();
                    if (Objects.equals(orderId, accountModel.lastOrderId)) {
                        String time = "ĐẾN LAST ORDER, lần chạy tiếp vào lúc: " + TimeUtils.addMinute(10);
                        if (newOrderId.isEmpty()) {
                            printGreen(time);
                            return;
                        }
                        updateAccountGoogleSheet(newOrderId);
                        printGreen(time);
                        accountModel.lastOrderId = newOrderId;
                        return;
                    }
                    if (newOrderId.isEmpty()) {
                        newOrderId = orderId;
                    }

                    WebElement element = contactBuyer.get(index);
                    String buyerName = element.getText();
                    do {
                        try {
                            element.click();
                            clickable = true;
                        } catch (Exception ex) {
                            Logger.info("DKS Exception CLICK " + ex);
                            clickable = false;
                            scrollBy(50);
                            delayMilliSecond(500);
                        }
                    } while (!clickable);
                    sendChat(buyerName, shopName);
                    index++;
                }
                scrollToBottom();
                delaySecond(2);
                isNextPage = nextPage();
                if (!isNextPage) {
                    printGreen("DONE, HẾT PAGE");
                }
            } while (isNextPage);

        } catch (Exception e) {
            print("LoadOrderDelivered Exception " + e);
            e.printStackTrace();
        }
    }

    private void sendChat(String buyerName, String shopName) {
        try {
            print("Send chat to " + buyerName);
            delaySecond(5);
            ArrayList<String> tabs = new ArrayList<>(webDriver.getWindowHandles());
            webDriver.switchTo().window(tabs.get(1));
            String hello = "Xin chào ";
            if (buyerName != null) {
                hello += buyerName.toUpperCase() + ",";
            }
            String[] array = randomThanks(hello, shopName);
            WebElement input = checkDoneById("chat-input-textarea");
            // check khách hàng có đang chat với shop không?
            delaySecond(5);
            List<WebElement> listContent = getElementsByCssSelector("div.chatd-scrollView-content > div");
            if (listContent != null && listContent.size() > 3) {
                WebElement orderId = getElementByClassName("lcYZDVix4GsvKJ9NZfOP");
                String _orderId = "NULL";
                if (orderId != null) {
                    _orderId = orderId.getText();
                }
                printColor("[SKIP]Khách hàng đang có cuộc trò chuyện với shop, bỏ qua đơn hàng " + _orderId, Color.BLUE);
                webDriver.close();
                webDriver.switchTo().window(tabs.get(0));
                delaySecond(5);
                return;
            }
            delaySecond(15);
            WebElement textArea = getElementByTagName(input, "textarea");
            for (String value : array) {
                textArea.sendKeys(value);
                textArea.sendKeys(Keys.SHIFT, Keys.ENTER);
                delaySecond(1);
            }
            textArea.sendKeys(Keys.ENTER);
            delayBetween(5, 10);
            webDriver.close();
            webDriver.switchTo().window(tabs.get(0));
            delayBetween(20, 30);
            delay5to10s();
        } catch (Exception exception) {
            printException(exception);
            exception.printStackTrace();
        }
    }

    private String[] randomThanks(String buyer, String shopName) {
        String[] thank1 = new String[]{
                buyer,
                "Cảm ơn bạn đã mua hàng tại cửa hàng của chúng tôi! Sự ủng hộ của bạn là động lực lớn để chúng tôi tiếp tục nỗ lực cung cấp những sản phẩm và dịch vụ tốt nhất cho khách hàng.",
                "Nếu bạn hài lòng với sản phẩm và dịch vụ của chúng tôi, xin vui lòng đánh giá 5* hoặc có bất kỳ câu hỏi hay ý kiến đóng góp nào, xin vui lòng liên hệ với chúng tôi. Chúng tôi luôn sẵn sàng hỗ trợ bạn.",
                "Xin lần nữa cảm ơn bạn rất nhiều!",
                "Trân trọng,",
                shopName.toUpperCase(),
        };

        String[] thank2 = new String[]{
                "Chào bạn",
                "Shop cảm ơn bạn v ì đã lựa chọn cửa hàng của chúng tôi và mua hàng tại đây.",
                "Nếu bạn hài lòng với sản phẩm và dịch vụ của chúng tôi, xin vui lòng đánh giá 5* hoặc có bất kỳ câu hỏi hay ý kiến đóng góp nào, xin vui lòng liên hệ với chúng tôi. Chúng tôi luôn sẵn sàng hỗ trợ bạn.",
                "Xin lần nữa cảm ơn bạn rất nhiều!",
                "Trân trọng,",
                shopName.toUpperCase(),
        };
        String[] thank3 = new String[]{
                "Xin chân thành cảm ơn bạn đã lựa chọn cửa hàng của chúng tôi và mua hàng.",
                "Nếu bạn hài lòng với dịch vụ của chúng tôi, xin vui lòng đánh giá 5* để giúp chúng tôi phục vụ tốt hơn.",
                "Nếu có vấn đề gì bạn có thể chat với shop để shop hỗ trợ bạn nhé.",
                "Một lần nữa cảm ơn sự ủng hộ của bạn và mong được gặp lại bạn trong những lần mua sắm tiếp theo!"
        };

        List<String[]> list = new ArrayList<>();
        list.add(thank1);
        list.add(thank2);
        list.add(thank3);
        int number = Utils.getRandomNumber(0, 2);
        return list.get(number);
    }

    private boolean nextPage() {
        try {
            WebElement pageContainer = getElementByClassName("zep-pagination-list");
            List<WebElement> pages = getElementsByClassName(pageContainer, "zep-pagination-item");
            if (pages == null) {
                return false;
            }
            int size = pages.size();
            for (WebElement element : pages) {
                String active = element.getAttribute("data-active");
                if (Objects.equals(active, "true")) {
                    print("PAGE " + element.getText());
                    break;
                }
            }
            WebElement lastElement = pages.get(size - 1);
            String att = lastElement.getAttribute("class");
            boolean isDisable = att.contains("disabled");
            if (!isDisable) {
                lastElement.click();
            }
            return !isDisable;
        } catch (Exception ex) {
            printException(ex);
            return false;
        }

    }
}
