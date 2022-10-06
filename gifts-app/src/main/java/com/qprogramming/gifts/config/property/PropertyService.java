package com.qprogramming.gifts.config.property;

import com.qprogramming.gifts.messages.MessagesService;
import com.qprogramming.gifts.support.Utils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.qprogramming.gifts.settings.Settings.APP_DEFAULT_LANG;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Created by XE050991499 on 2017-03-20.
 */
@Service
public class PropertyService {
    public static final String LANG_DEFAULT_MSG = "NOT FOUND";
    public static final String DEFAULT_APP_LANGUAGE = "pl";

    private PropertyRepository propertyRepository;
    private MessagesService msgSrv;
    private Environment env;

    @Autowired
    public PropertyService(PropertyRepository propertyRepository, MessagesService msgSrv, Environment env) {
        this.propertyRepository = propertyRepository;
        this.msgSrv = msgSrv;
        this.env = env;
    }

    /**
     * Updates property in database. If not found it's created
     *
     * @param key   key to be updated
     * @param value new value of either existing or new property
     * @return
     */
    public Property update(String key, String value) {
        Property langProperty = propertyRepository.findByKey(key);
        if (langProperty == null) {
            langProperty = new Property();
            langProperty.setKey(key);
        }
        langProperty.setValue(value);
        return propertyRepository.save(langProperty);
    }

    //TODO needs rewrite
    public Map<String, String> getLanguages() {
        Map<String, String> languages = new HashMap<>();
        Map<String, Locale> availableLocales = new HashMap<>();
        for (Locale locale : Locale.getAvailableLocales()) {
            String msg = msgSrv.getMessage("main.language.name", null, LANG_DEFAULT_MSG, locale);
            if (!LANG_DEFAULT_MSG.equals(msg) && !availableLocales.containsKey(locale.getLanguage())) {
                availableLocales.put(locale.getLanguage(), locale);
            }
        }
        for (String c : availableLocales.keySet()) {
            languages.put(c, availableLocales.get(c).getDisplayLanguage(Utils.getCurrentLocale()));
        }
        return languages;
    }

    /**
     * Get default application language set by admin in app management. In case of it's empty, DEFAULT_APP_LANGUAGE is returned
     *
     * @return default application language
     */
    public String getDefaultLang() {
        String lang = getProperty(APP_DEFAULT_LANG);
        if (StringUtils.isBlank(lang)) {
            return DEFAULT_APP_LANGUAGE;
        }
        return lang;
    }

    /**
     * Returns property. As database property is first in order it will be first place to look. Otherwise , file based properties will be searched
     *
     * @param key key for which value will be searched
     * @return String representation of parameter
     */
    public String getProperty(String key) {
        String property = env.getProperty(key);
        if (StringUtils.isEmpty(property)) {
            return EMPTY;
        }
        return property;
    }

    /**
     * Returns property or default value
     * @see #getProperty
     *
     * @param key key for which value will be searched
     * @param defaultValue default if value will be returned empty
     * @return String representation of parameter
     */
    public String getPropertyOrDefault(String key, String defaultValue) {
        String property = env.getProperty(key);
        if (StringUtils.isEmpty(property)) {
            return defaultValue;
        }
        return property;
    }
}
