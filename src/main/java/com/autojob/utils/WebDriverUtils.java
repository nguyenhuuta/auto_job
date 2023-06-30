package com.autojob.utils;

import com.autojob.App;
import com.autojob.model.entities.ChromeSetting;
import org.apache.commons.lang3.SystemUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.html5.LocalStorage;
import org.openqa.selenium.html5.WebStorage;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class WebDriverUtils {

    private static final String TAG = WebDriverUtils.class.getSimpleName();

    private static WebDriverUtils instance;
    private final List<WebDriver> webDriverList;

    public static synchronized WebDriverUtils getInstance() {
        if (instance == null) {
            instance = new WebDriverUtils();
        }
        return instance;
    }

    private WebDriverUtils() {
        webDriverList = new ArrayList<>();
    }

    public static void quitAll() {
        if (instance == null) {
            return;
        }
        for (WebDriver webDriver : instance.webDriverList) {
            try {
                quitBrowser(webDriver);
            } catch (Exception ignored) {
            }
        }
    }

    public static void quitBrowser(WebDriver webDriver) {
        try {
            try {
                webDriver.close();
            } catch (Exception ignored) {
            }
            webDriver.quit();
        } catch (Exception ignored) {
        }
    }

    public WebDriver createWebDriver() {
        ChromeOptions chromeOptions = new ChromeOptions();
        if (!App.debug) {
            chromeOptions.setHeadless(true);
        }
        if (SystemUtils.IS_OS_MAC) {
            String baseLocation = System.getProperty("user.dir") + "/ChromeMac";
            chromeOptions.setBinary(new File(baseLocation + "/Google Chrome.app/Contents/MacOS", "Google Chrome"));
        } else if (SystemUtils.IS_OS_WINDOWS) {
            String baseLocation = System.getProperty("user.dir") + "/ChromeWin";
            chromeOptions.setBinary(new File(baseLocation, "chrome.exe"));
        }
        chromeOptions.addArguments("--disable-dev-shm-usage");
        chromeOptions.addArguments("--no-sandbox");
        WebDriver webDriver = new ChromeDriver(chromeOptions);
        webDriver.manage().window().setSize(new Dimension(Constants.WEB_DRIVER_WINDOW_WIDTH, Constants.WEB_DRIVER_WINDOW_HEIGHT));
        synchronized (webDriverList) {
            webDriverList.add(webDriver);
        }
        return webDriver;
    }

    public WebDriver createWebDriver(boolean visible, int wight, int height) {
        WebDriver webDriver;
        ChromeOptions chromeOptions = new ChromeOptions();
        if (!visible) {
            chromeOptions.setHeadless(true);
        }
        if (SystemUtils.IS_OS_MAC) {
            String baseLocation = System.getProperty("user.dir") + "/ChromeMac";
            chromeOptions.setBinary(new File(baseLocation + "/Google Chrome.app/Contents/MacOS", "Google Chrome"));
        } else if (SystemUtils.IS_OS_WINDOWS) {
            String baseLocation = System.getProperty("user.dir") + "/ChromeWin";
            chromeOptions.setBinary(new File(baseLocation, "chrome.exe"));
        }
        chromeOptions.addArguments("--disable-dev-shm-usage");
        chromeOptions.addArguments("--no-sandbox");
        webDriver = new ChromeDriver(chromeOptions);
        if (wight > 0 && height > 0) {
            webDriver.manage().window().setSize(new Dimension(wight, height));
        }
        synchronized (webDriverList) {
            webDriverList.add(webDriver);
        }
        return webDriver;
    }

    public WebDriver createWebDriver(ChromeSetting chromeSetting) {
        WebDriver webDriver;
        ChromeOptions chromeOptions = new ChromeOptions();
        if (SystemUtils.IS_OS_MAC) {
            String baseLocation = System.getProperty("user.dir") + "/ChromeMac";
            chromeOptions.setBinary(new File(baseLocation + "/Google Chrome.app/Contents/MacOS", "Google Chrome"));
        } else if (SystemUtils.IS_OS_WINDOWS) {
            String baseLocation = System.getProperty("user.dir") + "/ChromeWin";
            chromeOptions.setBinary(new File(baseLocation, "chrome.exe"));
        }

        chromeOptions.addArguments("--disable-notifications");
        if (!chromeSetting.isVisible()) {
            chromeOptions.addArguments("--headless");
        }
        if (chromeSetting.getWight() > 0 && chromeSetting.getHeight() > 0) {
            chromeOptions.addArguments(String.format("--window-size=%s,%s", chromeSetting.getWight(), chromeSetting.getHeight()));
        }
        if (chromeSetting.isMaximized()) {
            chromeOptions.addArguments("--start-maximized");
        }
        if (chromeSetting.getProfilePath() != null) {
            chromeOptions.addArguments("--user-data-dir=" + chromeSetting.getProfilePath());
        }
        chromeOptions.addArguments("--disable-blink-features=AutomationControlled");
        chromeOptions.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
        chromeOptions.setExperimentalOption("useAutomationExtension", false);


        DesiredCapabilities capabilities = DesiredCapabilities.chrome();
        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
//        capabilities.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
        capabilities.setCapability("goog:loggingPrefs", logPrefs);
        capabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
        webDriver = new ChromeDriver(capabilities);

//        webDriver = new ChromeDriver(chromeOptions);
        synchronized (webDriverList) {
            webDriverList.add(webDriver);
        }
        return webDriver;
    }

    public void clearCache() {
        WebDriver webDriver = createWebDriver(true, 450, 450);
        webDriver.get("chrome://settings/clearBrowserData");

        new WebDriverWait(webDriver, 20).until(this::getClearCacheButton);

        getClearCacheButton(webDriver).click();

        new WebDriverWait(webDriver, 20).until(driver -> {
            if (getClearCacheButton(driver) == null) {
                return new Object();
            }
            return null;
        });
        webDriver.close();
    }

    private WebElement getClearCacheButton(WebDriver webDriver) {
        try {
            // begin identify clear data button via nested Shadow Dom elements
            // get 1st parent
            WebElement root1 = webDriver.findElement(By.cssSelector("settings-ui"));
            // get 1st shadowroot element
            WebElement shadowRoot1 = expandRootElement(webDriver, root1);

            // get 2nd parent
            WebElement root2 = shadowRoot1.findElement(By.cssSelector("settings-main"));
            // get 2nd shadowroot element
            WebElement shadowRoot2 = expandRootElement(webDriver, root2);

            // get 3rd parent
            WebElement root3 = shadowRoot2.findElement(By.cssSelector("settings-basic-page"));
            // get 3rd shadowroot element
            WebElement shadowRoot3 = expandRootElement(webDriver, root3);

            // get 4th parent
            WebElement root4 = shadowRoot3.findElement(By.cssSelector("settings-section > settings-privacy-page"));
            // get 4th shadowroot element
            WebElement shadowRoot4 = expandRootElement(webDriver, root4);

            // get 5th parent
            WebElement root5 = shadowRoot4.findElement(By.cssSelector("settings-clear-browsing-data-dialog"));
            // get 5th shadowroot element
            WebElement shadowRoot5 = expandRootElement(webDriver, root5);

            // get 6th parent
            WebElement root6 = shadowRoot5.findElement(By.cssSelector("#clearBrowsingDataDialog"));

            // get button (finally!)
            // end identify clear data button via nested Shadow Dom elements
            return root6.findElement(By.cssSelector("#clearBrowsingDataConfirm"));
        } catch (Exception ex) {
            System.out.println("getClearCacheButton Exception: " + ex.getMessage());
            return null;
        }
    }

    private WebElement expandRootElement(WebDriver driver, WebElement element) {
        WebElement ele = (WebElement) ((JavascriptExecutor) driver)
                .executeScript("return arguments[0].shadowRoot", element);
        return ele;
    }

    public WebDriver reloadWebDriver(WebDriver webDriver) {
        if (webDriver != null) {
            try {
                webDriver.close();
            } catch (Exception ex) {
                Logger.error("Reload web driver error", ex);
            }
        }
        return createWebDriver();
    }

    public WebDriver reloadWebDriver(WebDriver webDriver, boolean visible, int wight, int height) {
        if (webDriver != null) {
            try {
                webDriver.close();
            } catch (Exception ex) {
                Logger.error("Reload web driver error", ex);
            }
        }
        return createWebDriver(visible, wight, height);
    }


    public static boolean isElementPresentAndDisplayed(WebElement element) {
        try {
            if (element != null) {
                return element.isDisplayed();
            }
        } catch (WebDriverException ignored) {
        }
        return false;
    }

    public static void reSaveLocalStorage(WebDriver webDriver, String path) {
        List<ObjectUtils.ValueObject> listLocalStorage = getLocalStorage(webDriver);
        TextUtils.saveLocalStorage(listLocalStorage, path);
    }

    public static List<ObjectUtils.ValueObject> getLocalStorage(WebDriver webDriver) {
        LocalStorage localStorage = ((WebStorage) webDriver).getLocalStorage();
        List<ObjectUtils.ValueObject> listLocalStorage = new ArrayList<>();
        for (String item : localStorage.keySet()) {
            String value = localStorage.getItem(item);
            listLocalStorage.add(new ObjectUtils.ValueObject(item, value));
            Logger.info("#checkLogin localStorage item: " + item + " - " + value);
        }
        return listLocalStorage;
    }

    public static void setLocalStorage(List<ObjectUtils.ValueObject> listLocalStorage, WebDriver webDriver) {
        Logger.info("#setLocalStorage size: " + listLocalStorage.size());
        for (ObjectUtils.ValueObject valueObject : listLocalStorage) {
            ((WebStorage) webDriver).getLocalStorage().setItem(valueObject.getKey(), String.valueOf(valueObject.getValue()));
        }
    }
}
