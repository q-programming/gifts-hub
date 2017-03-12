package com.qprogramming.gifts.account;

/**
 * Created by Khobar on 12.03.2017.
 */
public enum AccountType {
    LOCAL, GOOGLE, FACEBOOK;

    public String getCode() {
        return toString().toLowerCase().substring(0, 1);
    }
}
