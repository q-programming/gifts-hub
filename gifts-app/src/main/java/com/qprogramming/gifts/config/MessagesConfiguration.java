package com.qprogramming.gifts.config;

import com.qprogramming.gifts.messages.ResourceMessageBundle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

/**
 * Created by Khobar on 22.03.2017.
 */
@Configuration
public class MessagesConfiguration {
    @Bean
    public ReloadableResourceBundleMessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ResourceMessageBundle();
        messageSource.setBasename("classpath:lang/messages");
        messageSource.setCacheSeconds(3600); //refresh cache once per hour
        messageSource.setFallbackToSystemLocale(false);
        return messageSource;
    }
}
