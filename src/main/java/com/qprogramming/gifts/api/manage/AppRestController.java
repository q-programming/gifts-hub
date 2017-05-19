package com.qprogramming.gifts.api.manage;

import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.config.mail.MailService;
import com.qprogramming.gifts.config.property.PropertyService;
import com.qprogramming.gifts.settings.SearchEngineService;
import com.qprogramming.gifts.settings.Settings;
import com.qprogramming.gifts.support.ResultData;
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
import javax.mail.MessagingException;

import static com.qprogramming.gifts.settings.Settings.*;

@RestController
@RequestMapping("/api/app")
public class AppRestController {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private PropertyService propertyService;
    private SearchEngineService searchEngineService;
    private MailService mailService;

    @Autowired
    public AppRestController(PropertyService propertyService, SearchEngineService searchEngineService, MailService mailService) {
        this.propertyService = propertyService;
        this.searchEngineService = searchEngineService;
        this.mailService = mailService;
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
        if (StringUtils.isNotBlank(settings.getAppUrl())) {
            propertyService.update(APP_URL, settings.getAppUrl());
        }
        propertyService.update(APP_DEFAULT_SORT, String.valueOf(settings.getSort()));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RolesAllowed("ROLE_ADMIN")
    @RequestMapping(value = "/settings/email", method = RequestMethod.POST)
    public ResponseEntity changeEmailSettings(@RequestBody Email settings) {
        Account currentAccount = Utils.getCurrentAccount();
        if (currentAccount == null || !currentAccount.getIsAdmin()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        try {
            mailService.testConnection(settings.getHost(), settings.getPort(), settings.getUsername(), settings.getPassword());
        } catch (MessagingException e) {
            LOG.warn("Bad SMTP configuration: {}", e);
            return new ResultData.ResultBuilder().ok().warn().message(e.getMessage()).build();
        }
        propertyService.update(APP_EMAIL_HOST, settings.getHost());
        propertyService.update(APP_EMAIL_PORT, String.valueOf(settings.getPort()));
        propertyService.update(APP_EMAIL_USERNAME, settings.getUsername());
        propertyService.update(APP_EMAIL_PASS, settings.getPassword());
        propertyService.update(APP_EMAIL_ENCODING, settings.getEncoding());
        mailService.initMailSender();
        //TODO remove sample
//        Mail mail = new Mail();
//        mail.setMailFrom("giftshub@q-programming.pl");
//        mail.setMailTo("kubarom@gmail.com");
//        mail.setMailSubject("Mail sending test");
//        Map<String, Object> model = new HashMap<>();
//        model.put("firstName", "John");
//        model.put("lastName", "Doe");
//        mail.setModel(model);
//        mailService.sendEmail(mail);
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
        settings.setLanguage(propertyService.getDefaultLang());
        settings.setSearchEngines(searchEngineService.getAllSearchEngines());
        settings.setGiftAge(propertyService.getProperty(APP_GIFT_AGE));
        settings.setSort(Settings.SortBy.fromString(propertyService.getProperty(APP_DEFAULT_SORT)));
        Settings.Email emailSettings = new Settings.Email();
        emailSettings.setHost(propertyService.getProperty(APP_EMAIL_HOST));
        try {
            emailSettings.setPort(Integer.parseInt(propertyService.getProperty(APP_EMAIL_PORT)));
        } catch (NumberFormatException e) {
            LOG.warn("Failed to set port from properties");
        }
        emailSettings.setUsername(propertyService.getProperty(APP_EMAIL_USERNAME));
        emailSettings.setPassword(propertyService.getProperty(APP_EMAIL_PASS));
        emailSettings.setEncoding(propertyService.getProperty(APP_EMAIL_ENCODING));
        settings.setEmail(emailSettings);
        settings.setAppUrl(propertyService.getProperty(APP_URL));
        return ResponseEntity.ok(settings);
    }

    @RolesAllowed("ROLE_ADMIN")
    @RequestMapping(value = "/setup", method = RequestMethod.GET)
    public ResponseEntity setupNeeded() {
        Account currentAccount = Utils.getCurrentAccount();
        if (currentAccount == null || !currentAccount.getIsAdmin()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return ResponseEntity.ok(StringUtils.isBlank(propertyService.getProperty(APP_URL)) || searchEngineService.getAllSearchEngines().isEmpty());
    }

    @RequestMapping(value = "/search-engines", method = RequestMethod.GET)
    public ResponseEntity getAllSearchEngines() {
        return ResponseEntity.ok(searchEngineService.getAllSearchEngines());
    }

    @RequestMapping(value = "/languages", method = RequestMethod.GET)
    public ResponseEntity getAllLanguages() {
        return ResponseEntity.ok(propertyService.getLanguages());
    }

    @RequestMapping(value = "/default-language", method = RequestMethod.GET)
    public ResponseEntity getDefaultLanguage() {
        return ResponseEntity.ok(propertyService.getDefaultLang());
    }

    @RequestMapping(value = "/sort", method = RequestMethod.GET)
    public ResponseEntity getSortBy() {
        return ResponseEntity.ok(Settings.SortBy.fromString(propertyService.getProperty(APP_DEFAULT_SORT)));
    }

}
