package com.autojob.task;

import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Created by OpenYourEyes on 05/06/2023
 */
public interface ILoadDonePage {
    WebElement loadDone();

    default List<WebElement> listElement() {
        return null;
    }

}
