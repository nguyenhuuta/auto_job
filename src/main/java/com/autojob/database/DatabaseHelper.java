package com.autojob.database;

import com.sun.org.apache.xerces.internal.impl.io.UTF8Reader;
import com.autojob.database.core.CustomStatement;
import com.autojob.utils.Constants;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;

public class DatabaseHelper {

    private static final String TAG = DatabaseHelper.class.getSimpleName();

    public static final String DIRECTORY = "data";

    private static final String DB_NAME = "data.db";

    private static DatabaseHelper instance;

    public static DatabaseHelper getInstance() {
        if (instance == null) {
            instance = new DatabaseHelper();
        }
        return instance;
    }

    private Connection connection;


    private DatabaseHelper() {
        try {
            Class.forName("org.sqlite.JDBC");
            ensureDbDirectoryExist();
            connection = DriverManager.getConnection("jdbc:sqlite:" + DIRECTORY + "/" + DB_NAME);
        } catch (ClassNotFoundException e) {
        } catch (SQLException e) {
        }
    }

    public CustomStatement getReadableDatabase() {
        return new CustomStatement();
    }

    public CustomStatement getWritableDatabase() {
        return new CustomStatement();
    }

    public PreparedStatement createPrepareStatement(String sql) {
        try {
            return connection.prepareStatement(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void ensureDbDirectoryExist() {
        File dbDirectory = new File(DIRECTORY);
        if (!dbDirectory.exists()) {
            dbDirectory.mkdir();
        }
    }

    public void createTable() {
        try {
            Statement statement = connection.createStatement();
            createTablePreference(statement);
        } catch (SQLException e) {
            System.out.println("DatabaseHelper " + e.toString());
        }
    }

//    private void insertTransaction(WaterMazon waterMazon) {
//        if (waterMazon != null && isNotExitsTransaction(waterMazon)) {
//            try {
//
//                String sql = "INSERT INTO " + DataContract.Transaction.TABLE_NAME
//                        + " ("
//                        + DataContract.Transaction.COLUMN_ID_SERVER + ","
//                        + DataContract.Transaction.COLUMN_AMAZON_ORDER_ID + ","
//                        + DataContract.Transaction.COLUMN_AMAZON_ORDER_ITEM_ID + ","
//                        + DataContract.Transaction.COLUMN_ASIN + ","
//                        + DataContract.Transaction.COLUMN_SKU + ","
//                        + DataContract.Transaction.COLUMN_PRODUCT_NAME + ","
//                        + DataContract.Transaction.COLUMN_ORDER_STATUS + ","
//                        + DataContract.Transaction.COLUMN_PRICE_AMAZON + ","
//                        + DataContract.Transaction.COLUMN_SHIPPING_PRICE + ","
//                        + DataContract.Transaction.COLUMN_QUANTITY_ORDER + ","
//                        + DataContract.Transaction.COLUMN_BUYER_NAME + ","
//                        + DataContract.Transaction.COLUMN_SHIPPING_ADDRESS + ","
//                        + DataContract.Transaction.COLUMN_CONDITION_NOTE + ","
//                        + DataContract.Transaction.COLUMN_CURRENT_PRICE_YAHOO + ","
//                        + DataContract.Transaction.COLUMN_PURCHASE_PRICE + ","
//                        + DataContract.Transaction.COLUMN_BUY_NOW_PRICE + ","
//                        + DataContract.Transaction.COLUMN_AUCTION_PROFIT + ","
//                        + DataContract.Transaction.COLUMN_BUY_NOW_PROFIT + ","
//                        + DataContract.Transaction.COLUMN_PROFIT + ","
//                        + DataContract.Transaction.COLUMN_TIME_EXPIRED + ","
//                        + DataContract.Transaction.COLUMN_IMAGE_PRODUCT + ","
//                        + DataContract.Transaction.COLUMN_PURCHASE_DATE_AMAZON + ","
//                        + DataContract.Transaction.COLUMN_DETAIL_YAHOO_PRODUCT + ","
//                        + DataContract.Transaction.COLUMN_SELLER_AUCTION + ","
//                        + DataContract.Transaction.COLUMN_STATUS
//                        + ")"
//                        + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
//
//                PreparedStatement pstmt = connection.prepareStatement(sql);
//                pstmt.setInt(1, waterMazon.getId());
//                pstmt.setString(2, waterMazon.getAmazonOrderId());
//                pstmt.setString(3, waterMazon.getAmazonOrderItemId());
//                pstmt.setString(4, waterMazon.getAsin());
//                pstmt.setString(5, waterMazon.getSellerSKU());
//                pstmt.setString(6, waterMazon.getProductName());
//                pstmt.setString(7, waterMazon.getOrderStatus());
//                pstmt.setInt(8, waterMazon.getPriceAmazon());
//                pstmt.setInt(9, waterMazon.getShippingPrice());
//                pstmt.setInt(10, waterMazon.getQuantityOrder());
//                pstmt.setString(11, waterMazon.getBuyerName());
//                pstmt.setString(12, waterMazon.getJsonAddress());
//                pstmt.setString(13, waterMazon.getConditionNote());
//                pstmt.setInt(14, waterMazon.getCurrentPriceYahoo());
//                pstmt.setInt(15, waterMazon.getPurchasePrice());
//                pstmt.setInt(16, waterMazon.getBuyNowPrice());
//                pstmt.setInt(17, waterMazon.getAuctionProfit());
//                pstmt.setInt(18, waterMazon.getBuyNowProfit());
//                pstmt.setInt(19, waterMazon.getProfit());
//                pstmt.setLong(20, waterMazon.getTimeExpired());
//                pstmt.setString(21, waterMazon.getImageProduct());
//                pstmt.setLong(22, waterMazon.getPurchaseDateAmazon());
//                pstmt.setString(23, waterMazon.getJsonDetailYahoo());
//                pstmt.setString(24, waterMazon.getSellerAuctionId());
//                pstmt.setInt(25, waterMazon.getStatusProduct());
//                pstmt.executeUpdate();
//            } catch (SQLException e) {
//                print("SQLException insertTransaction " + e.toString());
//            }
//        }
//    }
//    private void saveVersionDatabase(String json) {
//        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(DatabaseHelper.DIRECTORY + "/" + DatabaseHelper.DATABASE_VERSION, false), StandardCharsets.UTF_8))) {
//            bw.write(json);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    private void createTablePreference(Statement statement) {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS " + DataContract.Preference.TABLE_NAME
                    + "("
                    + DataContract.Preference.COLUMN_USER_ID + " INTEGER,"
                    + DataContract.Preference.COLUMN_IS_LOGIN + " INTEGER,"
                    + DataContract.Preference.COLUMN_HOST + " VARCHAR,"
                    + DataContract.Preference.COLUMN_TOKEN + " TEXT,"
                    + DataContract.Preference.COLUMN_SETTING + " TEXT,"
                    + DataContract.Preference.COLUMN_POSITION_CATEGORY + " INTEGER "
                    + ")";
            statement.execute(sql);
        } catch (SQLException e) {
        }
    }

}
