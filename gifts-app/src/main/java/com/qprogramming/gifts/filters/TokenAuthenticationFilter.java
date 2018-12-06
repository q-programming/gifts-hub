package com.qprogramming.gifts.filters;

import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.AccountService;
import com.qprogramming.gifts.login.AnonAuthentication;
import com.qprogramming.gifts.login.token.TokenBasedAuthentication;
import com.qprogramming.gifts.login.token.TokenService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Based on
 * https://github.com/bfwg/springboot-jwt-starter
 */
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private AccountService accountService;
    private TokenService tokenService;

    public TokenAuthenticationFilter(AccountService accountService, TokenService tokenService) {
        this.accountService = accountService;
        this.tokenService = tokenService;
    }

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!AuthUtils.isAuthenticated()) {
            String authToken = tokenService.getToken(request);
            if (authToken != null && !AuthUtils.skipPathRequest(request)) {
                // get username from token
                try {
                    String username = tokenService.getUsernameFromToken(authToken);
                    // get user
                    Account userDetails = accountService.loadUserByUsername(username);
                    // create authentication
                    TokenBasedAuthentication authentication = new TokenBasedAuthentication(userDetails);
                    authentication.setToken(authToken);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } catch (Exception e) {
                    SecurityContextHolder.getContext().setAuthentication(new AnonAuthentication());
                }
            } else {
                SecurityContextHolder.getContext().setAuthentication(new AnonAuthentication());
            }
        }
        chain.doFilter(request, response);
    }
}
