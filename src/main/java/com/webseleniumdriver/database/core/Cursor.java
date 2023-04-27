package com.webseleniumdriver.database.core;


import com.webseleniumdriver.utils.Constants;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @Project mercari.
 * @Created by NguyenHuuTa on 2019-07-15.
 */
public class Cursor {
    private ResultSet resultSet;

    public Cursor(ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    public boolean moveToFirst() {
        try {
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean moveToLast() {
        try {
            return resultSet.last();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean moveToNext() {
        try {
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getColumnIndex(String columnName) {
        return columnName;
    }

    public long getLongs(String columnName) {
        try {
            return resultSet.getLong(columnName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public long getLongs(int columnIndex) {
        try {
            return resultSet.getLong(columnIndex);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double getDoubles(String columnName) {
        try {
            return resultSet.getDouble(columnName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double getDoubles(int columnIndex) {
        try {
            return resultSet.getDouble(columnIndex);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }


    public int getInts(String columnName) {
        try {
            return resultSet.getInt(columnName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getInts(int columnIndex) {
        try {
            return resultSet.getInt(columnIndex);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public String getStrings(String columnName) {
        try {
            return resultSet.getString(columnName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Constants.EMPTY;
    }

    public String getStrings(int columnIndex) {
        try {
            return resultSet.getString(columnIndex);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Constants.EMPTY;
    }

    public void onClose() {
        try {
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

