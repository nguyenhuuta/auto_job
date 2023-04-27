package com.webseleniumdriver.model.object;

public class OptionObject {
    private String name;
    private Object value;

    public OptionObject(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return name;
    }
}
