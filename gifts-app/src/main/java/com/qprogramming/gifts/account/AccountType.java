package com.qprogramming.gifts.account;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Khobar on 12.03.2017.
 */
public enum AccountType {

    LOCAL, GOOGLE, FACEBOOK, KID, TEMP;

    private static final Logger LOG = LoggerFactory.getLogger(AccountType.class);

    public String getCode() {
        return toString().toLowerCase().substring(0, 1);
    }

    public static AccountType type(String string) {
        if (StringUtils.isNotBlank(string)) {
            try {
                return AccountType.valueOf(string.toUpperCase());
            } catch (IllegalArgumentException e) {
                LOG.error("Failed to parse enum of value {}", string);
            }
        }
        return LOCAL;
    }
}
