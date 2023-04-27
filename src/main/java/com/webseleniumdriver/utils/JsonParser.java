package com.webseleniumdriver.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;

public class JsonParser {
    private static final Gson gson = new Gson();

    private JsonParser() {
    }

    public static <T> T fromString(String json, Class<T> tClass) {
        try {
            return gson.fromJson(json, tClass);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static <T> List<T> fromStringToArray(String json, Class<T> tClass) {
        JsonArray jsonArray = fromString(json, JsonArray.class);
        List<T> list = new ArrayList<>();
        for (JsonElement element : jsonArray) {
            list.add(gson.fromJson(element, tClass));
        }
        return list;
    }

    public static <T> String toString(T object) {
        return gson.toJson(object);
    }

}
