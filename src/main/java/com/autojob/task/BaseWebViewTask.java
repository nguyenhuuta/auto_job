package com.autojob.task;

import com.autojob.model.entities.AccountModel;
import com.autojob.model.entities.ChromeSetting;
import com.autojob.model.entities.MessageListView;
import com.autojob.utils.Logger;
import com.autojob.utils.Utils;
import com.autojob.utils.WebDriverUtils;
import javafx.scene.paint.Color;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public abstract class BaseWebViewTask extends TimerTask {
    public static final int CAPTCHA = -1;
    public static final int IDLE = 0;
    public static final int RUNNING = 1;
    public static final int FORCE_STOP = 2;
    public int status = IDLE;


    public String TAG;

    private static final int WEB_DRIVER_WAIT_TIMEOUT = 10;

    private static final int DEFAULT_WAIT_TIME = 500;

    public WebDriver webDriver;

    public WebDriverWait webDriverWait;

    public String getTag() {
        return TAG;
    }

    public void setTag(String TAG) {
        this.TAG = TAG;
    }

    public AccountModel accountModel;
    public BehaviorSubject<MessageListView> triggerCurrentHistory = BehaviorSubject.create();
    public PublishSubject<Integer> triggerTaskId = PublishSubject.create();

    public BaseWebViewTask(AccountModel accountModel) {
        this.accountModel = accountModel;
        startWeb();
    }

    public void startWeb() {
        String profilePath = System.getProperty("user.dir") + "/data/chrome_profile/" + accountModel.shopName;
        ChromeSetting chromeSetting = new ChromeSetting(true, 0, 0, profilePath, true);
        WebDriver webDriver = WebDriverUtils.getInstance().createWebDriver(chromeSetting);
        setWebDriver(webDriver);
        updateListView("Start CHROME with profile " + accountModel.shopName);
    }

    public void stopWeb() {
        try {
            try {
                webDriver.close();
            } catch (Exception ignored) {
            }
            webDriver.quit();
        } catch (Exception ignored) {
        }
    }

    public void startJob(int jobId) {
        triggerTaskId.onNext(jobId);
    }

    public void stopJob() {
        status = FORCE_STOP;
        updateListView("JOB đang chạy, vui lòng đợi...");
    }

    public abstract String jobName();

    public void updateListView(String message) {
        String content = String.format("[%s] %s", jobName(), message);
        MessageListView item = new MessageListView(content);
        triggerCurrentHistory.onNext(item);
    }

    public void updateListView(String message, Color color) {
        String content = String.format("[%s] %s", jobName(), message);
        MessageListView item = new MessageListView(content, color);
        triggerCurrentHistory.onNext(item);
    }

    public void setWebDriver(WebDriver webDriver) {
        this.webDriver = webDriver;
        webDriverWait = new WebDriverWait(webDriver, WEB_DRIVER_WAIT_TIMEOUT);
    }

    public void load(String url) {
        webDriver.get(url);
        waitLoadDone();
    }

    public void waitLoadDone() {
        webDriverWait.until((ExpectedCondition<Boolean>) wd ->
                ((JavascriptExecutor) wd).executeScript("return document.readyState").equals("complete"));
        print("LOAD DONE " + webDriver.getCurrentUrl());
    }

    protected WebDriver getWebDriver() {
        return webDriver;
    }

    void closeLoginDialog() {
        try {
            WebElement element = webDriver.findElement(By.id("login-modal"));
            if (element == null) {
                return;
            }
            WebElement buttonClose = webDriver.findElement(By.xpath("//*[@id=\"login-modal\"]/div[2]"));
            buttonClose.click();
        } catch (Exception e) {

        }

    }

    public void finishTask() {
        status = IDLE;
        webDriver = null;
        webDriverWait = null;
    }

    protected final void maximizeWebDriver() {
        try {
            webDriver.manage().window().maximize();
        } catch (Exception ex) {
            Logger.warning(TAG, "#maximizeWebDriver false.");
        }
    }

    protected final JavascriptExecutor getJavascriptExecutor() {
        if (!isValidTaskInRunning()) return null;
        if (getWebDriver() instanceof JavascriptExecutor) {
            return (JavascriptExecutor) getWebDriver();
        } else {
            throw new IllegalArgumentException("This web driver does not support javascript");
        }
    }

    protected final void waitDefault() {
        wait(DEFAULT_WAIT_TIME, TimeUnit.MILLISECONDS);
    }

    void wait(long time, TimeUnit timeUnit) {
        if (!isValidTaskInRunning()) return;
        if (time > 0) {
            try {
                Thread.sleep(timeUnit.toMillis(time));
            } catch (InterruptedException e) {
                Logger.warning(TAG, "InterruptedException: " + e.getMessage());
            }
        }
    }

    protected final void clickElementJs(WebElement element) {
        if (element != null) {
            waitDefault();
            try {
                if (isElementPresentAndDisplayed(element)) {
                    executeJavaScript("arguments[0].click();", element);
                }
            } catch (WebDriverException e) {
                Logger.warning(TAG, "clickElementJs : " + e.getMessage());
            }
        }
    }

    protected final void scrollToElement(WebElement element) {
        if (!isValidTaskInRunning()) return;
        try {
            getJavascriptExecutor().executeScript("arguments[0].scrollIntoView(true);", element);
        } catch (Exception e) {
            Logger.warning(TAG, e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    protected final void scrollBy(int y) {
        if (!isValidTaskInRunning()) return;
        try {
            print("scrollBy " + y);
            String scrip = String.format("window.scrollBy(%o,%o)", 0, y);
            getJavascriptExecutor().executeScript(scrip, "");
        } catch (Exception e) {
            Logger.warning(TAG, e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    protected final void scrollToTop() {
        if (!isValidTaskInRunning()) return;
        try {
            getJavascriptExecutor().executeScript("window.scrollTo(0, 0);");
        } catch (Exception e) {
            Logger.warning(TAG, e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    protected final void scrollToBottom() {
        if (!isValidTaskInRunning()) return;
        try {
            getJavascriptExecutor().executeScript("window.scrollTo(0, document.body.scrollHeight);");
        } catch (Exception e) {
            Logger.warning(TAG, e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }


    void scrollToTopAuto() {
        if (!isValidTaskInRunning()) return;
        try {
            int bodyHeight = (int) bodyHeight();
            int start = 0, end = bodyHeight - 10000;
            do {
                print(String.format("window.scrollTo(%s, -%s);", start, end));
                getJavascriptExecutor().executeScript(String.format("window.scrollTo(%s, -%s);", start, end));
                waitLoadDone();
                int time = Utils.randomInteger(5, 10);
                delaySecond(time);
                start = end;
                int unit = Utils.randomInteger(5000, 15000);
                end = end - unit;
                if (end < 0) {
                    end = 0;
                }
            } while (end == 0);

        } catch (Exception e) {
            Logger.warning(TAG, e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    void scrollToBottomAuto() {
        if (!isValidTaskInRunning()) return;
        try {
            int countPage = 0;
            int start = 0, end = 10000;
            while (countPage < 30) {
                getJavascriptExecutor().executeScript(String.format("window.scrollTo(%s, %s);", start, end));
                waitLoadDone();
                int time = Utils.randomInteger(5, 10);
                delaySecond(time);
                start = end;
                int unit = Utils.randomInteger(5000, 15000);
                end = end + unit;
                countPage++;
            }

        } catch (Exception e) {
            Logger.warning(TAG, e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    long bodyHeight() {
        try {
            return (Long) getJavascriptExecutor().executeScript("return document.body.scrollHeight");
        } catch (Exception e) {
            print("bodyHeight " + e);
            return 0L;
        }

    }


    protected final void clickElement(WebElement element) {
        if (!isValidTaskInRunning()) return;
        if (element != null) {
            waitDefault();
            try {
                if (isElementPresentAndDisplayed(element)) {
                    element.click();
                }
            } catch (WebDriverException e) {
                Logger.warning(TAG, "WebDriverException: " + e.getMessage());
            }
        }
    }

    protected final void moveToElement(WebElement element) {
        if (!isValidTaskInRunning()) return;
        if (element != null) {
            waitDefault();
            try {
                Actions actions = new Actions(getWebDriver());
                actions.moveToElement(element);
                actions.build().perform();
            } catch (WebDriverException e) {
                Logger.warning(TAG, "WebDriverException: " + e.getMessage());
            }
        }
    }

    protected final boolean isElementPresentAndDisplayed(WebElement element) {
        return WebDriverUtils.isElementPresentAndDisplayed(element);
    }

    protected final void openUrl(String url) {
        openUrl(url, 1);
    }

    private void openUrl(String url, int time) {
        if (!isValidTaskInRunning()) return;
        Logger.d(TAG, "openUrl " + url + " - time " + time);
        try {
            getWebDriver().get(url);
        } catch (Exception ex) {
            Logger.error("Open url exception", ex);
            if (ex.getMessage().contains("unexpected alert open")) {
                closeAlert();
                openUrl(url, time + 1);
            } else if (time <= 2) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                openUrl(url, time + 1);
            } else {
                throw ex;
            }
        }
    }

    protected boolean closeAlert() {
        try {
            Alert alert = getWebDriver().switchTo().alert();
            alert.accept();
            return true;
        } catch (Exception ex) {
            Logger.error("#closeAlert confirm Exception: " + ex.getMessage(), ex);
            return false;
        }
    }

    protected final Object executeJavaScript(String script, Object... objects) {
        return getJavascriptExecutor().executeScript(script, objects);
    }

    protected void executeScript(String javaScript) {
        if (webDriver instanceof JavascriptExecutor) {
            ((JavascriptExecutor) webDriver).executeScript(javaScript);
        } else {
            throw new IllegalStateException("This driver does not support JavaScript!");
        }
    }

    protected void executeScript(String javaScript, WebElement element) {
        if (webDriver instanceof JavascriptExecutor) {
            ((JavascriptExecutor) webDriver).executeScript(javaScript, element);
        } else {
            throw new IllegalStateException("This driver does not support JavaScript!");
        }
    }

    protected final <E extends WebElement> E getElementByCondition(ExpectedCondition<E> expectedCondition) {
        if (!isValidTaskInRunning()) return null;
        try {
            return webDriverWait.until(expectedCondition);
        } catch (Exception e) {
            Logger.warning(TAG, e.getClass().getSimpleName() + ": " + e.getMessage());
            return null;
        }
    }

    final <T> T waitUntil(ExpectedCondition<T> expectedCondition) {
        if (!isValidTaskInRunning()) return null;
        try {
            return webDriverWait.until(expectedCondition);
        } catch (Exception e) {
            Logger.warning(TAG, "waitUntil Exception : " + expectedCondition.toString());
            return null;
        }
    }

    protected final WebElement getElementByCssSelector(WebElement parentElement, String selector) {
        if (!isValidTaskInRunning()) return null;
        try {
            return parentElement.findElement(By.cssSelector(selector));
        } catch (Exception e) {
            Logger.warning(TAG, "Not found element : " + selector);
            return null;
        }
    }

    protected final List<WebElement> getElementsByCssSelector(WebElement parentElement, String selector) {
        if (!isValidTaskInRunning()) return null;
        try {
            return parentElement.findElements(By.cssSelector(selector));
        } catch (Exception e) {
            Logger.warning(TAG, "Not found element : " + selector);
            return null;
        }
    }

    protected final List<WebElement> getElementsByCssSelector(String selector) {
        if (!isValidTaskInRunning()) return null;
        try {
            return getWebDriver().findElements(By.cssSelector(selector));
        } catch (Exception e) {
            Logger.warning(TAG, "Not found element : " + selector);
            return null;
        }
    }

    protected final WebElement getElementByCssSelector(String selector) {
        if (!isValidTaskInRunning()) return null;
        try {
            return getWebDriver().findElement(By.cssSelector(selector));
        } catch (Exception e) {
            Logger.warning(TAG, "Not found element : " + selector);
            return null;
        }
    }

    public WebElement getElementByXpath(String xpath) {
        if (!isValidTaskInRunning()) return null;
        try {
            return getWebDriver().findElement(By.xpath(xpath));
        } catch (Exception e) {
            Logger.warning(TAG, "Not found element : " + xpath);
            return null;
        }
    }

    public WebElement getElementByClassName(String className) {
        try {
            return getWebDriver().findElement(By.className(className));
        } catch (Exception e) {
            Logger.warning(TAG, "Not found element : " + className);
            return null;
        }
    }

    public WebElement getElementByClassName(WebElement parentElement, String className) {
        if (!isValidTaskInRunning()) return null;
        try {
            return parentElement.findElement(By.className(className));
        } catch (Exception e) {
            Logger.warning(TAG, "Not found element : " + className);
            return null;
        }
    }

    public List<WebElement> getElementsByClassName(String className) {
        if (!isValidTaskInRunning()) return null;
        try {
            return getWebDriver().findElements(By.className(className));
        } catch (Exception e) {
            Logger.warning(TAG, "Not found element : " + className);
            return null;
        }
    }

    public List<WebElement> getElementsByClassName(WebElement parentElement, String className) {
        if (!isValidTaskInRunning()) {
            print("isValidTaskInRunning FALSE");
            return null;
        }
        try {
            return parentElement.findElements(By.className(className));
        } catch (Exception e) {
            Logger.warning(TAG, "Not found element : " + className);
            return null;
        }
    }

    public WebElement getElementById(String id) {
        try {
            return webDriver.findElement(By.id(id));
        } catch (Exception e) {
            Logger.warning(TAG, "Not found element " + id);//e.getClass().getSimpleName() + ": " + e.getMessage()
            return null;
        }
    }

    protected final WebElement getElementById(WebElement parentElement, String id) {
        if (!isValidTaskInRunning()) return null;
        try {
            return parentElement.findElement(By.id(id));
        } catch (Exception e) {
            Logger.warning(TAG, "Not found element : " + id);
            return null;
        }
    }

    protected final List<WebElement> getElementsById(String id) {
        if (!isValidTaskInRunning()) return null;
        try {
            return getWebDriver().findElements(By.id(id));
        } catch (Exception e) {
            Logger.warning(TAG, "Not found element : " + id);
            return null;
        }
    }

    protected final List<WebElement> getElementsByXpath(String xpath) {
        if (!isValidTaskInRunning()) return null;
        try {
            return getWebDriver().findElements(By.xpath(xpath));
        } catch (Exception e) {
            Logger.warning(TAG, "Not found element : " + xpath);
            return null;
        }
    }

    protected final List<WebElement> getElementsById(WebElement parentElement, String id) {
        if (!isValidTaskInRunning()) return null;
        try {
            return parentElement.findElements(By.id(id));
        } catch (Exception e) {
            Logger.warning(TAG, "Not found element : " + id);
            return null;
        }
    }

    protected final WebElement getElementByName(String name) {
        if (!isValidTaskInRunning()) return null;
        try {
            return getWebDriver().findElement(By.name(name));
        } catch (Exception e) {
            Logger.warning(TAG, "Not found element : " + name);
            return null;
        }
    }

    protected final WebElement getElementByName(WebElement parentElement, String name) {
        if (!isValidTaskInRunning()) return null;
        try {
            return parentElement.findElement(By.name(name));
        } catch (Exception e) {
            Logger.warning(TAG, "Not found element : " + name);
            return null;
        }
    }

    protected final List<WebElement> getElementsByName(String name) {
        if (!isValidTaskInRunning()) return null;
        try {
            return getWebDriver().findElements(By.name(name));
        } catch (Exception e) {
            Logger.warning(TAG, "Not found element : " + name);
            return null;
        }
    }

    protected final List<WebElement> getElementsByName(WebElement parent, String name) {
        if (!isValidTaskInRunning()) return null;
        try {
            return parent.findElements(By.name(name));
        } catch (Exception e) {
            Logger.warning(TAG, "Not found element : " + name);
            return null;
        }
    }

    protected final WebElement getElementByTagName(String name) {
        if (!isValidTaskInRunning()) return null;
        try {
            return getWebDriver().findElement(By.tagName(name));
        } catch (Exception e) {
            Logger.warning(TAG, "Not found element : " + name);
            return null;
        }
    }

    public WebElement getElementByTagName(WebElement parentElement, String name) {
        if (!isValidTaskInRunning()) return null;
        try {
            return parentElement.findElement(By.tagName(name));
        } catch (Exception e) {
            Logger.warning(TAG, "Not found element : " + name);
            return null;
        }
    }

    public List<WebElement> getElementsByTagName(WebElement parentElement, String name) {
        if (!isValidTaskInRunning()) return null;
        try {
            return parentElement.findElements(By.tagName(name));
        } catch (Exception e) {
            Logger.warning(TAG, "Not found element : " + name);
            return null;
        }
    }

    protected final boolean isElementExist(String cssSelector) {
        if (!isValidTaskInRunning()) return false;
        try {
            List<WebElement> elements = getWebDriver().findElements(By.cssSelector(cssSelector));
            return elements != null && !elements.isEmpty();
        } catch (Exception e) {
            Logger.warning(TAG, "Not found element : " + cssSelector);
            return false;
        }
    }

    protected final boolean isElementExist(WebElement parentElement, String cssSelector) {
        if (!isValidTaskInRunning()) return false;
        try {
            List<WebElement> elements = parentElement.findElements(By.cssSelector(cssSelector));
            return elements != null && !elements.isEmpty();
        } catch (Exception e) {
            Logger.warning(TAG, "Not found element : " + cssSelector);
            return false;
        }
    }

    protected final boolean isElementExist(By by) {
        if (!isValidTaskInRunning()) return false;
        try {
            List<WebElement> elements = getWebDriver().findElements(by);
            return elements != null && !elements.isEmpty();
        } catch (Exception e) {
            Logger.warning(TAG, e.getClass().getSimpleName() + ": " + e.getMessage());
            return false;
        }
    }

    protected final boolean isElementExist(WebElement parentElement, By by) {
        if (!isValidTaskInRunning()) return false;
        try {
            List<WebElement> elements = parentElement.findElements(by);
            return elements != null && !elements.isEmpty();
        } catch (Exception e) {
            Logger.warning(TAG, e.getClass().getSimpleName() + ": " + e.getMessage());
            return false;
        }
    }

    final void simulateSendKeys(WebElement element, String text) {
        if (!isValidTaskInRunning()) return;
        if (element != null) {
            element.click();
            wait(500, TimeUnit.MILLISECONDS);
            Actions actions = new Actions(webDriver);
            for (char c : text.toCharArray()) {
                actions.sendKeys(Character.toString(c));
                actions.pause(Utils.getRandomNumber(50, 300));
            }
            actions.build().perform();
        }
    }

    protected final void sendKeys(WebElement element, String text) {
        if (element != null) {
            element.click();
            element.clear();
            element.click();
            element.sendKeys(text);
        }
    }

    protected boolean isAlertPresent() {
        if (!isValidTaskInRunning()) return false;
        try {
            getWebDriver().switchTo().alert();
            Logger.d(TAG, "#isAlertPresent try: " + getWebDriver().switchTo().alert().getText());
            return true;
        } catch (NoAlertPresentException Ex) {
            Logger.d(TAG, "#isAlertPresent catch : no alert present");
            return false;
        }
    }

    private boolean isValidTaskInRunning() {
        return status == RUNNING;
    }

    public void print(String message) {
        updateListView(message);
        Logger.d(getTag(), message);
    }

    public void printGreen(String message) {
        updateListView(message, Color.GREEN);
        Logger.d(getTag(), message);
    }

    public void printE(String message) {
        updateListView(message, Color.RED);
        Logger.error(getTag(), message);
    }

    public void print(String message, Throwable throwable) {
        Logger.d(getTag(), message, throwable);
    }

    public void delay5to10s() {
        try {
            int number = Utils.randomInteger(5, 10);
            Logger.d(getTag(), "Đợi " + number + "s");
            TimeUnit.SECONDS.sleep(number);
        } catch (InterruptedException e) {
            printE("delay5to10s " + e);
            e.printStackTrace();
        }
    }

    public void delaySecond(long time) {
        try {
            TimeUnit.SECONDS.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void delayMilliSecond(long time) {
        try {
            TimeUnit.MILLISECONDS.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void sendAction(Keys key) {
        Actions actionObject = new Actions(webDriver);
        actionObject.sendKeys(key);
        actionObject.perform();
    }

    public void tryClick(WebElement element, int height) {
        boolean clickable;
        do {
            try {
                print("Click " + element.getText());
                element.click();
                clickable = true;
            } catch (Exception ex) {
                clickable = false;
                scrollBy(height);
                delayMilliSecond(500);
            }
        } while (!clickable);
    }
}
