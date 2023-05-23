package com.autojob.model.entities;


import javafx.scene.paint.Color;

/**
 * Created by OpenYourEyes on 25/04/2023
 */
public class MessageListView {
    public String message;
    public Color color;

    public MessageListView(String message, Color color) {
        this(message);
        this.message = message;
        if (color == null) {
            this.color = Color.BLACK;
        } else {
            this.color = color;
        }

    }

    public MessageListView(String message) {
        this.message = message;
        this.color = Color.BLACK;
    }

    @Override
    public String toString() {
        return message;
    }
}
