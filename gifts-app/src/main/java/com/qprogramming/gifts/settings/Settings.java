package com.qprogramming.gifts.settings;

import com.qprogramming.gifts.gift.category.CategoryDTO;
import io.jsonwebtoken.lang.Collections;
import lombok.*;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Settings {
    public static final String APP_DEFAULT_LANG = "app.default.lang";
    public static final String APP_DEFAULT_SORT = "app.default.sort";
    public static final String APP_AVAILABLE_LANGS = "app.available.langs";
    public static final String APP_GIFT_AGE = "app.gift.age";
    public static final String APP_BIRTHDAY_REMINDER = "app.gift.birthday-reminder";
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
    private String birthdayReminder;
    private SortBy sort;
    private Email email;
    private String appUrl;
    private List<CategoryDTO> categories;

    public void addSearchEngine(SearchEngine engine) {
        if (searchEngines == null) {
            searchEngines = new ArrayList<>();
        }
        searchEngines.add(engine);
    }

    public SortBy getSort() {
        if (sort == null) {
            return SortBy.NAME;
        }
        return sort;
    }

    public List<CategoryDTO> getCategories() {
        if (Collections.isEmpty(categories)) {
            categories = new ArrayList<>();
        }
        return categories;
    }

    public enum SortBy {
        GROUP, NAME;
        public static SortBy fromString(String string) {
            if (StringUtils.isEmpty(string)) {
                return NAME;
            }
            return valueOf(string);
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Email {
        private String host;
        private int port;
        private String username;
        private String password;
        private String encoding;
        private String from;
    }

}
