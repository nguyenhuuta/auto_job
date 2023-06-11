package com.autojob.tiktok;

import com.autojob.api.ApiManager;
import com.autojob.api.RequestQueue;
import com.autojob.base.WebDriverCallback;
import com.autojob.model.entities.AccountModel;
import com.autojob.model.entities.BaseResponse;
import com.autojob.model.entities.TiktokOrderRateBody;
import com.autojob.utils.ColorConst;
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
class TiktokSendThanksTask extends BaseTiktokTask {
    final String urlDetail = "order?selected_sort=6&tab=all";
    List<TiktokOrderRateBody> jsonArray = new ArrayList<>();

    public TiktokSendThanksTask(AccountModel accountModel, WebDriverCallback webDriverCallback) {
        super(accountModel, webDriverCallback);
    }

    private List<String> orderIds = new ArrayList<>();

    @Override
    public String jobName() {
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
        Call<BaseResponse<List<String>>> call = ApiManager.BICA_ENDPOINT.orderNeedBuyerPhone(accountModel.shopId, 2);
        return RequestQueue.getInstance().executeRequest(call);
    }

    @Override
    public void updateApi() {
        updateOrderToServer();
    }

    /**
     * Cập nhật SĐT, sendThanks vào đơn hàng
     */
    public void updateOrderToServer() {
        if (jsonArray.isEmpty()) {
            return;
        }
        log("Data gửi lên server " + jsonArray);
        try {
            printGreen("Đang cập nhật lên server");
            Call<BaseResponse<Object>> call = ApiManager.BICA_ENDPOINT.updateBuyer(jsonArray);
            RequestQueue.getInstance().executeRequest(call);
            printGreen("Cập nhật lên server thành công");
            jsonArray.clear();
        } catch (InterruptedException e) {
            e.printStackTrace();
            printE("updateOrderToServer lỗi");
        }
    }


    @Override
    public void run() {
        try {
            delaySecond(3);
            orderIds = orderStringByType();
            printColor("LIST ĐƠN HÀNG: " + orderIds.size(), Color.WHITE, ColorConst.blueviolet);
            if (orderIds.isEmpty()) {
                return;
            }
            load(TiktokParentTask.ENDPOINT + urlDetail);
            searchOrder();
        } catch (Exception e) {
            printException(e);
        } finally {
            print("HOÀN THÀNH");
        }
    }

    void searchOrder() {
        int size = orderIds.size();
        int index = 0;
        while (index < size) {
            try {
                String orderId = orderIds.get(index);
                String currentIndex = (index + 1) + "/" + size;
                String format = String.format("============ %s| %s ============", currentIndex, orderId);
                print(format);
                WebElement inputSearch = checkDoneBy(By.cssSelector("input[data-dtid='order.filter_area.input.main_order_id']"), "InputSearch");
                inputSearch.click();
                inputSearch.clear();
                delayMilliSecond(500);
                inputSearch.sendKeys(orderId);
                delayMilliSecond(400);
                inputSearch.sendKeys(Keys.ENTER);
                print("Tìm kiếm đơn " + orderId);
                WebElement chatIcon = checkDoneBy(By.cssSelector("div[data-log_click_for='contact_buyer']"), "ContactBuyer");
                delaySecond(2);
                chatIcon.click();
                sendChat();
                TiktokOrderRateBody bodyThanks = new TiktokOrderRateBody();
                bodyThanks.orderId = orderId;
                bodyThanks.sendThanks = true;
                jsonArray.add(bodyThanks);
                if (jsonArray.size() == 10) {
                    updateOrderToServer();
                }
            } catch (Exception e) {
                printE("SearchOrder Lỗi " + e);
                delaySecond(30);
            }
            index++;
        }
        updateOrderToServer();
    }


    private void sendChat() {
        delaySecond(5);
        ArrayList<String> tabs = new ArrayList<>(webDriver.getWindowHandles());
        try {
            if (tabs.size() != 2) {
                throw new InterruptedException("Mở tab chat lỗi");
            }
            webDriver.switchTo().window(tabs.get(1));

            String[] array = message();
            WebElement textArea = checkDoneBy(By.xpath("//*[@id='chat-input-textarea']/textarea"), "ChatInput", 10);
            delaySecond(2);
            for (String value : array) {
                textArea.sendKeys(value);
                textArea.sendKeys(Keys.SHIFT, Keys.ENTER);
            }
            textArea.sendKeys(Keys.ENTER);
            print("Gửi chat thành công");
        } catch (Exception exception) {
            printException(exception);
            exception.printStackTrace();
        } finally {
            delayBetween(10, 20);
            webDriver.close();
            webDriver.switchTo().window(tabs.get(0));
            print("Tắt chat");
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
}