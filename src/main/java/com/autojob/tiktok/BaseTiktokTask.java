package com.autojob.tiktok;

import com.autojob.base.WebDriverCallback;
import com.autojob.model.entities.AccountModel;
import com.autojob.task.BaseWebViewTask;
import com.autojob.task.ILoadDonePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Created by OpenYourEyes on 05/06/2023
 */
abstract class BaseTiktokTask extends BaseWebViewTask {
    public BaseTiktokTask(AccountModel accountModel, WebDriverCallback webDriverCallback) {
        super(accountModel, webDriverCallback);
    }

    /**
     * Ẩn popup hỏi "trả lời khách hàng luôn"
     */
    public void hidePopupReplyLate() {
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
    }

    List<WebElement> openUrlByList(String url, By by, String tag) throws InterruptedException {
        return openUrlByList(url, by, tag, true);
    }

    List<WebElement> openUrlByList(String url, By by, String tag, boolean hasEmpty) throws InterruptedException {
        load(url);
        List<WebElement> listRate = checkDoneListBy(by, tag, hasEmpty);
        hidePopupReplyLate();
        return listRate;
    }


    WebElement openUrl(String url, By by, String tag) throws InterruptedException {
        load(url);
        WebElement element = checkDoneBy(by, tag);
        hidePopupReplyLate();
        return element;
    }
}
