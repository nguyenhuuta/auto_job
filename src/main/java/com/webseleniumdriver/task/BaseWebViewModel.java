package com.webseleniumdriver.task;

import com.webseleniumdriver.utils.Logger;
import com.webseleniumdriver.utils.Utils;
import com.webseleniumdriver.utils.WebDriverUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
public abstract class BaseWebViewModel {

    public String TAG = BaseWebViewModel.class.getSimpleName();

    private static final int WEB_DRIVER_WAIT_TIMEOUT = 15;

    private static final int DEFAULT_WAIT_TIME = 500;

    private WebDriver webDriver;

    private final WebDriverWait wait;

    public String getTag() {
        return Thread.currentThread().getId() + "-" + TAG;
    }

    public BaseWebViewModel(WebDriver webDriver) {
        this.webDriver = webDriver;
        TAG = getTag();
        wait = new WebDriverWait(webDriver, WEB_DRIVER_WAIT_TIMEOUT);

    }

    protected WebDriver getWebDriver() {
        return webDriver;
    }

    protected void setWebDriver(WebDriver webDriver) {
        this.webDriver = webDriver;
    }

    protected final JavascriptExecutor getJavascriptExecutor() {
        if (webDriver instanceof JavascriptExecutor) {
            return (JavascriptExecutor) webDriver;
        } else {
            throw new IllegalArgumentException("This web driver does not support javascript");
        }
    }

    protected final void waitDefault() {
        wait(DEFAULT_WAIT_TIME, TimeUnit.MILLISECONDS);
    }

    protected final void wait(long time, TimeUnit timeUnit) {
        if (time > 0) {
            try {
                Thread.sleep(timeUnit.toMillis(time));
            } catch (InterruptedException e) {
                Logger.warning(TAG, "InterruptedException: " + e.getMessage());
            }
        }
    }

    protected final void maximizeWebDriver() {
        try {
            webDriver.manage().window().maximize();
        } catch (Exception ex) {
            Logger.warning(TAG, "#maximizeWebDriver false.");
        }
    }

    protected final void scrollToElement(WebElement element) {
        try {
            getJavascriptExecutor().executeScript("arguments[0].scrollIntoView();", element);
        } catch (Exception e) {
            Logger.warning(TAG, e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    protected final void scrollToTop() {
        try {
            getJavascriptExecutor().executeScript("window.scrollTo(0, 0);");
        } catch (Exception e) {
            Logger.warning(TAG, e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    protected final void scrollToBottom() {
        try {
            getJavascriptExecutor().executeScript("window.scrollTo(0, document.body.scrollHeight);");
        } catch (Exception e) {
            Logger.warning(TAG, e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }


    protected final void clickElement(WebElement element) {
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

    protected final void moveToElement(WebElement element) {
        if (element != null) {
            waitDefault();
            try {
                Actions actions = new Actions(webDriver);
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
        Logger.d(TAG, "openUrl " + url);
        Logger.d(TAG, "openUrl time " + time);
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

    protected final <E extends WebElement> E getElementByCondition(ExpectedCondition<E> expectedCondition) {
        try {
            return wait.until(expectedCondition);
        } catch (Exception e) {
            Logger.warning(TAG, e.getClass().getSimpleName() + ": " + e.getMessage());
            return null;
        }
    }

    protected final <T> T waitUntil(ExpectedCondition<T> expectedCondition) {
        try {
            return wait.until(expectedCondition);
        } catch (Exception e) {
            Logger.warning(TAG, e.getClass().getSimpleName() + ": " + e.getMessage());
            return null;
        }
    }

    protected final WebElement getElementByCssSelector(WebElement parentElement, String selector) {
        try {
            return parentElement.findElement(By.cssSelector(selector));
        } catch (Exception e) {
            Logger.warning(TAG, e.getClass().getSimpleName() + ": " + e.getMessage());
            Logger.warning(TAG, "NoSuchElementException : " + selector);
            return null;
        }
    }

    protected final List<WebElement> getElementsByCssSelector(WebElement parentElement, String selector) {
        try {
            return parentElement.findElements(By.cssSelector(selector));
        } catch (Exception e) {
            Logger.warning(TAG, e.getClass().getSimpleName() + ": " + e.getMessage());
            Logger.warning(TAG, "NoSuchElementException : " + selector);
            return null;
        }
    }

    protected final List<WebElement> getElementsByCssSelector(String selector) {
        try {
            return webDriver.findElements(By.cssSelector(selector));
        } catch (Exception e) {
            Logger.warning(TAG, e.getClass().getSimpleName() + ": " + e.getMessage());
            return null;
        }
    }

    protected final WebElement getElementByCssSelector(String selector) {
        try {
            return webDriver.findElement(By.cssSelector(selector));
        } catch (Exception e) {
            Logger.warning(TAG, e.getClass().getSimpleName() + ": " + e.getMessage());
            Logger.warning(TAG, "NoSuchElementException : " + selector);
            return null;
        }
    }

    protected final WebElement getElementByXpath(String xpath) {
        try {
            return webDriver.findElement(By.xpath(xpath));
        } catch (Exception e) {
            Logger.warning(TAG, "NoSuchElementException : " + xpath);
            return null;
        }
    }

    protected final List<WebElement> getElementsByXpath(String xpath) {
        try {
            return webDriver.findElements(By.xpath(xpath));
        } catch (Exception e) {
            Logger.warning(TAG, "NoSuchElementException : " + xpath);
            return new ArrayList<>();
        }
    }

    protected final WebElement getElementByClassName(String className) {
        try {
            return webDriver.findElement(By.className(className));
        } catch (Exception e) {
            Logger.warning(TAG, "NoSuchElementException : " + className);
            return null;
        }
    }

    protected final WebElement getElementByClassName(WebElement parentElement, String className) {
        try {
            return parentElement.findElement(By.className(className));
        } catch (Exception e) {
            Logger.warning(TAG, "NoSuchElementException : " + className);
            return null;
        }
    }

    protected final List<WebElement> getElementsByClassName(String className) {
        try {
            return webDriver.findElements(By.className(className));
        } catch (Exception e) {
            Logger.d(TAG, e.getClass().getSimpleName() + ": " + e.getMessage());
            return null;
        }
    }

    protected final List<WebElement> getElementsByClassName(WebElement parentElement, String className) {
        try {
            return parentElement.findElements(By.className(className));
        } catch (Exception e) {
            Logger.d(TAG, e.getClass().getSimpleName() + ": " + e.getMessage());
            return null;
        }
    }

    protected final WebElement getElementById(String id) {
        try {
            return webDriver.findElement(By.id(id));
        } catch (Exception e) {
            Logger.warning(TAG, "NoSuchElementException " + id);//e.getClass().getSimpleName() + ": " + e.getMessage()
            return null;
        }
    }

    protected final WebElement getElementById(WebElement parentElement, String id) {
        try {
            return parentElement.findElement(By.id(id));
        } catch (Exception e) {
            Logger.warning(TAG, "NoSuchElementException : " + id);
            return null;
        }
    }

    protected final List<WebElement> getElementsById(String id) {
        try {
            return webDriver.findElements(By.id(id));
        } catch (Exception e) {
            Logger.warning(TAG, "NoSuchElementException : " + id);
            return null;
        }
    }

    protected final List<WebElement> getElementsById(WebElement parentElement, String id) {
        try {
            return parentElement.findElements(By.id(id));
        } catch (Exception e) {
            Logger.warning(TAG, "NoSuchElementException : " + id);
            return null;
        }
    }

    protected final WebElement getElementByName(String name) {
        try {
            return webDriver.findElement(By.name(name));
        } catch (Exception e) {
            Logger.warning(TAG, "NoSuchElementException : " + name);
            return null;
        }
    }

    protected final WebElement getElementByName(WebElement parentElement, String name) {
        try {
            return parentElement.findElement(By.name(name));
        } catch (Exception e) {
            Logger.warning(TAG, "NoSuchElementException : " + name);
            return null;
        }
    }

    protected final List<WebElement> getElementsByName(String name) {
        try {
            return webDriver.findElements(By.name(name));
        } catch (Exception e) {
            Logger.warning(TAG, "NoSuchElementException : " + name);
            return null;
        }
    }

    protected final List<WebElement> getElementsByName(WebElement parent, String name) {
        try {
            return parent.findElements(By.name(name));
        } catch (Exception e) {
            Logger.warning(TAG, "NoSuchElementException : " + name);
            return null;
        }
    }

    protected final WebElement getElementByTagName(String name) {
        try {
            return webDriver.findElement(By.tagName(name));
        } catch (Exception e) {
            Logger.warning(TAG, "NoSuchElementException : " + name);
            return null;
        }
    }

    protected final WebElement getElementByTagName(WebElement parentElement, String name) {
        try {
            return parentElement.findElement(By.tagName(name));
        } catch (Exception e) {
            Logger.warning(TAG, "NoSuchElementException : " + name);
            return null;
        }
    }

    protected final List<WebElement> getElementsByTagName(WebElement parentElement, String name) {
        try {
            return parentElement.findElements(By.tagName(name));
        } catch (Exception e) {
            Logger.warning(TAG, "NoSuchElementException : " + name);
            return null;
        }
    }

    protected final boolean isElementExist(String cssSelector) {
        try {
            List<WebElement> elements = webDriver.findElements(By.cssSelector(cssSelector));
            return elements != null && !elements.isEmpty();
        } catch (Exception e) {
            Logger.warning(TAG, "NoSuchElementException : " + cssSelector);
            return false;
        }
    }

    protected final boolean isElementExist(WebElement parentElement, String cssSelector) {
        try {
            List<WebElement> elements = parentElement.findElements(By.cssSelector(cssSelector));
            return elements != null && !elements.isEmpty();
        } catch (Exception e) {
            Logger.warning(TAG, "NoSuchElementException : " + cssSelector);
            return false;
        }
    }

    protected final boolean isElementExist(By by) {
        try {
            List<WebElement> elements = webDriver.findElements(by);
            return elements != null && !elements.isEmpty();
        } catch (Exception e) {
            Logger.warning(TAG, e.getClass().getSimpleName() + ": " + e.getMessage());
            return false;
        }
    }

    protected final boolean isElementExist(WebElement parentElement, By by) {
        try {
            List<WebElement> elements = parentElement.findElements(by);
            return elements != null && !elements.isEmpty();
        } catch (Exception e) {
            Logger.warning(TAG, e.getClass().getSimpleName() + ": " + e.getMessage());
            return false;
        }
    }

    protected final WebElement waitElementByXpath(String xpath) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
        } catch (Exception ex) {
            Logger.error("waitElementByXpath Exception " + xpath, ex);
        }
        Logger.warning(TAG, "NoSuchElementException : " + xpath);
        return null;
    }

    protected final void simulateSendKeys(WebElement element, String text) {
        executeOnCatchErrorBlock(() -> {
            if (element != null) {
                element.click();
                Actions actions = new Actions(webDriver);
                for (char c : text.toCharArray()) {
                    actions.sendKeys(Character.toString(c));
                    actions.pause(Utils.getRandomNumber(50, 300));
                }
                actions.build().perform();
            }
        });
    }

    protected boolean isAlertPresent() {
        try {
            webDriver.switchTo().alert();
            Logger.d(TAG, "#isAlertPresent try: " + webDriver.switchTo().alert().getText());
            return true;
        } catch (NoAlertPresentException Ex) {
            Logger.d(TAG, "#isAlertPresent catch : no alert present");
            return false;
        }
    }

    public void onStopApp() {
        if (webDriver != null) {
            try {
                //webDriver.manage().deleteAllCookies();
                webDriver.quit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void executeOnCatchErrorBlock(IExecuteOnCatchError executeOnCatchError) {
        try {
            executeOnCatchError.onExecute();
        } catch (Exception ex) {
            Logger.error("Error: ", ex);
            Logger.error("HTML Error", webDriver.getPageSource());
        }
    }

    private interface IExecuteOnCatchError {
        void onExecute();
    }
}
