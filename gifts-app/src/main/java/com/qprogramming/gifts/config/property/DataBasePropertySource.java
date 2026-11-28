package com.qprogramming.gifts.config.property;

import org.springframework.core.env.PropertySource;
import org.springframework.util.StringUtils;

/**
 * Created by Khobar on 19.03.2017.
 */
public class DataBasePropertySource extends PropertySource {

    private PropertyRepository propertyRepository;

    public DataBasePropertySource(String name, PropertyRepository propertyRepository) {
        super(name);
        this.propertyRepository = propertyRepository;
    }

    @Override
    public Object getProperty(String key) {
        Property property = propertyRepository.findByKey(key);
        if (property == null || !StringUtils.hasText(property.getValue())) {
            return null;
        }
        return property.getValue();
    }
}
