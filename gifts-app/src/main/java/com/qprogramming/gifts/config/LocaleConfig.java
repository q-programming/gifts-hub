package com.qprogramming.gifts.config;

import com.qprogramming.gifts.config.property.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

/**
 * Created by XE050991499 on 2017-03-17.
 */
@Configuration
@Import({PropertiesConfiguration.class})
public class LocaleConfig {

    private PropertyService propertyService;

    @Autowired
    public LocaleConfig(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(new Locale(propertyService.getDefaultLang()));
        return slr;
    }

}
