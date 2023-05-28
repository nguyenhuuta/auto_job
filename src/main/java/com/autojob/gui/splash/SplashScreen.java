package com.autojob.gui.splash;

import com.autojob.App;
import com.autojob.HomeStage;
import com.autojob.utils.CheckChrome;
import com.autojob.utils.Logger;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;


public class SplashScreen extends BorderPane {

    VBox body;
    ProgressIndicator progressIndicator;

    public SplashScreen() {
        prefHeight(400);
        prefWidth(400);
        Text button = new Text("Tải Chrome...");
        body = new VBox();
        progressIndicator = new ProgressIndicator();

        progressIndicator.setMinWidth(50);
        progressIndicator.setMinHeight(50);
        progressIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);

        body.setPadding(new Insets(80));
        body.setAlignment(Pos.CENTER);
        body.setSpacing(20);
        body.getChildren().addAll(progressIndicator, button);
        setCenter(body);

        CheckChrome.startCheckChrome(() -> {
            Logger.info("startCheckChrome -> DONE");
            Platform.runLater(() -> {
                new HomeStage(App.getInstance().getStage());
            });
        });
    }
}
