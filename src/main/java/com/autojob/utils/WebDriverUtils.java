package com.autojob.utils;

import com.autojob.model.entities.ChromeSetting;
import org.apache.commons.lang3.SystemUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.html5.LocalStorage;
import org.openqa.selenium.html5.WebStorage;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class WebDriverUtils {


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


//        DesiredCapabilities capabilities = DesiredCapabilities.chrome();
//        LoggingPreferences logPrefs = new LoggingPreferences();
//        logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
////        capabilities.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
//        capabilities.setCapability("goog:loggingPrefs", logPrefs);
//        capabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
//        webDriver = new ChromeDriver(capabilities);

        webDriver = new ChromeDriver(chromeOptions);
        synchronized (webDriverList) {
            webDriverList.add(webDriver);
        }
        return webDriver;
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

}
