package com.webseleniumdriver.gui.splash;

import com.jfoenix.controls.JFXButton;
import com.webseleniumdriver.App;
import com.webseleniumdriver.model.entities.ChromeSetting;
import com.webseleniumdriver.shopee.ShopeeController;
import com.webseleniumdriver.tiktok.TiktokController;
import com.webseleniumdriver.utils.WebDriverUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.openqa.selenium.WebDriver;


public class SplashScreen extends BorderPane {

    VBox maincontent;
    ProgressIndicator progressIndicator;

    public SplashScreen() {
        JFXButton button = new JFXButton("Next");
        maincontent = new VBox();
        progressIndicator = new ProgressIndicator();

        progressIndicator.setMinWidth(50);
        progressIndicator.setMinHeight(50);
        progressIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);

        maincontent.setPadding(new Insets(80));
        maincontent.setAlignment(Pos.CENTER);
        maincontent.setSpacing(20);
        maincontent.getChildren().addAll(progressIndicator, button);
        setCenter(maincontent);

    }
}
