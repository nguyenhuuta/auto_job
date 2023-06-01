package com.autojob.tiktok;

import com.autojob.api.ApiManager;
import com.autojob.api.RequestQueue;
import com.autojob.base.WebDriverCallback;
import com.autojob.model.entities.AccountModel;
import com.autojob.model.entities.BaseResponse;
import com.autojob.model.entities.TiktokOrderRateBody;
import com.autojob.task.BaseWebViewTask;
import com.autojob.utils.Utils;
import javafx.scene.paint.Color;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import retrofit2.Call;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by OpenYourEyes on 01/06/2023
 */
class TiktokOrderDetailTask extends BaseWebViewTask {
    final String urlDetail = "%sorder/detail?order_no=%s&shop_region=VN";
    int type;
    List<TiktokOrderRateBody> jsonArray = new ArrayList<>();

    public TiktokOrderDetailTask(AccountModel accountModel, WebDriverCallback webDriverCallback, int type) {
        super(accountModel, webDriverCallback);
        this.type = type;
    }

    private List<String> orderIds = new ArrayList<>();

    @Override
    public String jobName() {
        if (type == 1) {
            return "LẤY SĐT";
        }
        return "GỬI CẢM ƠN";
    }

    /**
     * Lấy orderId bởi type
     * type
     * 1: Lấy sđt từ đơn hàng
     * 2: Lấy đơn hàng chưa gửi lời cảm ơn
     *
     * @return
     */
    List<String> orderStringByType() throws InterruptedException {
        Call<BaseResponse<List<String>>> call = ApiManager.BICA_ENDPOINT.orderNeedBuyerPhone(accountModel.shopId, type);
        return RequestQueue.getInstance().executeRequest(call);
    }

    /**
     * Cập nhật SĐT, sendThanks vào đơn hàng
     */
    public void updateOrder(List<TiktokOrderRateBody> body) throws InterruptedException {
        Call<BaseResponse<Object>> call = ApiManager.BICA_ENDPOINT.updateBuyer(body);
        RequestQueue.getInstance().executeRequest(call);
    }


    @Override
    public void run() {
        try {
            delaySecond(3);
            orderIds = orderStringByType();
            if (orderIds.isEmpty()) {
                updateListView("List đơn hàng trống");
                return;
            }
            log("OrderIds " + orderIds);
            openOrderDetail();
        } catch (Exception e) {
            printException(e);
            e.printStackTrace();
        }

    }

    void openOrderDetail() {
        int size = orderIds.size();
        int index = 0;
        while (index < size) {
            String orderId = "577344802301184671";//orderIds.get(index);
            print("============ Đơn hàng " + orderId + "============");
            String url = String.format(urlDetail, TiktokParentTask.ENDPOINT, orderId);
            load(url);
            delaySecond(5);

            switch (type) {
                case 1:
                    String phone = getBuyerPhone(1);
                    if (phone.isEmpty()) {
                        phone = "Không lấy được phone";
                    }
                    TiktokOrderRateBody body = new TiktokOrderRateBody();
                    body.orderId = orderId;
                    body.buyerPhone = phone;
                    jsonArray.add(body);
                    break;
                case 2:
                    List<WebElement> elements = getElementsByXpath("//div[contains(text(),'Tên')]"); //Tên
                    if (elements != null) {
                        for (WebElement element : elements) {
                            System.out.println(element.getText());
                        }
                    } else {
                        printE("Lấy tên buyer thất bại");
                    }
                    break;
            }
            print("============ Đơn hàng " + orderId + "============");
            index++;
        }
        try {
            updateOrder(jsonArray);
            printGreen("Cập nhật SĐT thành công lên server");
        } catch (InterruptedException e) {
            printException(e);
            e.printStackTrace();
        }

    }

    private String getBuyerPhone(int count) {
        if (count == 5) {
            printE("Không tìm thấy sđt người mua");
            return "";
        }
        WebElement element = getElementByClassName("order-arco-icon-eyeInvisible");
        if (element == null) {
            delaySecond(2);
            getBuyerPhone(count + 1);
            return "";
        }
        print("Click Eye");
        element.click();
        delaySecond(3);
        element = getElementByClassName("order-arco-icon-eye");
        if (element == null) {
            print("Click eye không tìm thấy element SDT");
            return "";
        }
        WebElement parentElement = element.findElement(By.xpath("./.."));
        return parentElement.getText();
    }


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
}