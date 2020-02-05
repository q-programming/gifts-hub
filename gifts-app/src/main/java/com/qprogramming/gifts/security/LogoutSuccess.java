package com.qprogramming.gifts.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jakub Romaniszyn on 19.07.2018.
 * <p>
 * Based on
 * https://github.com/bfwg/springboot-jwt-starter
 */
@Component
public class LogoutSuccess implements LogoutSuccessHandler {


    private final ObjectMapper _objectMapper;
    private final TokenService _tokenService;

    @Autowired
    public LogoutSuccess(ObjectMapper objectMapper, TokenService tokenService) {
        _objectMapper = objectMapper;
        _tokenService = tokenService;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {
        Map<String, String> result = new HashMap<>();
        result.put("result", "success");
        response.setContentType("application/json");
        response.getWriter().write(_objectMapper.writeValueAsString(result));
        _tokenService.invalidateTokenCookie(request, response);
        response.setStatus(HttpServletResponse.SC_OK);

    }

}