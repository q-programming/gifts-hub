package com.qprogramming.gifts.messages;

import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.util.Locale;
import java.util.Properties;

public class ResourceMessageBundle extends ReloadableResourceBundleMessageSource {

    public Properties getMessages(Locale locale) {
        clearCacheIncludingAncestors();
        return getMergedProperties(locale).getProperties();
    }
}
