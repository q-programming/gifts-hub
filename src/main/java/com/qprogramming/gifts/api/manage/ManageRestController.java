package com.qprogramming.gifts.api.manage;

import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.config.property.PropertyService;
import com.qprogramming.gifts.support.Utils;
import org.apache.commons.lang3.StringUtils;
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

import static com.qprogramming.gifts.api.manage.Settings.APP_DEFAULT_LANG;

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
    @RequestMapping(value = "/settings", method = RequestMethod.POST)
    public ResponseEntity changeSettings(@RequestBody Settings settings) {
        if (StringUtils.isNotBlank(settings.getLanguage())) {
            propertyService.update(APP_DEFAULT_LANG, settings.getLanguage());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RolesAllowed("ROLE_ADMIN")
    @RequestMapping(value = "/settings", method = RequestMethod.GET)
    public ResponseEntity applicationSettings() {
        Account currentAccount = Utils.getCurrentAccount();
        if (currentAccount == null || !currentAccount.getIsAdmin()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Settings settings = new Settings();
        settings.setLanguage(propertyService.getProperty(APP_DEFAULT_LANG));
        return ResponseEntity.ok(settings);
    }

}
