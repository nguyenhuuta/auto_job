package com.autojob.tiktok;

import com.autojob.api.ApiManager;
import com.autojob.api.RequestQueue;
import com.autojob.base.WebDriverCallback;
import com.autojob.model.entities.AccountModel;
import com.autojob.model.entities.BaseResponse;
import com.autojob.model.entities.TiktokOrderRateBody;
import com.autojob.task.BaseWebViewTask;
import com.autojob.utils.Logger;
import com.autojob.utils.TimeUtils;
import com.autojob.utils.Utils;
import javafx.scene.paint.Color;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import retrofit2.Call;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by OpenYourEyes on 01/06/2023
 */
class TiktokSendThanksTask extends BaseWebViewTask {
    private List<String> orderIds = new ArrayList<>();

    public TiktokSendThanksTask(AccountModel accountModel, WebDriverCallback webDriverCallback) {
        super(accountModel, webDriverCallback);
    }

    @Override
    public String jobName() {
        return "GỬI CẢM ƠN";
    }

    @Override
    public void run() {
        try {
            delaySecond(3);
            print("=======BẮT ĐẦU GỬI CẢM ƠN=======");
//            orderIds = TiktokParentTask.orderStringByType(accountModel.shopId, 2);
            log("orderIds " + orderIds);
            if (orderIds.isEmpty()) {
                updateListView("Không có đơn hàng chưa gửi cảm ơn");
                return;
            }
//            openOrderDetail();
            print("=======KẾT THÚC GỬI CẢM ƠN=======");
        } catch (Exception e) {
            printException(e);
            e.printStackTrace();
        }
    }


//    void loadOrderDelivered() {
//        try {
//            load(ORDER_DELIVERED);
//            String newOrderId = "";
//            boolean isNextPage;
//            do {
//                WebElement parent = checkDoneByClass("arco-spin-children");
//                List<WebElement> contactBuyer;
//                int limit = 0;
//                do {
//                    if (limit == 2) {
//                        updateListView("DONE, List đơn hàng trống");
//                        return;
//                    }
//                    delaySecond(10);
//                    contactBuyer = getElementsByCssSelector(parent, "div[data-log_click_for='contact_buyer']");
//                    limit++;
//
//                } while (contactBuyer == null);
//                List<WebElement> listOrderId = getElementsByCssSelector(parent, "a[data-log_click_for='order_id_link']");
//                if (listOrderId == null) {
//                    printE("listOrderId NULL");
//                    return;
//                }
//                int size = contactBuyer.size();
//                if (listOrderId.size() != size) {
//                    printE("listOrderId.size() != size");
//                    return;
//                }
//                int index = 0;
//                WebElement shopNameElement = getElementByClassName("index__name--z2FyO");
//                String shopName = "";
//                if (shopNameElement != null) {
//                    shopName = shopNameElement.getText();
//                }
//                while (index < size) {
//                    boolean clickable;
//                    String orderId = listOrderId.get(index).getText();
//                    String startOrder = String.format("--------- Đơn hàng %s ---------", orderId);
//                    print(startOrder);
//                    if (Objects.equals(orderId, accountModel.lastOrderId)) {
//                        String time = "ĐẾN LAST ORDER, lần chạy tiếp vào lúc: " + TimeUtils.addMinute(10);
//                        if (newOrderId.isEmpty()) {
//                            printGreen(time);
//                            return;
//                        }
//                        updateAccountGoogleSheet(newOrderId);
//                        printGreen(time);
//                        return;
//                    }
//                    if (newOrderId.isEmpty()) {
//                        newOrderId = orderId;
//                    }
//
//                    WebElement element = contactBuyer.get(index);
//                    String buyerName = element.getText();
//                    do {
//                        try {
//                            element.click();
//                            clickable = true;
//                        } catch (Exception ex) {
//                            Logger.info("DKS Exception CLICK " + ex);
//                            clickable = false;
//                            scrollBy(50);
//                            delayMilliSecond(500);
//                        }
//                    } while (!clickable);
//                    sendChat(buyerName, shopName);
//                    String endOrder = String.format("---------Gửi xong %s ---------", orderId);
//                    print(endOrder);
//                    index++;
//                }
//                scrollToBottom();
//                delaySecond(2);
//                isNextPage = nextPage();
//                if (!isNextPage) {
//                    String time = "HẾT PAGE, lần chạy tiếp theo " + TimeUtils.addMinute(10);
//                    printGreen(time);
//                    if (newOrderId != null && !newOrderId.isEmpty()) {
//                        updateAccountGoogleSheet(newOrderId);
//                    }
//                }
//            } while (isNextPage);
//
//        } catch (Exception e) {
//            print("LoadOrderDelivered Exception " + e);
//            e.printStackTrace();
//        }
//    }

    private void sendChat(String buyerName, String shopName) {
        try {
            String hello = "Xin chào ";
            if (buyerName != null) {
                buyerName = buyerName.toUpperCase();
                hello += buyerName + ",";
                print("Send chat to " + buyerName);
            }

            delaySecond(5);
            ArrayList<String> tabs = new ArrayList<>(webDriver.getWindowHandles());
            webDriver.switchTo().window(tabs.get(1));

            String[] array = randomThanks(hello, shopName);
            WebElement input = checkDoneById("chat-input-textarea");
            // check khách hàng có đang chat với shop không?
            delaySecond(5);
            List<WebElement> listContent = getElementsByCssSelector("div.chatd-scrollView-content > div");
            if (listContent != null && listContent.size() > 3) {
                printColor("[SKIP]Khách hàng đang có cuộc trò chuyện với shop, bỏ qua đơn hàng ", Color.BLUE);
                webDriver.close();
                webDriver.switchTo().window(tabs.get(0));
                delaySecond(5);
                return;
            }
            delaySecond(10);
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
            print("----------------------------");
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
