package com.autojob.api;

import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
public final class RetrofitClient {

    private final Map<String, Retrofit> clients;

    public static RetrofitClient getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private RetrofitClient() {
        clients = new HashMap<>();
    }

    private Retrofit getClient(String baseUrl, boolean followRedirect) {
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .followRedirects(followRedirect)
                .followSslRedirects(followRedirect)
                .connectTimeout(600, TimeUnit.SECONDS)
                .readTimeout(600, TimeUnit.SECONDS)
                .writeTimeout(600, TimeUnit.SECONDS)
                .build();
        Retrofit retrofit = clients.get(baseUrl + followRedirect);
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            clients.put(baseUrl, retrofit);
        }
        return retrofit;
    }

    private Retrofit getClient(String baseUrl, String proxyS) {
        // ip:port:username:password
        String[] proxySplit = proxyS.split(":");
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxySplit[0], Integer.parseInt(proxySplit[1])));
        Authenticator authenticator = (route, response) -> {
            String credential = Credentials.basic(proxySplit[2], proxySplit[3]);
            return response.request().newBuilder()
                    .header("Proxy-Authorization", credential)
                    .build();
        };
        OkHttpClient proxyOkHttpClient = new OkHttpClient.Builder().followSslRedirects(false)
                .followRedirects(false).proxy(proxy).proxyAuthenticator(authenticator)
                .connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .build();
        Retrofit retrofit = clients.get(baseUrl + proxyS);
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(proxyOkHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            clients.put(baseUrl + proxyS, retrofit);
        }
        return retrofit;
    }

    public <T> T getService(String baseUrl, Class<T> service) {
        Retrofit retrofit = getClient(baseUrl, true);
        return retrofit.create(service);
    }

    public <T> T getService(String baseUrl, Class<T> service, boolean followRedirect) {
        Retrofit retrofit = getClient(baseUrl, followRedirect);
        return retrofit.create(service);
    }

    public <T> T getService(String baseUrl, String proxy, Class<T> service) {
        Retrofit retrofit = getClient(baseUrl, proxy);
        return retrofit.create(service);
    }

    private static final class InstanceHolder {

        private static final RetrofitClient INSTANCE = new RetrofitClient();

        private InstanceHolder() {
            //no instance
        }
    }
}
