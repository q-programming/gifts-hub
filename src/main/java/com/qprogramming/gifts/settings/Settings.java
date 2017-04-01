package com.qprogramming.gifts.settings;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Khobar on 20.03.2017.
 */
public class Settings {
    public static final String APP_DEFAULT_LANG = "app.default.lang";
    public static final String APP_AVAILABLE_LANGS = "app.available.langs";
    public static final String APP_GIFT_AGE ="app.gift.age";
    private String language;
    private List<SearchEngine> searchEngines;
    private String giftAge;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public List<SearchEngine> getSearchEngines() {
        return searchEngines;
    }

    public void setSearchEngines(List<SearchEngine> searchEngines) {
        this.searchEngines = searchEngines;
    }

    public void addSearchEngine(SearchEngine engine) {
        if (searchEngines == null) {
            searchEngines = new ArrayList<>();
        }
        searchEngines.add(engine);
    }

    public String getGiftAge() {
        return giftAge;
    }

    public void setGiftAge(String giftAge) {
        this.giftAge = giftAge;
    }
}
