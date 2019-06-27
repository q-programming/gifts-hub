package com.qprogramming.gifts.api.manage;

import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.AccountService;
import com.qprogramming.gifts.config.mail.MailService;
import com.qprogramming.gifts.config.property.PropertyService;
import com.qprogramming.gifts.exceptions.AccountNotFoundException;
import com.qprogramming.gifts.gift.Gift;
import com.qprogramming.gifts.gift.GiftService;
import com.qprogramming.gifts.gift.category.CategoriesDTO;
import com.qprogramming.gifts.gift.category.Category;
import com.qprogramming.gifts.gift.category.CategoryDTO;
import com.qprogramming.gifts.gift.category.CategoryService;
import com.qprogramming.gifts.messages.MessagesService;
import com.qprogramming.gifts.settings.SearchEngineService;
import com.qprogramming.gifts.settings.Settings;
import com.qprogramming.gifts.support.Utils;
import org.apache.commons.collections4.CollectionUtils;
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
import javax.mail.MessagingException;
import javax.transaction.Transactional;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.qprogramming.gifts.settings.Settings.*;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

@RestController
@RequestMapping("/api/app")
public class AppRestController {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private PropertyService propertyService;
    private SearchEngineService searchEngineService;
    private MailService mailService;
    private CategoryService categoryService;
    private GiftService giftService;
    private AccountService accountService;
    private MessagesService msgSrv;

    @Autowired
    public AppRestController(PropertyService propertyService, SearchEngineService searchEngineService, MailService mailService, CategoryService categoryService, GiftService giftService, AccountService accountService, MessagesService messagesService) {
        this.propertyService = propertyService;
        this.searchEngineService = searchEngineService;
        this.mailService = mailService;
        this.categoryService = categoryService;
        this.giftService = giftService;
        this.accountService = accountService;
        this.msgSrv = messagesService;
    }

    @RolesAllowed("ROLE_ADMIN")
    @RequestMapping(value = "/settings", method = RequestMethod.PUT)
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
        updateCategoriesPriorities(settings.getCategories().stream().map(CategoryDTO::getCategory).collect(Collectors.toList()));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Update all passed categories with new priorities based on order in collection
     *
     * @param categories list of categories
     */
    private void updateCategoriesPriorities(List<Category> categories) {
        int counter = 0;
        for (Category category : categories) {
            category.setPriority(Integer.MAX_VALUE - counter);
            counter++;
        }
        categoryService.update(categories);
    }

    @RolesAllowed("ROLE_ADMIN")
    @RequestMapping(value = "/remove-category", method = RequestMethod.DELETE)
    public ResponseEntity removeCategory(@RequestBody Category category) {
        Account currentAccount = Utils.getCurrentAccount();
        if (currentAccount == null || !currentAccount.getIsAdmin()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Category dbCategory = categoryService.findById(category.getId());
        if (dbCategory == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        //find all gifts with this category and remove it from there
        giftService.removeCategory(category);
        categoryService.remove(dbCategory);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RolesAllowed("ROLE_ADMIN")
    @RequestMapping(value = "/update-category", method = RequestMethod.PUT)
    public ResponseEntity updateCategory(@RequestBody Category category) {
        Account currentAccount = Utils.getCurrentAccount();
        if (currentAccount == null || !currentAccount.getIsAdmin()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Category dbCategory = categoryService.findById(category.getId());
        if (dbCategory == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        dbCategory.setName(category.getName());
        categoryService.save(category);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RolesAllowed("ROLE_ADMIN")
    @RequestMapping(value = "/merge-categories", method = RequestMethod.PUT)
    public ResponseEntity mergeCategories(@RequestBody CategoriesDTO categories) {
        Account currentAccount = Utils.getCurrentAccount();
        if (currentAccount == null || !currentAccount.getIsAdmin()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        List<Long> ids = categories.getCategories().stream().map(Category::getId).collect(Collectors.toList());
        List<Category> categoriesList = categoryService.findByIds(ids);
        if (categoriesList.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Category newCategory = categoryService.findByName(categories.getName());
        giftService.mergeCategories(newCategory, categoriesList);
        categoriesList.remove(newCategory);
        categoryService.removeAll(categoriesList);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @RolesAllowed("ROLE_ADMIN")
    @RequestMapping(value = "/settings/email", method = RequestMethod.PUT)
    public ResponseEntity changeEmailSettings(@RequestBody Email settings) {
        Account currentAccount = Utils.getCurrentAccount();
        if (currentAccount == null || !currentAccount.getIsAdmin()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        try {
            mailService.testConnection(settings.getHost(), settings.getPort(), settings.getUsername(), settings.getPassword());
        } catch (MessagingException e) {
            LOG.warn("Bad SMTP configuration: {}", e);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
        propertyService.update(APP_EMAIL_HOST, settings.getHost());
        propertyService.update(APP_EMAIL_PORT, String.valueOf(settings.getPort()));
        propertyService.update(APP_EMAIL_USERNAME, settings.getUsername());
        propertyService.update(APP_EMAIL_PASS, settings.getPassword());
        propertyService.update(APP_EMAIL_ENCODING, settings.getEncoding());
        propertyService.update(APP_EMAIL_FROM, settings.getFrom());
        mailService.initMailSender();
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
        emailSettings.setFrom(propertyService.getProperty(APP_EMAIL_FROM));
        settings.setEmail(emailSettings);
        settings.setAppUrl(propertyService.getProperty(APP_URL));
        settings.setCategories(getCategories());
        return ResponseEntity.ok(settings);
    }


    private List<CategoryDTO> getCategories() {
        Map<Category, Long> giftsCategories = giftService
                .findAll()
                .stream()
                .map(Gift::getCategory)
                .filter(category -> category.getId() != Integer.MIN_VALUE)
                .collect(groupingBy(Function.identity(), counting()));
        Map<Category, Long> sortedMap = new TreeMap<>(giftsCategories);
        List<Category> allCategories = categoryService.findAll();
        CollectionUtils.disjunction(allCategories, giftsCategories.keySet())
                .forEach(category -> sortedMap.put(category, 0L));
        return sortedMap
                .entrySet()
                .stream()
                .map(entry -> new CategoryDTO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
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

    @RolesAllowed("ROLE_ADMIN")
    @RequestMapping(value = "/add-admin", method = RequestMethod.PUT)
    public ResponseEntity addAdmin(@RequestBody String id) {
        Account currentAccount = Utils.getCurrentAccount();
        if (currentAccount == null || !currentAccount.getIsAdmin()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Account account;
        try {
            account = accountService.findById(id);
        } catch (AccountNotFoundException e) {
            LOG.debug("Current account not found");
            return ResponseEntity.notFound().build();
        }
        accountService.addAsAdministrator(account);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RolesAllowed("ROLE_ADMIN")
    @Transactional
    @RequestMapping(value = "/remove-admin", method = RequestMethod.PUT)
    public ResponseEntity removeAdmin(@RequestBody String id) {
        Account currentAccount = Utils.getCurrentAccount();
        if (currentAccount == null || !currentAccount.getIsAdmin()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Account account;
        try {
            account = accountService.findById(id);
        } catch (AccountNotFoundException e) {
            LOG.debug("Current account not found");
            return ResponseEntity.notFound().build();
        }
        if (accountService.findUsers().stream().filter(Account::getIsAdmin).count() == 1) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("error.lastAdmin");
        }
        accountService.removeAdministrator(account);
        return new ResponseEntity<>(HttpStatus.OK);
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
        Map<String, Object> model = new HashMap<>();
        model.put("language", propertyService.getDefaultLang());
        return ResponseEntity.ok(model);
    }

    @RequestMapping(value = "/sort", method = RequestMethod.GET)
    public ResponseEntity getSortBy() {
        return ResponseEntity.ok(Settings.SortBy.fromString(propertyService.getProperty(APP_DEFAULT_SORT)));
    }

}
