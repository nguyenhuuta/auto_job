package com.autojob.database;

import com.autojob.database.core.Cursor;
import com.autojob.database.core.CustomStatement;
import com.autojob.model.entities.AccountModel;
import javafx.util.Pair;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
            createTableAccount(statement);
        } catch (SQLException e) {
            System.out.println("DatabaseHelper " + e.toString());
        }
    }

    public void insertAccount(AccountModel accountModel) {
        try {
            String sql = "INSERT INTO " + DataContract.Account.TABLE_NAME
                    + " ("
                    + DataContract.Account.shopName + ","
                    + DataContract.Account.shopId + ","
                    + DataContract.Account.type
                    + ")"
                    + " VALUES (?,?,?)";

            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, accountModel.shopName);
            pstmt.setInt(2, accountModel.shopId);
            pstmt.setInt(3, accountModel.type);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("SQLException insertTransaction " + e.toString());
        }
    }

    public void updateOrderSendVoucherAccount(AccountModel account) {
        try {
            String sql = "UPDATE %s SET %s = %s WHERE %s = %s";
            String fullSQL = String.format(sql, DataContract.Account.TABLE_NAME, DataContract.Account.orderSendVoucher, account.currentOrderId, DataContract.Account.shopName, "'" + account.shopName + "'");
            connection.createStatement().execute(fullSQL);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Pair<Integer, String>> getOrderSendVoucher() {
        try {
            String query = "SELECT * FROM " + DataContract.Account.TABLE_NAME;
            PreparedStatement preparedStatement = createPrepareStatement(query);
            Cursor cursor = new Cursor(preparedStatement.executeQuery());
            List<Pair<Integer, String>> list = new ArrayList<>();
            if (cursor.moveToFirst()) {
                do {
                    String orderId = cursor.getStrings(DataContract.Account.orderSendVoucher);
                    if (orderId != null && !orderId.isEmpty()) {
                        int shopId = cursor.getInts(DataContract.Account.shopId);
                        Pair<Integer, String> pair = new Pair<>(shopId, orderId);
                        list.add(pair);
                    }
                } while (cursor.moveToNext());
            } else {
                cursor.onClose();
            }
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void createTableAccount(Statement statement) {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS " + DataContract.Account.TABLE_NAME
                    + "("
                    + DataContract.Account.shopName + " VARCHAR PRIMARY KEY,"
                    + DataContract.Account.shopId + " INTEGER,"
                    + DataContract.Account.type + " INTEGER,"
                    + DataContract.Account.orderSendVoucher + " VARCHAR NULL"
                    + ")";
            statement.execute(sql);
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

}
