package com.qprogramming.gifts;

import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.settings.SearchEngine;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

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

    public static byte[] convertObjectToJsonBytes(Object object)
            throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsBytes(object);
    }

    public static <T> T convertJsonToObject(String json, Class<T> object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, object);
    }

    public static <T> List<T> convertJsonToList(String json, Class<List> listClass, Class<T> elementClass) throws java.io.IOException {
        ObjectMapper mapper = new org.codehaus.jackson.map.ObjectMapper();
        return mapper.readValue(json, TypeFactory.defaultInstance().constructCollectionType(listClass, elementClass));
    }


    public static Account createAccount() {
        Account testAccount = new Account(USERNAME, EMAIL, "password");
        return createAccount("name", "surname");
    }

    public static Account createAccount(String name, String surname) {
        Account account = new Account(USERNAME, EMAIL, "password");
        account.setName(name);
        account.setSurname(surname);
        account.setLanguage("en");
        account.setId(USER_RANDOM_ID);
        return account;
    }

    public static SearchEngine createSearchEngine(String name, String link, String icon) {
        SearchEngine engine = new SearchEngine();
        engine.setName(name);
        engine.setSearchString(link);
        engine.setIcon(icon);
        return engine;
    }

}
