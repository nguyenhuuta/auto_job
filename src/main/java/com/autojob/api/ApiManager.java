package com.autojob.api;

import com.autojob.model.entities.AccountBody;
import com.autojob.model.entities.AccountModel;
import com.autojob.model.entities.BaseResponse;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

/**
 * Created by OpenYourEyes on 25/04/2023
 */
public interface ApiManager {

    ApiManager GOOGLE_ENDPOINT = RetrofitClient.getInstance().getService("https://script.google.com/", ApiManager.class);
    static String URL_GOOGLE_SHEET = "macros/s/AKfycbzK8JugCcLRPwXWIevf_oVplm9UjAP6Ix99d-tOw2eld5xiwTr2cjn77Cj5rR72LRN1zg/exec";

    @GET
    @Headers("Accept: application/json")
    Call<BaseResponse<List<AccountModel>>> getAccountShopee(@Url String url);

    @POST
    @Headers("Accept: application/json")
    Call<BaseResponse<String>> updateAccountShopee(@Url String url, @Body AccountBody body);
}