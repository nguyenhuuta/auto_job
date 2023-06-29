package com.autojob.tiktok;

import com.autojob.api.ApiManager;
import com.autojob.api.RequestQueue;
import com.autojob.base.WebDriverCallback;
import com.autojob.model.entities.AccountModel;
import com.autojob.model.entities.BaseResponse;
import com.autojob.model.entities.TiktokOrderRateBody;
import com.autojob.utils.ColorConst;
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
class TiktokOrderDetailTask extends BaseTiktokTask {
    final String urlDetail = "%sorder/detail?order_no=%s&shop_region=VN";
    List<TiktokOrderRateBody> jsonArray = new ArrayList<>();

    public TiktokOrderDetailTask(AccountModel accountModel, WebDriverCallback webDriverCallback) {
        super(accountModel, webDriverCallback);
    }

    private List<String> orderIds = new ArrayList<>();

    @Override
    public String jobName() {
        return "LẤY SĐT";

    }

    /**
     * Lấy orderId bởi type
     * type
     * 1: Lấy sđt từ đơn hàng
     * 2: Lấy đơn hàng chưa gửi lời cảm ơn
     */
    List<String> orderStringByType() throws InterruptedException {
        Call<BaseResponse<List<String>>> call = ApiManager.BICA_ENDPOINT.orderIdsByType(accountModel.shopId, 1);
        return RequestQueue.getInstance().executeRequest(call);
    }

    String randomURL() {
        print("Mở trang bất kỳ tránh BLOCK");
        int random = Utils.randomInteger(1, 3);
        if (random == 1) {
            return TiktokParentTask.ENDPOINT + "product/rating";
        } else if (random == 2) {
            return TiktokParentTask.ENDPOINT + "profile/seller-profile";
        }
        return TiktokParentTask.ENDPOINT + "homepage";
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
            String url = randomURL();
            load(url);
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
            openOrderDetail();
        } catch (Exception e) {
            printException(e);
        } finally {
            print("HOÀN THÀNH");
        }
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
                openUrl(url, By.xpath("//div[contains(@class, 'IMICon__IMIconBackground')]"), "Icon Chat", 15);
                delaySecond(2);
                String phone = getBuyerPhone();
                if (phone.isEmpty()) {
                    phone = "Không lấy được phone";
                }
                print("SĐT: " + phone);
                TiktokOrderRateBody body = new TiktokOrderRateBody();
                body.orderId = orderId;
                body.buyerPhone = phone;
                jsonArray.add(body);

                if (jsonArray.size() == 10) {
                    updateOrderToServer();
                }
            } catch (Exception e) {
                printE("OpenOrderDetail Lỗi " + e);
                delaySecond(30);
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
}