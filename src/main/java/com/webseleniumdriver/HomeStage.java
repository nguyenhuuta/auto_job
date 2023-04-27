package com.webseleniumdriver;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Created by OpenYourEyes on 25/04/2023
 */
public class HomeStage extends Stage {
    public HomeStage(Stage stage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/home.fxml"));
            stage.setTitle("Home");
            stage.centerOnScreen();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
