package com.webseleniumdriver.model.entities;

/**
 * Created by OpenYourEyes on 25/04/2023
 */
public class BaseResponse<T> {
    public boolean isSuccess;
    public T data;

    public T getData() {
        return data;
    }
}
