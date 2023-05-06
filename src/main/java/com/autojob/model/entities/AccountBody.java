package com.autojob.model.entities;

/**
 * Created by OpenYourEyes on 25/04/2023
 */
public class AccountBody {
    String orderId;
    String rowId;

    public AccountBody(String orderId, String rowId) {
        this.orderId = orderId;
        this.rowId = rowId;
    }
}
