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
import java.util.Arrays;
import java.util.Collections;
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
            log("OrderIds " + jobName() + " " + orderIds);
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
            try {
                String orderId = orderIds.get(index);
                String currentIndex = (index + 1) + "/" + size;
                String format = String.format("============ %s. %s ============", currentIndex, orderId);
                print(format);
                String url = String.format(urlDetail, TiktokParentTask.ENDPOINT, orderId);
                load(url);
                switch (type) {
                    case 1: // Lấy SĐT
                        String phone = getBuyerPhone();
                        if (phone.isEmpty()) {
                            phone = "Không lấy được phone";
                        }
                        TiktokOrderRateBody body = new TiktokOrderRateBody();
                        body.orderId = orderId;
                        body.buyerPhone = phone;
                        jsonArray.add(body);
                        break;
                    case 2:// Gửi cảm ơn
                        WebElement chatIcon = checkDoneBy(By.xpath("//div[contains(@class, 'IMICon__IMIconBackground')]"), "Icon Chat");
                        TiktokOrderRateBody bodyThanks = new TiktokOrderRateBody();
                        if (chatIcon != null) {
                            chatIcon.click();
                            sendChat();
                        }
                        bodyThanks.orderId = orderId;
                        bodyThanks.sendThanks = true;
                        jsonArray.add(bodyThanks);
                        break;
                }
                print("============ END ============");
            } catch (Exception e) {
                printE("OpenOrderDetail Lỗi " + e);
                delaySecond(5);
            }
            index++;
        }
        try {
            updateOrder(jsonArray);
            printGreen("Cập nhật lên server thành công");
        } catch (InterruptedException e) {
            printException(e);
            e.printStackTrace();
        }

    }

    private String getBuyerPhone() {
        WebElement element = checkDoneBy(By.className("order-arco-icon-eyeInvisible"), "Eye show phone");
        if (element == null) {
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


    private void sendChat() {
        try {
            delaySecond(5);
            ArrayList<String> tabs = new ArrayList<>(webDriver.getWindowHandles());
            webDriver.switchTo().window(tabs.get(1));
            String[] array = new String[]{
                    "Shop thấy bạn đã nhận hàng",
                    "Không biết sản phẩm bên mình bạn có hài lòng không ạ?",
                    "Hàng bên mình được đổi trả trong 3 ngày.",
                    "Nếu bạn hài lòng hãy ĐÁNH GIÁ cho shop 5* nhé ^^.",
                    "ĐỪNG VỘI ĐÁNH GIÁ XẤU nếu sản phẩm có vấn đề, hãy nhắn tin hoặc liên hệ: 0342.092.686 để shop xử lý ngay ạ."
            };
            WebElement textArea = checkDoneBy(By.xpath("//*[@id='chat-input-textarea']/textarea"), "ChatInput");
            if (textArea == null) {
                return;
            }
//          Check khách hàng có đang chat với shop không?
//            delaySecond(5);
//            List<WebElement> listContent = getElementsByXpath("//div[@class='chatd-scrollView-content']/div");
//            System.out.println(listContent);
//            if (listContent != null && listContent.size() > 3) {
//                printColor("[SKIP]Khách hàng đang có cuộc trò chuyện với shop, bỏ qua đơn hàng ", Color.BLUE);
//            } else {
            for (String value : array) {
                textArea.sendKeys(value);
                textArea.sendKeys(Keys.SHIFT, Keys.ENTER);
            }
            textArea.sendKeys(Keys.ENTER);
            print("Gửi chat thành công");
            delayBetween(5, 10);
            webDriver.close();
            webDriver.switchTo().window(tabs.get(0));
            print("Tắt chat");
            delayBetween(10, 20);
        } catch (Exception exception) {
            printException(exception);
            exception.printStackTrace();
        }
    }
}