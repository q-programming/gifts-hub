package com.qprogramming.gifts.login;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Jakub Romaniszyn on 2018-10-24
 */
@Component
public class LogoutHandlerImpl implements LogoutHandler {
    private LogoutSuccess logoutSuccessHandler;


    private static final Logger LOG = LoggerFactory.getLogger(LogoutHandlerImpl.class);

    @Autowired
    public LogoutHandlerImpl(LogoutSuccess logoutSuccessHandler) {
        this.logoutSuccessHandler = logoutSuccessHandler;
    }

    @Override
    public void logout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) {
        LOG.debug("logout(..): logging out the user : " + authentication.getName());
        try {
            this.logoutSuccessHandler.onLogoutSuccess(httpServletRequest, httpServletResponse, authentication);
        } catch (IOException e) {
            LOG.debug("There were errors while trying to logout {}", e);
        }
    }
}
