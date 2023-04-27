package com.webseleniumdriver.database.core;

import com.webseleniumdriver.database.DatabaseHelper;
import com.webseleniumdriver.utils.Logger;
import com.webseleniumdriver.utils.TextUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @Project mercari.
 * @Created by NguyenHuuTa on 2019-07-15.
 */
public class CustomStatement {
    public static final String TAG = "CustomStatement";

    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {

        if (TextUtils.isEmpty(groupBy) && !TextUtils.isEmpty(having)) {
            throw new IllegalArgumentException(
                    "HAVING clauses are only permitted when using a groupBy clause");
        }
        StringBuilder query = new StringBuilder(120);
        query.append("SELECT ");
        if (columns != null && columns.length != 0) {
            appendColumns(query, columns);
        } else {
            query.append("* ");
        }
        query.append("FROM ");
        query.append(table);
        appendClause(query, " WHERE ", selection);
        appendClause(query, " GROUP BY ", groupBy);
        appendClause(query, " HAVING ", having);
        appendClause(query, " ORDER BY ", orderBy);
        try {
            PreparedStatement preparedStatement = DatabaseHelper.getInstance().createPrepareStatement(query.toString());
            appendValueClause(preparedStatement, selectionArgs, 1);
            return new Cursor(preparedStatement.executeQuery());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit, String offset) {

        if (TextUtils.isEmpty(groupBy) && !TextUtils.isEmpty(having)) {
            throw new IllegalArgumentException(
                    "HAVING clauses are only permitted when using a groupBy clause");
        }
        StringBuilder query = new StringBuilder(120);
        query.append("SELECT ");
        if (columns != null && columns.length != 0) {
            appendColumns(query, columns);
        } else {
            query.append("* ");
        }
        query.append("FROM ");
        query.append(table);
        appendClause(query, " WHERE ", selection);
        appendClause(query, " GROUP BY ", groupBy);
        appendClause(query, " HAVING ", having);
        appendClause(query, " ORDER BY ", orderBy);
        appendClause(query, " LIMIT ", limit);
        if (!TextUtils.isEmpty(offset)) {
            appendClause(query, " OFFSET ", offset);
        }
        try {
            Logger.d(TAG, query.toString());
            PreparedStatement preparedStatement = DatabaseHelper.getInstance().createPrepareStatement(query.toString());
            appendValueClause(preparedStatement, selectionArgs, 1);
            return new Cursor(preparedStatement.executeQuery());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public long insert(String table, String nullColumnHack, ContentValues values) {
        int size = values.size();
        if (size == 0 || TextUtils.isEmpty(table)) return -1;

        StringBuilder query = new StringBuilder("INSERT INTO ");
        query.append(table);
        query.append(" (");


        StringBuilder valueString = new StringBuilder(" VALUES (");
        int index = 0;
        for (ContentValues.Value item : values.getList()) {
            String key = item.getKey();
            if (index == (size - 1)) {
                query.append(key);
                valueString.append("?");
                valueString.append(")");
            } else {
                query.append(key).append(",");
                valueString.append("?").append(",");
            }
            index++;
        }
        query.append(")");
        query.append(valueString.toString());
        try {
            PreparedStatement preparedStatement = DatabaseHelper.getInstance().createPrepareStatement(query.toString());
            index = 0;
            for (ContentValues.Value item : values.getList()) {
                Object value = item.getValue();
                setValue(preparedStatement, value, index + 1);
                index++;
            }
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int update(String table, ContentValues values, String selection, String[] selectionArgs) {
        int size = values.size();
        if (size == 0 || TextUtils.isEmpty(table)) return -1;

        StringBuilder query = new StringBuilder("UPDATE ");
        query.append(table);
        query.append(" SET ");

        int index = 0;
        for (ContentValues.Value item : values.getList()) {
            String key = item.getKey();
            if (index == (size - 1)) {
                query.append(key).append("=?");
            } else {
                query.append(key).append("=?, ");
            }
            index++;
        }

        appendClause(query, " WHERE ", selection);
        try {
            PreparedStatement preparedStatement = DatabaseHelper.getInstance().createPrepareStatement(query.toString());
            index = 0;
            for (ContentValues.Value item : values.getList()) {
                Object value = item.getValue();
                setValue(preparedStatement, value, index + 1);
                index++;
            }
            appendValueClause(preparedStatement, selectionArgs, index);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int delete(String table, String selection, String[] selectionArgs) {

        StringBuilder query = new StringBuilder("DELETE FROM ");
        query.append(table);

        appendClause(query, " WHERE ", selection);
        try {
            PreparedStatement preparedStatement = DatabaseHelper.getInstance().createPrepareStatement(query.toString());
            appendValueClause(preparedStatement, selectionArgs, 1);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }


    private void setValue(PreparedStatement preparedStatement, Object object, int index) {
        if (object == null) return;
        try {
            if (object instanceof Integer) {
                preparedStatement.setInt(index, (Integer) object);
            } else if (object instanceof String) {
                preparedStatement.setString(index, object.toString());
            } else if (object instanceof Long) {
                preparedStatement.setLong(index, (Long) object);
            } else if (object instanceof Double) {
                preparedStatement.setDouble(index, (Double) object);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    public void appendColumns(StringBuilder s, String[] columns) {
        int n = columns.length;
        for (int i = 0; i < n; i++) {
            String column = columns[i];
            if (column != null) {
                if (i > 0) {
                    s.append(", ");
                }
                s.append(column);
            }
        }
        s.append(' ');
    }

    private void appendClause(StringBuilder s, String name, String clause) {
        if (!TextUtils.isEmpty(clause)) {
            s.append(name);
            s.append(clause);
        }
    }

    private void appendValueClause(PreparedStatement preparedStatement, String[] selectionArgs, int start) {
        if (selectionArgs != null && selectionArgs.length > 0) {
            int n = selectionArgs.length;
            for (int i = 0; i < n; i++) {
                String value = selectionArgs[i];
                try {
                    preparedStatement.setString(i + start, value);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
