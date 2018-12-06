package com.qprogramming.gifts;

import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.authority.Authority;
import com.qprogramming.gifts.account.authority.Role;
import com.qprogramming.gifts.gift.Gift;
import com.qprogramming.gifts.schedule.AppEvent;
import com.qprogramming.gifts.schedule.AppEventType;
import com.qprogramming.gifts.settings.SearchEngine;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

//import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by Khobar on 05.03.2017.
 */
public class TestUtil {

    /**
     * MediaType for JSON UTF8
     */
    public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(
            MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));
    public static final String EMAIL = "user@test.com";
    public static final String USERNAME = "username";
    public static final String USER_RANDOM_ID = "USER-RANDOM-ID";
    public static final String ADMIN_EMAIL = "admin@test.com";
    public static final String ADMIN_USERNAME = "username_admin";
    public static final String ADMIN_RANDOM_ID = "ADMIN-USER-RANDOM-ID";

    public static byte[] convertObjectToJsonBytes(Object object)
            throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.writeValueAsBytes(object);
    }

    public static <T> T convertJsonToObject(String json, Class<T> object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.readValue(json, object);
    }

    public static <T> List<T> convertJsonToList(String json, Class<? extends List> collectionClass, Class<T> elementClass) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.readValue(json, TypeFactory.defaultInstance().constructCollectionType(collectionClass, elementClass));
    }

    public static <T> Set<T> convertJsonToSet(String json, Class<? extends Set> collectionClass, Class<T> elementClass) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.readValue(json, TypeFactory.defaultInstance().constructCollectionType(collectionClass, elementClass));
    }

    public static <T, V> Map<T, V> convertJsonToTreeMap(String json, Class<T> keyClass, Class<V> valueClass) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.readValue(json, TypeFactory.defaultInstance().constructMapType(TreeMap.class, keyClass, valueClass));
    }


    public static Account createAccount() {
        Account testAccount = new Account(USERNAME, EMAIL, "password");
        testAccount.setAuthorities(Collections.singletonList(createUserAuthority()));
        return createAccount("name", "surname");
    }

    public static Account createAccount(String name, String surname) {
        Account account = new Account(USERNAME, EMAIL, "password");
        account.setName(name);
        account.setSurname(surname);
        account.setLanguage("en");
        account.setId(USER_RANDOM_ID);
        account.setAuthorities(Collections.singletonList(createUserAuthority()));
        return account;
    }

    public static Account createAdminAccount() {
        Account account = createAccount("name", "surname");
        account.setAuthorities(Collections.singletonList(createAdminAuthority()));
        account.setUsername(ADMIN_USERNAME);
        account.setEmail(ADMIN_EMAIL);
        account.setId(ADMIN_RANDOM_ID);

        return account;
    }

    public static SearchEngine createSearchEngine(String name, String link, String icon) {
        SearchEngine engine = new SearchEngine();
        engine.setName(name);
        engine.setSearchString(link);
        engine.setIcon(icon);
        return engine;
    }


    public static Gift createGift(long id, Account account) {
        Gift gift = new Gift();
        gift.setId(id);
        gift.setName("name");
        gift.setLink("http://google.ocm");
        gift.setUserId(account.getId());
        return gift;
    }

    public static List<Account> createAccountList() {
        Account account1 = TestUtil.createAccount("Bob", "Doe");
        account1.setId(USER_RANDOM_ID + "1");
        Account account2 = TestUtil.createAccount("Andy", "Doe");
        account2.setId(USER_RANDOM_ID + "2");
        Account account3 = TestUtil.createAccount("John", "Doe");
        account3.setId(USER_RANDOM_ID + "3");
        Account account4 = TestUtil.createAccount("John", "Doe");
        account4.setId(USER_RANDOM_ID + "4");
        Account account5 = TestUtil.createAccount("John", "Doe");
        account5.setId(USER_RANDOM_ID + "5");
        Account account6 = TestUtil.createAccount("John", "Doe");
        account6.setId(USER_RANDOM_ID + "6");
        List<Account> all = new ArrayList<>();
        all.add(account1);
        all.add(account2);
        all.add(account3);
        all.add(account4);
        all.add(account5);
        all.add(account6);
        return all;
    }

    public static AppEvent createEvent(Account account) {
        AppEvent event = new AppEvent();
        event.setAccount(account);
        event.setType(AppEventType.NEW);
        Gift gift = createGift(1L, account);
        event.setGift(gift);
        return event;
    }


    public static Authority createUserAuthority() {
        Authority authority = new Authority();
        authority.setName(Role.ROLE_USER);
        return authority;
    }

    public static Authority createAdminAuthority() {
        Authority authority = new Authority();
        authority.setName(Role.ROLE_ADMIN);
        return authority;
    }


}
