package com.webseleniumdriver.gui.main;

import com.jfoenix.controls.JFXButton;
import com.webseleniumdriver.base.BaseController;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController extends BaseController implements Initializable {
    public ListView listViewAccount;
    public JFXButton btnLogout;
    public JFXButton btnReload;
    public Label lbVersion;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }



}
