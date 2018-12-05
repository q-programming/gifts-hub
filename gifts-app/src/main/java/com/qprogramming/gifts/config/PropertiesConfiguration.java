package com.qprogramming.gifts.config;

import com.qprogramming.gifts.config.property.DataBasePropertySource;
import com.qprogramming.gifts.config.property.PropertyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;

/**
 * Created by Remote on 19.03.2017.
 */

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PropertiesConfiguration {

    private PropertyRepository propertyRepository;
    private ConfigurableEnvironment env;

    @Autowired
    public PropertiesConfiguration(PropertyRepository propertyRepository, ConfigurableEnvironment env) {
        this.propertyRepository = propertyRepository;
        this.env = env;
    }


    @Bean
    public DataBasePropertySource dataBasePropertySource() {
        DataBasePropertySource propertySource = new DataBasePropertySource("database-properties", propertyRepository);
        MutablePropertySources sources = env.getPropertySources();
        sources.addFirst(propertySource);
        return propertySource;
    }
}
