package com.qprogramming.gifts.api.manage;

import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.config.property.PropertyService;
import com.qprogramming.gifts.settings.SearchEngineService;
import com.qprogramming.gifts.settings.Settings;
import com.qprogramming.gifts.support.Utils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.RolesAllowed;

import static com.qprogramming.gifts.settings.Settings.*;

@RestController
@RequestMapping("/api/app")
public class AppRestController {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private PropertyService propertyService;
    private SearchEngineService searchEngineService;

    @Autowired
    public AppRestController(PropertyService propertyService, SearchEngineService searchEngineService) {
        this.propertyService = propertyService;
        this.searchEngineService = searchEngineService;
    }

    @RolesAllowed("ROLE_ADMIN")
    @RequestMapping(value = "/settings", method = RequestMethod.POST)
    public ResponseEntity changeSettings(@RequestBody Settings settings) {
        Account currentAccount = Utils.getCurrentAccount();
        if (currentAccount == null || !currentAccount.getIsAdmin()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        if (StringUtils.isNotBlank(settings.getLanguage())) {
            propertyService.update(APP_DEFAULT_LANG, settings.getLanguage());
        }
        if (StringUtils.isNotBlank(settings.getGiftAge())) {
            propertyService.update(APP_GIFT_AGE, settings.getGiftAge());
        }
        if (!CollectionUtils.isEmpty(settings.getSearchEngines())) {
            searchEngineService.updateSearchEngines(settings.getSearchEngines());
        }
        propertyService.update(APP_DEFAULT_SORT, String.valueOf(settings.getSort()));
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
        settings.setSearchEngines(searchEngineService.getAllSearchEngines());
        settings.setGiftAge(propertyService.getProperty(APP_GIFT_AGE));
        settings.setSort(Settings.SortBy.fromString(propertyService.getProperty(APP_DEFAULT_SORT)));
        return ResponseEntity.ok(settings);
    }

    @RequestMapping(value = "/search-engines", method = RequestMethod.GET)
    public ResponseEntity getAllSearchEngines() {
        return ResponseEntity.ok(searchEngineService.getAllSearchEngines());
    }

    @RequestMapping(value = "/languages", method = RequestMethod.GET)
    public ResponseEntity getAllLanguages() {
        return ResponseEntity.ok(propertyService.getLanguages());
    }

    @RequestMapping(value = "/sort", method = RequestMethod.GET)
    public ResponseEntity getSortBy() {
        return ResponseEntity.ok(Settings.SortBy.fromString(propertyService.getProperty(APP_DEFAULT_SORT)));
    }

}
