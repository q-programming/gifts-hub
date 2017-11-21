package com.qprogramming.gifts.settings;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class Settings {
    public static final String APP_DEFAULT_LANG = "app.default.lang";
    public static final String APP_DEFAULT_SORT = "app.default.sort";
    public static final String APP_AVAILABLE_LANGS = "app.available.langs";
    public static final String APP_GIFT_AGE = "app.gift.age";
    public static final String APP_URL = "app.url";
    public static final String APP_EMAIL_HOST = "spring.mail.host";
    public static final String APP_EMAIL_PORT = "spring.mail.port";
    public static final String APP_EMAIL_USERNAME = "spring.mail.username";
    public static final String APP_EMAIL_PASS = "spring.mail.password";
    public static final String APP_EMAIL_ENCODING = "spring.mail.defaultEncoding";
    public static final String APP_EMAIL_FROM = "spring.mail.from";
    public static final String APP_NEWSLETTER_SCHEDULE = "app.newsletter.schedule";

    private String language;
    private List<SearchEngine> searchEngines;
    private String giftAge;
    private SortBy sort;
    private Email email;
    private String appUrl;

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

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

    public String getAppUrl() {
        return appUrl;
    }

    public void setAppUrl(String appUrl) {
        this.appUrl = appUrl;
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

    public static class Email {
        private String host;
        private int port;
        private String username;
        private String password;
        private String encoding;
        private String from;

        public Email() {
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getEncoding() {
            return encoding;
        }

        public void setEncoding(String encoding) {
            this.encoding = encoding;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }
    }

}
