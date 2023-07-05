package com.autojob.tiktok;

import com.autojob.api.ApiManager;
import com.autojob.api.RequestQueue;
import com.autojob.base.WebDriverCallback;
import com.autojob.model.entities.TiktokAffiliateOrderBody;
import com.autojob.model.entities.AccountModel;
import com.autojob.model.entities.BaseResponse;
import com.autojob.utils.ColorConst;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.SystemUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import retrofit2.Call;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by OpenYourEyes on 27/06/2023
 */
class TiktokAffiliateOrderTask extends BaseTiktokTask {
    final String affiliateUrl = "https://affiliate.tiktok.com/product/order";
    private List<String> orderIds = new ArrayList<>();
    List<TiktokAffiliateOrderBody> jsonArray = new ArrayList<>();

    public TiktokAffiliateOrderTask(AccountModel accountModel, WebDriverCallback webDriverCallback) {
        super(accountModel, webDriverCallback);
    }

    @Override
    public void updateApi() {
        updateOrderToServer();
    }

    List<String> orderIdsByType() throws InterruptedException {
        Call<BaseResponse<List<String>>> call = ApiManager.BICA_ENDPOINT.orderIdsByType(accountModel.shopId, 3);
        return RequestQueue.getInstance().executeRequest(call);
    }

    public void updateOrderToServer() {
        if (jsonArray.isEmpty()) {
            return;
        }
        log("Data gửi lên server " + jsonArray);
        try {
            printGreen("Đang cập nhật lên server");
            Call<BaseResponse<Object>> call = ApiManager.BICA_ENDPOINT.updateAffiliateOrder(jsonArray);
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
            orderIds = orderIdsByType();
            printColor("LIST ĐƠN HÀNG: " + orderIds.size(), Color.WHITE, ColorConst.blueviolet);
            if (orderIds.isEmpty()) {
                return;
            }
//            orderIds.clear();
//            orderIds.add("577491039904762048");
//            orderIds.add("577480232197261318");
//            orderIds.add("577492827605535696");
//            orderIds.add("577492827605535690");

            openAffiliatePage();

        } catch (Exception e) {
            printException(e);
        }
    }

    void openAffiliatePage() throws Exception {

        List<WebElement> comboBoxList = openUrlByList(affiliateUrl, By.xpath("//div[@role='combobox']"), "DropDownOrder");
        if (comboBoxList == null || comboBoxList.size() != 2) {
            throw new InterruptedException("ComboBox is NUll or size != 2 " + comboBoxList);
        }
        WebElement comboBox = comboBoxList.get(0);
        print("Click Dropdown");
        comboBox.click();
        delaySecond(2);
        WebElement listBox = getElementById("m4b-base-select-popup-0");
        List<WebElement> li = getElementsByTagName(listBox, "li");
        if (li != null && li.size() == 4) {
            print("Click ID Đơn Hàng");
            li.get(3).click();
        } else {
            throw new InterruptedException("Tag li is NUll or size != 4 " + li);
        }
        delaySecond(2);
        String textDropdown = comboBox.getText();
        boolean isValid = "ID đơn hàng".equals(textDropdown) || "Order ID".equals(textDropdown);
        if(isValid){
            print("Chọn " + textDropdown);
        }else{
            printColor("Chọn " + textDropdown, Color.RED);
            screenShotFull("affiliate");
            throw new InterruptedException("Không thể chọn ID đơn hàng affiliate");
        }
        WebElement inputSearch = getElementByXpath("//input[@placeholder='Search orders' or @placeholder='Tìm kiếm đơn hàng']");
        if (inputSearch == null) {
            throw new InterruptedException("InputSearch Not Found");
        }
        inputSearch.click();

        int size = orderIds.size();
        int index = 0;
        while (index < size) {
            try {
                System.out.println(inputSearch.getText());
                Keys key = SystemUtils.IS_OS_WINDOWS ? Keys.CONTROL : Keys.COMMAND;
                inputSearch.sendKeys(Keys.chord(key, "a", Keys.DELETE));
                print("Xoá kết quả cũ");
                delaySecond(2);
                String order = orderIds.get(index);
                print("Tìm đơn hàng " + order);
                String currentIndex = (index + 1) + "/" + size;
                String format = String.format("============ %s| %s ============", currentIndex, order);
                print(format);
                inputSearch.sendKeys(order);
                delayMilliSecond(300);
                inputSearch.sendKeys(Keys.ENTER);
                delaySecond(6);
                WebElement loading;
                do {
                    loading = getElementByClassName("m4b-base-spin-loading-layer");
                    delaySecond(1);
                    String status = loading == null ? "ẩn" : "hiện";
                    print("Loading " + status);
                } while (loading != null);

                WebElement noData = getElementByClassName("m4b-base-table-no-data");
                if (noData != null) {
                    print("Đơn hàng không phải affiliate");
                    index++;
                    TiktokAffiliateOrderBody orderBody = new TiktokAffiliateOrderBody();
                    orderBody.orderId = order;
                    orderBody.sourceOrder = 0;
                    jsonArray.add(orderBody);
                    continue;
                }

                List<WebElement> elements = getElementsByClassName("m4b-base-table-content-inner");
                if (elements == null || elements.size() != 2) {
                    throw new InterruptedException("m4b-base-table-content-inner null or != 2 " + elements);
                }
                WebElement element = elements.get(1);

                List<WebElement> elementLis = element.findElements(By.cssSelector("tbody > tr"));
                if (elementLis == null) {
                    throw new InterruptedException("Item order null");
                }
                String creator = "", sourOrder = "";
                int discount = 0;
                for (WebElement e : elementLis) {
                    List<String> text = Arrays.asList(e.getText().split("\n"));
                    creator = text.get(5).replace("@", "");
                    sourOrder = text.get(7);
                    discount += Integer.parseInt(text.get(10).replace("₫", ""));
                }
                printColor(creator + " | " + sourOrder + " | " + discount, Color.YELLOWGREEN);
                delaySecond(5);
                TiktokAffiliateOrderBody orderBody = new TiktokAffiliateOrderBody();
                orderBody.orderId = order;
                int type = 0;
                if ("Phát trực tiếp".equals(sourOrder) || "Livestream".equals(sourOrder)) {
                    type = 1;
                } else if ("Video".equals(sourOrder)) {
                    type = 2;
                } else if ("Trưng bày".equals(sourOrder)) {
                    type = 3;
                }
                orderBody.sourceOrder = type;
                orderBody.affiliateId = creator;
                orderBody.commissionDiscount = discount;
                jsonArray.add(orderBody);
                if (jsonArray.size() >= 10) {
                    updateOrderToServer();
                }
            } catch (Exception ex) {
                printException(ex);
            }
            index++;
        }
        updateOrderToServer();

    }

    public void clearConsoleErrors() {
        System.out.println("CLEAR LOG");
        JavascriptExecutor js = (JavascriptExecutor) webDriver;
        String script = "console.clear();";
        js.executeScript(script);
        LogEntries logs = webDriver.manage().logs().get(LogType.PERFORMANCE);
    }

    void checkDone() {
        LogEntries logs = webDriver.manage().logs().get(LogType.PERFORMANCE);
        for (Iterator<LogEntry> it = logs.iterator(); it.hasNext(); ) {
            LogEntry entry = it.next();

            try {
                JSONObject json = new JSONObject(entry.getMessage());

                JSONObject message = json.getJSONObject("message");
                String method = message.getString("method");

                if ("Network.responseReceived".equals(method)) {
                    JSONObject params = message.getJSONObject("params");
                    JSONObject response = params.getJSONObject("response");
                    String messageUrl = response.getString("url");
                    String url = "https://affiliate.tiktok.com/api/v1/affiliate/orders";
                    status = response.getInt("status");
                    if (messageUrl.contains(url)) {
                        log(method + "/" + status + " / " + messageUrl);
                    }

                    if (messageUrl.contains(url) && status == 200) {
                        print("LOAD DONE");
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        clearConsoleErrors();
    }

    @Override
    public String jobName() {
        return "SyncAffiliate";
    }
}
