package com.qprogramming.gifts.config;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Created by Jakub Romaniszyn on 2018-08-13
 * <p>
 * Adds Jackson Hibernate 5 module based configuration to properly handle fetch = LAZY objects ( only return them when requested )
 */
@Configuration
public class MappingConfiguration {

    public static class Public {
    }
    public static class Members {
    }


    @Bean
    public Jackson2ObjectMapperBuilder configureObjectMapper() {
        return new Jackson2ObjectMapperBuilder()
                .defaultViewInclusion(true)
                .failOnUnknownProperties(false)
                .modulesToInstall(Hibernate5Module.class);
    }

}
