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


    public static final String FILE_ACCOUNT = "account.txt";

    public static final String SHIPPING_TEMPLATE = "shipping_template.txt";

    public static final String DATABASE_VERSION = "database_version.txt";

    public static final String PRODUCT_ERROR = "product_error.txt";

    public static final String PRODUCT_AMAZON_UPLOAD = "product_amazon_upload.txt";

    public static final String PRODUCT_AMAZON_SHIP = "product_amazon_ship.txt";

    public static final String YAHOO_SEARCH_SUGGEST = "search_suggest.txt";

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
        File file = new File(DIRECTORY + "/" + FILE_ACCOUNT);
        File fileShippingTemplate = new File(DIRECTORY + "/" + SHIPPING_TEMPLATE);
        File fileVersionDatabase = new File(DIRECTORY + "/" + DATABASE_VERSION);
        File fileProductError = new File(DIRECTORY + "/" + PRODUCT_ERROR);
        File fileProductAmazonUpload = new File(DIRECTORY + "/" + PRODUCT_AMAZON_UPLOAD);
        File fileProductAmazonShip = new File(DIRECTORY + "/" + PRODUCT_AMAZON_SHIP);
        File fileSuggest = new File(DIRECTORY + "/" + YAHOO_SEARCH_SUGGEST);

        try {
            file.createNewFile();
            fileShippingTemplate.createNewFile();
            fileVersionDatabase.createNewFile();
            fileProductError.createNewFile();
            fileProductAmazonUpload.createNewFile();
            fileProductAmazonShip.createNewFile();
            fileSuggest.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
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

    private int getVersionDatabase() {
        try {
            BufferedReader bw = new BufferedReader(new UTF8Reader(new FileInputStream(DatabaseHelper.DIRECTORY + "/" + DatabaseHelper.DATABASE_VERSION)));
            String version = bw.readLine();
            if (version != null && !version.isEmpty()) {
                return Integer.parseInt(version);
            } else {
                saveVersionDatabase(String.valueOf(Constants.DATABASE_VERSION));
                return Constants.DATABASE_VERSION;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 1;
    }

    private void saveVersionDatabase(String json) {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(DatabaseHelper.DIRECTORY + "/" + DatabaseHelper.DATABASE_VERSION, false), StandardCharsets.UTF_8))) {
            bw.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
