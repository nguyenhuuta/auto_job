package com.autojob.utils;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.sun.org.apache.xerces.internal.impl.io.UTF8Reader;
import okhttp3.Headers;
import org.openqa.selenium.Cookie;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtils {
    public static boolean isEmpty(String text) {
        return text == null || text.isEmpty();
    }

    public static String readFile(String inputPath, String format) {
        String line;
        String output = "";
        int index = 0;
        try {
            FileInputStream fileReader = new FileInputStream(inputPath);
            InputStreamReader isr = new InputStreamReader(fileReader, Charset.forName(format));// SHIFT-JIS; UTF-8
//            InputStreamReader isr = new InputStreamReader(fileReader);
            BufferedReader bufferedReader =
                    new BufferedReader(isr);
            while ((line = bufferedReader.readLine()) != null) {
                output += (line + "\n");
                index++;
            }
            bufferedReader.close();
        } catch (Exception ex) {
            Logger.error("#readFile Exception: " + inputPath, ex);
        }
        return output;
    }

    public static void mergeSetCookie(Map<String, String> mapCookie, Headers headers) {
        List<String> setCookies = headers.values("Set-Cookie");
        for (String item : setCookies) {
            if (isEmpty(item) || !item.contains("=")) continue;
            item = item.split(";")[0];
            String[] s = item.split("=");
            mapCookie.put(s[0], item.replace(s[0] + "=", ""));
        }
    }

    public static String parserCookie(Map<String, String> mapCookie) {
        try {
            String res = "";
            for (Map.Entry<String, String> entry : mapCookie.entrySet()) {
                res = res + entry.getKey() + "=" + entry.getValue() + "; ";
            }
            return res;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static Set<Cookie> getCookieFromFile(String path) {
        Gson gson = new Gson();
        Set<Cookie> cookies = new HashSet<>();
        BufferedReader bw;
        File file = new File(path);
        if (!file.exists()) return cookies;
        try {
            bw = new BufferedReader(new UTF8Reader(new FileInputStream(file)));
            String cookie = bw.readLine();
            if (cookie != null && !cookie.isEmpty()) {
                cookies = gson.fromJson(cookie, new TypeToken<Set<Cookie>>() {
                }.getType());
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cookies;
    }

    public static void saveCookieToFile(Set<Cookie> cookies, String path) {
        try {
            File file = new File(path);
            file.getParentFile().mkdirs();
            if (!file.exists()) {
                file.createNewFile();
            }
            Gson gson = new Gson();
            String json = gson.toJson(cookies);
            Logger.info("saveCookieToFile: " + path + " - " + json);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path, false), StandardCharsets.UTF_8));
            bw.write(json);
            bw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void writeFile(String input, String path) {
        try {
            File file = new File(path);
            file.getParentFile().mkdirs();
            if (!file.exists()) {
                file.createNewFile();
            }
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), Charset.forName("SHIFT-JIS"));
            writer.write(input);
            writer.close();
        } catch (Exception ex) {
            Logger.error("#writeFile Exception: " + path, ex);
        }
    }

    public static List<ObjectUtils.ValueObject> getLocalStorageFromFile(String path) {
        try {
            String text = TextUtils.readFile(path, "UTF-8");
            return JsonParser.fromStringToArray(text, ObjectUtils.ValueObject.class);
        } catch (Exception ex) {
            Logger.error("#getLocalStorageFromFile Exception: " + path + " - " + ex.getMessage());
            return new ArrayList<>();
        }
    }

    public static void saveLocalStorage(List<ObjectUtils.ValueObject> valueObjectList, String path) {
        try {
            File file = new File(path);
            file.getParentFile().mkdirs();
            if (!file.exists()) {
                file.createNewFile();
            }
            String json = JsonParser.toString(valueObjectList);
            Logger.info("saveLocalStorage: " + path + " - " + json);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path, false), StandardCharsets.UTF_8));
            bw.write(json);
            bw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static String randomString(String input, int length) {
        int n = input.length();
        Random random = new Random();
        String res = "";
        for (int i = 0; i < length; i++) {
            res += (input.charAt(random.nextInt(n)));
        }
        return res;
    }

    public static String randomMercariUUID() {
        String res = "";
        String input = "abcdefghijklmnopqrstuvwxyz1234567890";
        res = res + randomString(input, 8) + "-";
        res = res + randomString(input, 4) + "-";
        res = res + randomString(input, 4) + "-";
        res = res + randomString(input, 4) + "-";
        res = res + randomString(input, 12);
        return res;
    }

    public static String findRegex(String regex, String source) {
        Pattern r = Pattern.compile(regex);
        Matcher m = r.matcher(source);

        if (m.find()) {
            return m.group(1);
        }
        return "";
    }

    public static String decodeUrl(String input) {
        try {
            return URLDecoder.decode(input, "UTF-8");
        } catch (Exception ex) {
            Logger.error("#decodeUrl Exception: " + input + " - " + ex.getMessage());
            return null;
        }
    }
}
