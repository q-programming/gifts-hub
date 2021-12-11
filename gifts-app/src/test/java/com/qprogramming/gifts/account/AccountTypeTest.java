package com.qprogramming.gifts.account;

import lombok.val;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountTypeTest {

    @Test
    public void testTypeSuccess() {
        val comparedTo = AccountType.GOOGLE;
        val result = AccountType.type("GOOGLE");
        assertThat(result).isEqualTo(comparedTo);
    }

    @Test
    public void testTypeDefaultsToLocal() {
        val result = AccountType.type("none");
        assertThat(result).isEqualTo(AccountType.LOCAL);
    }

}