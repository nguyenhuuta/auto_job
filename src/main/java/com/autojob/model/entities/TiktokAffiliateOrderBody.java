package com.autojob.model.entities;

/**
 * Created by OpenYourEyes on 28/06/2023
 */
public class TiktokAffiliateOrderBody {
    public String orderId;
    public String affiliateId;
    public Integer commissionDiscount;
    /**
     * -1, 0: đơn tự nhiên
     * 1: livestream
     * 2: video
     */
    public Integer sourceOrder;

    @Override
    public String toString() {
        return "TiktokAffiliateOrderBody{" +
                "orderId='" + orderId + '\'' +
                ", affiliateId='" + affiliateId + '\'' +
                ", commissionDiscount=" + commissionDiscount +
                ", sourceOrder=" + sourceOrder +
                '}';
    }
}
