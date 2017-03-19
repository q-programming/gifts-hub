package com.qprogramming.gifts.api.manage;

import com.qprogramming.gifts.config.property.Property;
import com.qprogramming.gifts.config.property.PropertyRepository;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.RolesAllowed;

/**
 * Created by Khobar on 19.03.2017.
 */
@RestController
@RequestMapping("/api/manage")
public class ManageRestController {

    public static final String LANG = "app.language";
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private PropertyRepository propertyRepository;

    @Autowired
    public ManageRestController(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    @RolesAllowed("ROLE_ADMIN")
    @RequestMapping(value = "/language", method = RequestMethod.POST)
    public ResponseEntity changeLanguage(@RequestBody String jsonObj) {
        JSONObject object = new JSONObject(jsonObj);
        if (object.has(LANG)) {
            //TODO Move to service
            Property langProperty = propertyRepository.findByKey(LANG);
            if (langProperty == null) {
                langProperty = new Property();
                langProperty.setKey(LANG);
            }
            String language = object.getString(LANG);
            langProperty.setValue(language);
            propertyRepository.save(langProperty);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
