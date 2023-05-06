package com.autojob;

import javafx.application.Application;
import javafx.stage.Stage;
import org.openqa.selenium.WebDriver;

import java.util.Map;

public class MainTest extends Application {
    private static MainTest instance;

    public static MainTest getInstance() {
        return instance;
    }

    public static void setInstance(MainTest instance) {
        MainTest.instance = instance;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

    }

}
