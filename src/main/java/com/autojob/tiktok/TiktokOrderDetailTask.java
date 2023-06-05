package com.autojob.tiktok;

import com.autojob.api.ApiManager;
import com.autojob.api.RequestQueue;
import com.autojob.base.WebDriverCallback;
import com.autojob.model.entities.AccountModel;
import com.autojob.model.entities.BaseResponse;
import com.autojob.model.entities.TiktokOrderRateBody;
import com.autojob.task.BaseWebViewTask;
import com.autojob.utils.TimeUtils;
import javafx.scene.paint.Color;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import retrofit2.Call;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by OpenYourEyes on 01/06/2023
 */
class TiktokOrderDetailTask extends BaseWebViewTask {
    final String urlDetail = "%sorder/detail?order_no=%s&shop_region=VN";
    /**
     * 0: Trạng thái bắt đầu
     * 1: Lấy SĐT
     * 2: Gửi lời cảm ơn đơn hàng
     * 3: Trả lời đánh giá khách hàng
     */
    private int type = 0;
    List<TiktokOrderRateBody> jsonArray = new ArrayList<>();

    public TiktokOrderDetailTask(AccountModel accountModel, WebDriverCallback webDriverCallback) {
        super(accountModel, webDriverCallback);
    }

    private List<String> orderIds = new ArrayList<>();

    @Override
    public String jobName() {
        if (type == 1) {
            return "LẤY SĐT";
        } else if (type == 2) {
            return "GỬI CẢM ƠN";
        }
        return "";

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
        print("OrderByType " + accountModel.shopId + "/" + type);
        Call<BaseResponse<List<String>>> call = ApiManager.BICA_ENDPOINT.orderNeedBuyerPhone(accountModel.shopId, type);
        return RequestQueue.getInstance().executeRequest(call);
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
        }
    }


    @Override
    public void run() {
        try {
            type++;
            delaySecond(3);
            orderIds = orderStringByType();
            print("List đơn hàng: " + orderIds.size());
            if (orderIds.isEmpty()) {
                return;
            }
            openOrderDetail();
        } catch (Exception e) {
            printException(e);
        } finally {
            print("HOÀN THÀNH");
            if (type < 2) {
                run();
            } else {
                type = 0;
                String text = "LẦN CHẠY TỚI VÀO: " + TimeUtils.addMinute(10);
                print(text);
            }
        }
    }

    /**
     * Trả lời khách hàng từ đánh giá 1, 2*
     */
    public void feedbackOrder(String orderId) {
        type = 2;
        orderIds.add(orderId);
        openOrderDetail();
    }

    void openOrderDetail() {
        int size = orderIds.size();
        int index = 0;
        while (index < size) {
            try {
                String orderId = orderIds.get(index);
                String currentIndex = (index + 1) + "/" + size;
                String format = String.format("============ %s| %s ============", currentIndex, orderId);
                print(format);
                String url = String.format(urlDetail, TiktokParentTask.ENDPOINT, orderId);
                load(url);
                WebElement chatIcon = checkDoneBy(By.xpath("//div[contains(@class, 'IMICon__IMIconBackground')]"), "Icon Chat");
                delaySecond(2);
                // check có hiển thị popup trả lời tin nhắn ngay không
                WebElement popup = getElementByClassName("arco-popover-content");
                if (popup != null) {
                    print("Hiển thị popup trả lời tin nhắn");
                    try {
                        List<WebElement> actions = getElementsByTagName(popup, "button");
                        if (actions != null && actions.size() == 2) {
                            actions.get(0).click();
                        } else {
                            throw new InterruptedException("action button maybe null");
                        }
                        print("Click button: Có lẽ để sau");
                    } catch (Exception e) {
                        printE("Click button 'Có lẽ để sau' lỗi " + e);
                        executeScript("document.getElementsByClassName('zoomInFadeOut-enter-done')[0].style.display = 'none'");
                    }
                }
                switch (type) {
                    case 1: // Lấy SĐT
                        String phone = getBuyerPhone();
                        if (phone.isEmpty()) {
                            phone = "Không lấy được phone";
                        }
                        print("SĐT: " + phone);
                        TiktokOrderRateBody body = new TiktokOrderRateBody();
                        body.orderId = orderId;
                        body.buyerPhone = phone;
                        jsonArray.add(body);
                        break;
                    case 2:// Gửi cảm ơn
                        chatIcon.click();
                        sendChat();
                        TiktokOrderRateBody bodyThanks = new TiktokOrderRateBody();
                        bodyThanks.orderId = orderId;
                        bodyThanks.sendThanks = true;
                        jsonArray.add(bodyThanks);
                        break;
                }
                print("============ END ============");
                if (jsonArray.size() == 30) {
                    updateOrderToServer();
                }
            } catch (Exception e) {
                printE("OpenOrderDetail Lỗi " + e);
                delaySecond(5);
            }
            index++;
        }
        updateOrderToServer();
    }

    private String getBuyerPhone() {
        WebElement element;
        try {
            element = checkDoneBy(By.className("order-arco-icon-eyeInvisible"), "Icon Eye");
            element.click();
            delaySecond(3);
            element = getElementByClassName("order-arco-icon-eye");
            if (element == null) {
                throw new InterruptedException("Click eye: không tìm thấy element SDT");
            }
            WebElement parentElement = element.findElement(By.xpath("./.."));
            return parentElement.getText();
        } catch (InterruptedException e) {
            printE(e.getMessage());
            return "";
        }
    }


    private void sendChat() {
        try {
            delaySecond(5);
            ArrayList<String> tabs = new ArrayList<>(webDriver.getWindowHandles());
            if (tabs.size() == 1) {
                throw new InterruptedException("Mở tab chat lỗi");
            }
            webDriver.switchTo().window(tabs.get(1));
            String[] array = new String[]{
                    "Shop thấy bạn đã nhận hàng",
                    "Không biết sản phẩm bên mình bạn có hài lòng không ạ?",
                    "Hàng bên mình được đổi trả trong 3 ngày.",
                    "Nếu bạn hài lòng hãy ĐÁNH GIÁ cho shop 5* nhé ^^.",
                    "ĐỪNG VỘI ĐÁNH GIÁ XẤU nếu sản phẩm có vấn đề, hãy nhắn tin hoặc liên hệ: 0342.092.686 để shop xử lý ngay ạ."
            };
            WebElement textArea = checkDoneBy(By.xpath("//*[@id='chat-input-textarea']/textarea"), "ChatInput");
//          Check khách hàng có đang chat với shop không?
            delaySecond(5);
            List<WebElement> listContent = getElementsByXpath("//div[@class='chatd-scrollView-content']/div");
            System.out.println(listContent);
            if (listContent != null && listContent.size() > 3) {
                printColor("[SKIP]Khách hàng đang có cuộc trò chuyện với shop, bỏ qua đơn hàng ", Color.BLUE);
            } else {
                for (String value : array) {
                    textArea.sendKeys(value);
                    textArea.sendKeys(Keys.SHIFT, Keys.ENTER);
                }
                textArea.sendKeys(Keys.ENTER);
                print("Gửi chat thành công");
                delayBetween(5, 10);
            }
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