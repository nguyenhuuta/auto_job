package com.autojob.gui.splash;

import com.autojob.App;
import com.autojob.HomeStage;
import com.autojob.utils.CheckChrome;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;


public class SplashScreen extends BorderPane {

    VBox maincontent;
    ProgressIndicator progressIndicator;

    public SplashScreen() {
        Text button = new Text("Tải Chrome...");
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

        CheckChrome.startCheckChrome(() -> {
            Platform.runLater(() -> {
                new HomeStage(App.getInstance().getStage());
            });
        });
    }
}
