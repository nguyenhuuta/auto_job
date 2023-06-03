package com.autojob.api;

import com.autojob.model.entities.AccountBody;
import com.autojob.model.entities.AccountModel;
import com.autojob.model.entities.BaseResponse;
import com.autojob.model.entities.TiktokOrderRateBody;
import org.json.JSONArray;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

/**
 * Created by OpenYourEyes on 25/04/2023
 */
public interface ApiManager {

    ApiManager GOOGLE_ENDPOINT = RetrofitClient.getInstance().getService("https://script.google.com/", ApiManager.class);

    ApiManager BICA_ENDPOINT = RetrofitClient.getInstance().getService("https://api.4mencomestic.com/", ApiManager.class);
    String URL_GOOGLE_SHEET = "macros/s/AKfycbzK8JugCcLRPwXWIevf_oVplm9UjAP6Ix99d-tOw2eld5xiwTr2cjn77Cj5rR72LRN1zg/exec";

    @GET
    @Headers("Accept: application/json")
    Call<BaseResponse<List<AccountModel>>> getAccountShopee(@Url String url);

    @POST
    @Headers("Accept: application/json")
    Call<BaseResponse<String>> updateAccountShopee(@Url String url, @Body AccountBody body);


    //type = 1 lấy sđt
    //type = 2 chưa rate
    @GET("tiktok/orderNeedBuyerPhone")
    @Headers("Accept: application/json")
    Call<BaseResponse<List<String>>> orderNeedBuyerPhone(@Query("tiktokKeyId") int tiktokKeyId, @Query("type") int type);

    @POST("tiktok/updateBuyer")
    @Headers("Accept: application/json")
    Call<BaseResponse<Object>> updateBuyer(@Body() List<TiktokOrderRateBody> body);
}
