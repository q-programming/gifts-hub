package com.qprogramming.gifts.filters;

import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.AccountService;
import com.qprogramming.gifts.login.AnonAuthentication;
import com.qprogramming.gifts.login.token.TokenBasedAuthentication;
import com.qprogramming.gifts.login.token.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private AccountService accountService;
    @Autowired
    private TokenService tokenService;

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (SecurityContextHolder.getContext().getAuthentication() == null || SecurityContextHolder.getContext().getAuthentication() instanceof AnonAuthentication) {
            String authToken = tokenService.getToken(request);
            if (authToken != null) {
                // get username from token
                String username = tokenService.getUsernameFromToken(authToken);
                if (username != null) {
                    Account account = accountService.findByUsername(username);
                    TokenBasedAuthentication authentication = new TokenBasedAuthentication(account);
                    authentication.setToken(authToken);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } else {
                SecurityContextHolder.getContext().setAuthentication(new AnonAuthentication());
            }
        }
        chain.doFilter(request, response);
    }
}
