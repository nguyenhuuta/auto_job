package com.webseleniumdriver;

import javafx.application.Application;
import javafx.stage.Stage;
import org.openqa.selenium.WebDriver;

import java.util.Map;

public class MainTest extends Application {
    private transient WebDriver webDriverFuriru;
    private static Map<String, Object> myBundleObject;
    private static MainTest instance;

    public static MainTest getInstance() {
        return instance;
    }

    public static void setInstance(MainTest instance) {
        MainTest.instance = instance;
    }

    public static Map<String, Object> getMyBundleObject() {
        return myBundleObject;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {


    }

}
