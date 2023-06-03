package com.autojob.model.entities;

/**
 * Created by OpenYourEyes on 01/06/2023
 */
public class TiktokOrderRateBody {
    public String orderId;
    public String buyerPhone;
    public Integer rate;
    public Boolean sendThanks;

    @Override
    public String toString() {
        return "TiktokOrderRateBody{" +
                "orderId='" + orderId + '\'' +
                ", buyerPhone='" + buyerPhone + '\'' +
                ", rate=" + rate +
                ", sendThanks=" + sendThanks +
                '}';
    }
}