package com.autojob;

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
            Scene scene = new Scene(root);
            scene.getRoot().setStyle("-fx-font-family: 'serif'");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
