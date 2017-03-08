package com.qprogramming.gifts;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.qprogramming.gifts.account.Account;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.Charset;

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
    public static final String USER_RANDOM_ID = "USER-RANDOM-ID";

    public static byte[] convertObjectToJsonBytes(Object object)
            throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        JavaTimeModule module = new JavaTimeModule();
        mapper.registerModule(module);

        return mapper.writeValueAsBytes(object);
    }

    public static Account createAccount() {
        Account testAccount = new Account(EMAIL, "password");
        return createAccount("user", "surname");
    }

    public static Account createAccount(String name, String surname) {
        Account account = new Account(EMAIL, "password");
        account.setName(name);
        account.setSurname(surname);
        account.setLanguage("en");
        account.setId(USER_RANDOM_ID);
        return account;
    }

}
