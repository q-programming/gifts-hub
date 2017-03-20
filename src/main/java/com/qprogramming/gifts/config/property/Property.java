package com.qprogramming.gifts.config.property;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Property {
    public static final String APP_DEFAULT_LANG = "app.default.lang";
    public static final String APP_AVAILABLE_LANGS = "app.available.langs";


    @Id
    private String key;
    @Column
    private String value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
