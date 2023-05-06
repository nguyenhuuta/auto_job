package com.autojob.gui.main;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainStage extends Stage {
    public MainStage(Stage stage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/layout_main.fxml"));
            stage.setTitle("Main");
            stage.centerOnScreen();
            stage.setMinWidth(600);
            stage.setMinHeight(600);
            stage.setWidth(601);
            stage.setHeight(601);
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
