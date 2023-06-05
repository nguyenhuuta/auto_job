package com.autojob;

import com.autojob.utils.Logger;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class ScreenshotFullModel {

    private static final String TAG = ScreenshotFullModel.class.getSimpleName();

    public static String screenShotFull(WebDriver driver, String nameFile) {
        try {
            String pathSaveImage = System.getProperty("user.dir") + "/data/capture/" + nameFile + "_" + System.currentTimeMillis() + ".jpg";
            driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
            TakesScreenshot s = (TakesScreenshot) driver;
            File source = s.getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(source, new File(pathSaveImage));
            Logger.info(TAG, "#screenShotFull : " + pathSaveImage);
            return pathSaveImage;
        } catch (Exception e) {
            Logger.error(TAG, "#screenShotFull : " + nameFile + "ERROR: " + e.getMessage());
        }
        Logger.info(TAG, "#screenShotFull : " + driver.getCurrentUrl());
        return "";
    }
}
