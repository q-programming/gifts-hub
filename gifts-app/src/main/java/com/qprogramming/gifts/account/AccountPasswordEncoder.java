package com.qprogramming.gifts.account;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Created by Jakub Romaniszyn on 20.07.2018.
 */
@Component
public class AccountPasswordEncoder extends BCryptPasswordEncoder {
    public AccountPasswordEncoder() {
        super(11);
    }
}
