package com.autojob.model.entities;

import sun.util.resources.LocaleData;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by OpenYourEyes on 25/04/2023
 */
public class AccountModel {
    public String shopName;
    // 1: Shopee, 2: TiktokShop
    public int type;
    public int shopId;

    public String contentVoucher;

    public String sellerSKU;

    public String currentOrderId;

    public Date expired;


    public String buttonName() {
        if (expired == null) {
            return shopName;
        }
        Date start = new Date();
        long time = expired.getTime() - start.getTime();
        long second = TimeUnit.MILLISECONDS.toSeconds(time) % 60;
        long minute = TimeUnit.MILLISECONDS.toMinutes(time) % 60;
        long hour = TimeUnit.MILLISECONDS.toHours(time) % 24;
        long day = TimeUnit.MILLISECONDS.toDays(time) % 365;
        String timeExpired = "%s còn %s ngày %s giờ %s phút %s giây";
        return String.format(timeExpired, shopName, day, hour, minute, second);

    }
}
