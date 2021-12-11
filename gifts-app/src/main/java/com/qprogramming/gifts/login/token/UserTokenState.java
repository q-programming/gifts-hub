package com.qprogramming.gifts.login.token;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Based on
 * https://github.com/bfwg/springboot-jwt-starter
 */
@Getter
@Setter
@AllArgsConstructor
public class UserTokenState {
    private String access_token;
    private Long expires_in;

    public UserTokenState() {
        this.access_token = null;
        this.expires_in = -1L;
    }
}
