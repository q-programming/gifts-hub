package com.qprogramming.gifts.settings;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class Settings {
    public static final String APP_DEFAULT_LANG = "app.default.lang";
    public static final String APP_DEFAULT_SORT = "app.default.sort";
    public static final String APP_AVAILABLE_LANGS = "app.available.langs";
    public static final String APP_GIFT_AGE = "app.gift.age";
    public static final String APP_EMAIL_HOST = "app.email.host";
    public static final String APP_EMAIL_PORT = "app.email.post";
    public static final String APP_EMAIL_USERNAME = "app.email.username";
    public static final String APP_EMAIL_PASS = "app.email.pass";
    public static final String APP_EMAIL_SMTP_AUTH = "app.email.smtp.auth";
    public static final String APP_EMAIL_START_TTLS = "app.email.start.ttls";
    public static final String APP_EMAIL_ENCODING = "app.email.encoding";


    private String language;
    private List<SearchEngine> searchEngines;
    private String giftAge;
    private SortBy sort;
    private Email email;


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
        private boolean smtpauth;
        private boolean startttls;

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

        public boolean isSmtpauth() {
            return smtpauth;
        }

        public void setSmtpauth(boolean smtpauth) {
            this.smtpauth = smtpauth;
        }

        public boolean isStartttls() {
            return startttls;
        }

        public void setStartttls(boolean startttls) {
            this.startttls = startttls;
        }
    }
}
