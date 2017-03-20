package com.qprogramming.gifts.api.manage;

import com.qprogramming.gifts.config.property.PropertyService;
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

import static com.qprogramming.gifts.config.property.Property.APP_DEFAULT_LANG;

/**
 * Created by Khobar on 19.03.2017.
 */
@RestController
@RequestMapping("/api/manage")
public class ManageRestController {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private PropertyService propertyService;

    @Autowired
    public ManageRestController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @RolesAllowed("ROLE_ADMIN")
    @RequestMapping(value = "/language", method = RequestMethod.POST)
    public ResponseEntity changeLanguage(@RequestBody String jsonObj) {
        JSONObject object = new JSONObject(jsonObj);
        if (object.has(APP_DEFAULT_LANG)) {
            propertyService.update(APP_DEFAULT_LANG, object.getString(APP_DEFAULT_LANG));
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
