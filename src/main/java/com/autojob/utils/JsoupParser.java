package com.autojob.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class JsoupParser {
    public static Document parseByString(String html) {
        return Jsoup.parse(html);
    }

    public static String getValueByClass(String html, String className, String attributeKey) {
        try {
            Document document = Jsoup.parse(html);
            Element elements = document.getElementsByClass(className).get(0);
            return elements.attr(attributeKey);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static String getElementByAttributeValue(Document document, String key, String value, String attributeKey) {
        Elements elementsId = document.getElementsByAttributeValue(key, value);
        if (elementsId != null && !elementsId.isEmpty()) {
            return elementsId.get(0).attr(attributeKey);
        }
        return "";
    }
}
