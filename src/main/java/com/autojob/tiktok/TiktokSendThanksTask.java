package com.autojob.tiktok;

import com.autojob.api.ApiManager;
import com.autojob.api.RequestQueue;
import com.autojob.base.WebDriverCallback;
import com.autojob.model.entities.AccountModel;
import com.autojob.model.entities.BaseResponse;
import com.autojob.model.entities.TiktokOrderRateBody;
import com.autojob.utils.ColorConst;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.SystemUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import retrofit2.Call;

import java.security.Key;
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
        Call<BaseResponse<List<String>>> call = ApiManager.BICA_ENDPOINT.orderIdsByType(accountModel.shopId, 2);
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
            delaySecond(60);
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
                hidePopupReplyLate();
                String orderId = orderIds.get(index);
                String currentIndex = (index + 1) + "/" + size;
                String format = String.format("============ %s| %s ============", currentIndex, orderId);
                print(format);
                System.out.println(webDriver.manage().window().getSize());
                WebElement inputSearch = checkDoneBy(By.cssSelector("input[data-dtid='order.filter_area.input.main_order_id']"), "InputSearch");
                inputSearch.click();
                delayMilliSecond(500);
                Keys key = SystemUtils.IS_OS_WINDOWS ? Keys.CONTROL : Keys.COMMAND;
                inputSearch.sendKeys(Keys.chord(key, "a", Keys.DELETE));
                print("Xoá kết quả cũ");
                delayMilliSecond(500);
                inputSearch.sendKeys(orderId);
                delayMilliSecond(400);
                print("Nhập đơn hàng mới");
                inputSearch.sendKeys(Keys.ENTER);
                print("Tìm kiếm đơn " + orderId);
                WebElement chatIcon = checkDoneBy(By.cssSelector("div[data-log_click_for='contact_buyer']"), "ContactBuyer");
                int count = 0;
                WebElement chatSVG;
                do {
                    delaySecond(2);
                    chatSVG = getElementByTagName(chatIcon, "svg");
                    count++;
                    if (count > 3) {
                        throw new InterruptedException("Icon Chat không hiển thị");
                    }
                } while (chatSVG == null);

                print("Đợi click chat");
                tryClick(chatIcon, 100);

                String buyerName = chatIcon.getText();
                System.out.println("buyerName " + buyerName);
                sendChat(buyerName);
                TiktokOrderRateBody bodyThanks = new TiktokOrderRateBody();
                bodyThanks.orderId = orderId;
                bodyThanks.sendThanks = true;
                jsonArray.add(bodyThanks);
                if (jsonArray.size() >= 10) {
                    updateOrderToServer();
                }
            } catch (Exception e) {
                printE("SearchOrder Lỗi " + e);
                e.printStackTrace();
                updateApi();
                delaySecond(5);
            }
            index++;
        }

        ArrayList<String> tabs = new ArrayList<>(webDriver.getWindowHandles());
        try {
            if (tabs.size() == 2) {
                webDriver.switchTo().window(tabs.get(1));
                webDriver.close();
                webDriver.switchTo().window(tabs.get(0));
            }
        } catch (Exception e) {

        }

        updateOrderToServer();
    }


    private void sendChat(String buyerName) {
        delaySecond(5);
        ArrayList<String> tabs = new ArrayList<>(webDriver.getWindowHandles());
        try {
            if (tabs.size() != 2) {
                throw new InterruptedException("Mở tab chat lỗi");
            }
            webDriver.switchTo().window(tabs.get(1));

            String[] array = message();
            WebElement buyerNameElement;
            String text = "";
            int count = 0;
            do {
                delaySecond(2);
                buyerNameElement = checkDoneBy(By.xpath("//div[contains(@class, 'FOIFN_')]"), "BuyerName");
                if (buyerNameElement != null) {
                    text = buyerNameElement.getText();
                    if (count == 5) {
                        buyerName = text;
                    }
                    print("new Buyer: " + text + "/" + buyerName);
                    count++;
                }
                count++;
            } while (!buyerName.equals(text));
            WebElement textArea = checkDoneBy(By.xpath("//*[@id='chat-input-textarea']/textarea"), "ChatInput", 10);
            print("Hiển thị chat thành công");
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
//            webDriver.close();
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