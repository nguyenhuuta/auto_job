package com.webseleniumdriver.model.entities;

/**
 * Created by OpenYourEyes on 25/04/2023
 */
public class MessageListView {
    public String message;
    public String color;

    public MessageListView(String message, String color) {
        this.message = message;
        this.color = color;
    }

    public MessageListView(String message) {
        this.message = message;
        this.color = "BLACK";
    }

    @Override
    public String toString() {
        return message;
    }
}
