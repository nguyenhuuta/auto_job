package com.autojob.model.entities;


import javafx.scene.paint.Color;

/**
 * Created by OpenYourEyes on 25/04/2023
 */
public class MessageListView {
    public String message;
    public Color color;
    public String bgColor;

    public MessageListView(String message) {
        this(message,null);
    }

    public MessageListView(String message, Color color) {
        this(message,color,null);
    }
    public MessageListView(String message, Color color, String bgColor) {
        this.message = message;
        if (color == null) {
            this.color = Color.BLACK;
        } else {
            this.color = color;
        }
        this.bgColor = bgColor;
//        if(bgColor == null){
//            this.bgColor = "white";
//        }else{
//            this.bgColor = bgColor;
//        }
    }



    @Override
    public String toString() {
        return message;
    }
}
