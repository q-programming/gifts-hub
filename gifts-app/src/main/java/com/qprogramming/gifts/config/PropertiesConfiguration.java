package com.qprogramming.gifts.config;

import com.qprogramming.gifts.config.property.DataBasePropertySource;
import com.qprogramming.gifts.config.property.PropertyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;

import java.util.Objects;
import java.util.Properties;

/**
 * Created by Remote on 19.03.2017.
 */

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PropertiesConfiguration {

    private static final String LOCAL_PROPERTIES = "localProperties";
    private static final String DATABASE_PROPERTIES = "database-properties";
    private static final String APP_PREFIX = "app";
    private PropertyRepository propertyRepository;
    private ConfigurableEnvironment env;
    private PropertySourcesPlaceholderConfigurer ppc;

    @Autowired
    public PropertiesConfiguration(PropertyRepository propertyRepository, ConfigurableEnvironment env,
                                   @Qualifier("propertySourcePlaceholderConfigurer") PropertySourcesPlaceholderConfigurer ppc) {
        this.propertyRepository = propertyRepository;
        this.env = env;
        this.ppc = ppc;
    }


    @Bean
    public DataBasePropertySource dataBasePropertySource() {
        DataBasePropertySource propertySource = new DataBasePropertySource(DATABASE_PROPERTIES, propertyRepository);
        MutablePropertySources sources = env.getPropertySources();
        sources.addFirst(propertySource);
        loadAppProperties();
        return propertySource;
    }

    /**
     * Ensure all application  localProperties are within datasources used while application is running
     */
    private void loadAppProperties() {
        Properties appProperties = new Properties();
        Properties localProperties = (Properties) Objects.requireNonNull(ppc.getAppliedPropertySources().get(LOCAL_PROPERTIES)).getSource();
        localProperties.stringPropertyNames().forEach(key -> {
            if (key.startsWith(APP_PREFIX)) {
                appProperties.setProperty(key, localProperties.getProperty(key));
            }
        });
        env.getPropertySources().addLast(new PropertiesPropertySource(LOCAL_PROPERTIES, appProperties));
    }
}
