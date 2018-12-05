package com.qprogramming.gifts.login;

import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.login.token.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Based on
 * https://github.com/bfwg/springboot-jwt-starter
 */
@Service
public class AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private TokenService tokenService;

    @Autowired
    public AuthenticationSuccessHandler(TokenService tokenService) {
        this.tokenService = tokenService;
    }


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        clearAuthenticationAttributes(request);
        Account user = (Account) authentication.getPrincipal();
        tokenService.createTokenCookies(response, user);
    }
}
