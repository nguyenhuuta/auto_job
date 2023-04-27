package com.webseleniumdriver.utils;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class ObjectUtils {
    public static class CalculatorPriceObject {
        private boolean isOk;
        private int startPrice;
        private int bidorPrice;

        public CalculatorPriceObject(boolean isOk, int startPrice, int bidorPrice) {
            this.isOk = isOk;
            this.startPrice = startPrice;
            this.bidorPrice = bidorPrice;
        }

        public CalculatorPriceObject() {
            this.isOk = true;
            this.startPrice = 0;
            this.bidorPrice = 0;
        }

        public boolean isOk() {
            return isOk;
        }

        public void setOk(boolean ok) {
            isOk = ok;
        }

        public int getStartPrice() {
            return startPrice;
        }

        public void setStartPrice(int startPrice) {
            this.startPrice = startPrice;
        }

        public int getBidorPrice() {
            return bidorPrice;
        }

        public void setBidorPrice(int bidorPrice) {
            this.bidorPrice = bidorPrice;
        }
    }

    public static class CountObject {
        private int count;

        public CountObject() {
            count = 0;
        }

        public void addCount() {
            count++;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }

    public static class ReturnObject {
        private Object object;

        public ReturnObject() {
        }

        public Object getObject() {
            return object;
        }

        public void setObject(Object object) {
            this.object = object;
        }
    }

    public static class BooleanObject {
        private boolean object;

        public BooleanObject(boolean object) {
            this.object = object;
        }

        public boolean getObject() {
            return object;
        }

        public void setObject(boolean object) {
            this.object = object;
        }
    }

    public static class ErrorObject {
        private boolean isError;
        private String message;

        public ErrorObject() {
            isError = false;
            message = "";
        }

        public boolean isError() {
            return isError;
        }

        public void setError(boolean error, String message) {
            isError = error;
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }


    public static class ImageAmazonJson {
        private List<Item> itemList;

        public List<Item> getItemList() {
            return itemList == null ? new ArrayList<>() : itemList;
        }

        public static class Item {
            @SerializedName("url")
            private String url;
            @SerializedName("width")
            private String width;
            @SerializedName("height")
            private String height;

            public String getUrl() {
                return url;
            }

            public int getWidth() {
                return Integer.parseInt(width);
            }

            public int getHeight() {
                return Integer.parseInt(height);
            }
        }
    }

    public static class AmazonShipPriceList {
        private int priceNew;
        private int usedLikeNew;
        private int usedVeryGood;
        private int usedGood;
        private int usedAcceptable;

        public AmazonShipPriceList() {
        }

        public void setPriceNew(int priceNew) {
            this.priceNew = priceNew;
        }

        public void setUsedLikeNew(int usedLikeNew) {
            this.usedLikeNew = usedLikeNew;
        }

        public void setUsedVeryGood(int usedVeryGood) {
            this.usedVeryGood = usedVeryGood;
        }

        public void setUsedGood(int usedGood) {
            this.usedGood = usedGood;
        }

        public void setUsedAcceptable(int usedAcceptable) {
            this.usedAcceptable = usedAcceptable;
        }

        public void setPrice(String key, int price) {
            switch (key.toLowerCase()) {
                case "new":
                    if (priceNew == 0 || priceNew > price) priceNew = price;
                    break;
                case "acceptable":
                    if (usedAcceptable == 0 || usedAcceptable > price) usedAcceptable = price;
                    break;
                case "good":
                    if (usedGood == 0 || usedGood > price) usedGood = price;
                    break;
                case "verygood":
                    if (usedVeryGood == 0 || usedVeryGood > price) usedVeryGood = price;
                    break;
                case "mint":
                    if (usedLikeNew == 0 || usedLikeNew > price) usedLikeNew = price;
                    break;
            }
        }
    }

    public static class ValueObject {
        private String key;
        private Object value;

        public ValueObject(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }
    }
}
