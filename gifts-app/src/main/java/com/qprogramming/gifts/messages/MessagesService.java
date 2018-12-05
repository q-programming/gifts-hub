package com.qprogramming.gifts.messages;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Properties;

/**
 * Created by XE050991499 on 2017-03-17.
 */
@Service
public class MessagesService {

    private ResourceMessageBundle messageSource;

    @Autowired
    public MessagesService(ResourceMessageBundle messageSource) {
        this.messageSource = messageSource;

    }

    public String getMessage(String id) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(id, null, locale);
    }

    public Properties getAllProperties(Locale locale) {
        Properties messages = messageSource.getMessages(locale);
        return messages;
    }

    public String getMessage(String string, Object[] objects, String defaultMessage, Locale locale) {
        return messageSource.getMessage(string, objects, defaultMessage, locale);
    }
}
