package com.autojob.database.core;

import java.util.ArrayList;
import java.util.List;

/**
 * @Project mercari.
 * @Created by NguyenHuuTa on 2019-07-15.
 */
public class ContentValues {

    public ContentValues() {
        list = new ArrayList<>();
    }

    private List<Value> list;

    public void put(String key, Object value) {
        list.add(new Value(key, value));
    }


    public int size(){
       return list.size();
    }

    public List<Value> getList() {
        return list;
    }

    public class Value {
        Value(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        private String key;
        private Object value;

        public String getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }
    }
}
