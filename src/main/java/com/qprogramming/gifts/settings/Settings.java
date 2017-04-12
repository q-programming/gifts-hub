package com.qprogramming.gifts.settings;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Khobar on 20.03.2017.
 */
public class Settings {
    public static final String APP_DEFAULT_LANG = "app.default.lang";
    public static final String APP_DEFAULT_SORT = "app.default.sort";
    public static final String APP_AVAILABLE_LANGS = "app.available.langs";
    public static final String APP_GIFT_AGE = "app.gift.age";
    private String language;
    private List<SearchEngine> searchEngines;
    private String giftAge;
    private SortBy sort;

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

    public SortBy getSort() {
        if (sort == null) {
            return SortBy.NAME;
        }
        return sort;
    }

    public void setSort(SortBy sort) {
        this.sort = sort;
    }

    public enum SortBy {
        FAMILY, NAME;

        public static SortBy fromString(String string) {
            if (StringUtils.isEmpty(string)) {
                return NAME;
            }
            return valueOf(string);
        }

    }
}
