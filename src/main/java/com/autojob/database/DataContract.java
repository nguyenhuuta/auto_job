package com.autojob.database;

public class DataContract {
    public static class AmazonProductLocal {
        public static final String TABLE_NAME = "AmazonProductLocal";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_ASIN = "asin";
        public static final String COLUMN_PRODUCT_ID = "productId";
        public static final String COLUMN_AMAZON_PRODUCT_NAME = "productName";
        public static final String COLUMN_ROOT_CATEGORY = "rootCategoryId";
        public static final String COLUMN_PRICE_AMAZON = "priceAmazon";
        public static final String COLUMN_USED_PRICE = "usedPrice";
        public static final String COLUMN_SEARCH_PRICE = "searchPrice";
        public static final String COLUMN_LIST_IMAGE = "listImage";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_IMAGE_THUMBNAIL = "imageAmazon";
        public static final String COLUMN_MPN = "mpn";
        public static final String COLUMN_SKU = "SKU";
        public static final String COLUMN_AUCTION_ID = "yahooAuctionId";
        public static final String COLUMN_YAHOO_PRODUCT_NAME = "yahooProductName";
        public static final String COLUMN_START_PRICE = "yahooStartPrice";
        public static final String COLUMN_BIDOR_PRICE = "yahooBidorPrice";
        public static final String COLUMN_DIFF_PRICE = "yahooAuctionDifPrice";
        public static final String COLUMN_MAIN_PICTURE = "yahooMainPicture";
        public static final String COLUMN_YAHOO_SYNC_STATUS = "yahooSyncStatus";
        public static final String COLUMN_END_TIME_YAHOO = "endTimeYahoo";
        public static final String COLUMN_LAST_TIME_MAPPING = "lastTimeMapping";
        public static final String COLUMN_SELLER = "sellerId";
    }


    static class Preference {

        public static final String TABLE_NAME = "Preference";

        public static final String COLUMN_USER_ID = "id";

        public static final String COLUMN_IS_LOGIN = "IsLogin";

        public static final String COLUMN_HOST = "host";

        public static final String COLUMN_TOKEN = "token";

        public static final String COLUMN_SETTING = "setting";

        public static final String COLUMN_POSITION_CATEGORY = "position_category";

        public static final String COLUMN_TEST = "test";

        private Preference() {
            //no instance
        }
    }


    static class Transaction {
        public static final String TABLE_NAME = "transactions";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_ID_SERVER = "id_server";
        public static final String COLUMN_AMAZON_ORDER_ID = "amazon_order_id";
        public static final String COLUMN_AMAZON_ORDER_ITEM_ID = "amazon_order_item_id";
        public static final String COLUMN_ASIN = "asin";
        public static final String COLUMN_SKU = "sku";
        public static final String COLUMN_PRODUCT_NAME = "product_name";
        public static final String COLUMN_ORDER_STATUS = "order_status";
        public static final String COLUMN_PRICE_AMAZON = "price_amazon";
        public static final String COLUMN_SHIPPING_PRICE = "shipping_price";
        public static final String COLUMN_QUANTITY_ORDER = "quantity_order";
        public static final String COLUMN_BUYER_NAME = "buyer_name";
        public static final String COLUMN_SHIPPING_ADDRESS = "shipping_address";
        public static final String COLUMN_CONDITION_NOTE = "condition_note";
        public static final String COLUMN_CURRENT_PRICE_YAHOO = "current_price_yahoo";
        public static final String COLUMN_PURCHASE_PRICE = "purchase_price";
        public static final String COLUMN_BUY_NOW_PRICE = "buy_now_price";
        public static final String COLUMN_AUCTION_PROFIT = "auction_profit";
        public static final String COLUMN_BUY_NOW_PROFIT = "buy_now_profit";
        public static final String COLUMN_PROFIT = "profit";
        public static final String COLUMN_TIME_EXPIRED = "expired";
        public static final String COLUMN_STATUS = "status";
        public static final String COLUMN_SELLER_AUCTION = "seller_auction";
        public static final String COLUMN_DETAIL_YAHOO_PRODUCT = "detail_yahoo";
        public static final String COLUMN_IMAGE_PRODUCT = "image_product";
        public static final String COLUMN_PURCHASE_DATE_AMAZON = "purchase_date_amazon";
    }

    /* favorite product*/
    static class Product {

        public static final String TABLE_NAME = "AmazonProduct";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_ASIN = "asin";
        public static final String COLUMN_LIKE = "like";
        //public static final String COLUMN_SELLER = "seller";
    }

    static class MessagePattern {
        public static final String TABLE_NAME = "MessagePattern";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_CONTENT = "content";
    }

    static class NeedMappingProduct {
        public static final String TABLE_NAME = "NeedMappingProduct";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_AUCTION_ID = "auction_id";
        public static final String COLUMN_ASIN_ID = "asin_id";
        public static final String COLUMN_SKU = "sku";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_IMAGE = "image";
        public static final String COLUMN_STATUS = "status";
        public static final String COLUMN_YAHOO_PRODUCT = "yahoo_product";
        public static final String COLUMN_PRODUCT_ID = "product_id";
    }

    private DataContract() {
        //no instance
    }
}
