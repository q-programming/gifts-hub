package com.qprogramming.gifts.api.manage;

/**
 * Created by Khobar on 20.03.2017.
 */
public class Settings {
    public static final String APP_DEFAULT_LANG = "app.default.lang";
    public static final String APP_AVAILABLE_LANGS = "app.available.langs";
    private String language;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
