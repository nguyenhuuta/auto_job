package com.autojob.database;

import com.autojob.database.core.Cursor;
import com.autojob.database.core.CustomStatement;
import com.autojob.model.entities.AccountModel;

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
            createTableAccount(statement);
        } catch (SQLException e) {
            System.out.println("DatabaseHelper " + e.toString());
        }
    }

    public void insertAccount(AccountModel accountModel) {
        try {

            String sql = "INSERT INTO " + DataContract.Account.TABLE_NAME
                    + " ("
                    + DataContract.Account.shopId + ","
                    + DataContract.Account.shopName + ","
                    + DataContract.Account.type + ","
                    + DataContract.Account.lastOrderId + ","
                    + DataContract.Account.rowId
                    + ")"
                    + " VALUES (?,?,?,?,?)";

            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, accountModel.shopId);
            pstmt.setString(2, accountModel.shopName);
            pstmt.setInt(3, accountModel.type);
            pstmt.setString(4, accountModel.lastOrderId);
            pstmt.setString(5, accountModel.rowId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("SQLException insertTransaction " + e.toString());
        }
    }

    public void getAccount() {

        try {
            String query = "SELECT * FROM " + DataContract.Account.TABLE_NAME;
            PreparedStatement preparedStatement = DatabaseHelper.getInstance().createPrepareStatement(query);
            Cursor cursor = new Cursor(preparedStatement.executeQuery());
            if (cursor.moveToFirst()) {
                do {
//                    AmazonProduct amazonProduct = new AmazonProduct();
//                    AccountModel accountModel = new AccountModel();
//                    amazonProduct.columnId = cursor.getInts(DataContract.Account.COLUMN_ID);
//                    amazonProduct.setAsinId(cursor.getStrings(DataContract.AmazonProductLocal.COLUMN_ASIN));
//
//                    listResult.add(AmazonProduct.fromCursor(cursor));
                } while (cursor.moveToNext());
            } else {
                cursor.onClose();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void createTableAccount(Statement statement) {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS " + DataContract.Account.TABLE_NAME
                    + "("
                    + DataContract.Account.id + " INTEGER PRIMARY KEY AUTO_INCREMENT,"
                    + DataContract.Account.shopId + " INTEGER,"
                    + DataContract.Account.shopName + " VARCHAR,"
                    + DataContract.Account.type + " INTEGER,"
                    + DataContract.Account.lastOrderId + " VARCHAR,"
                    + DataContract.Account.rowId + " VARCHAR"
                    + ")";
            statement.execute(sql);
        } catch (SQLException e) {
        }
    }

}
