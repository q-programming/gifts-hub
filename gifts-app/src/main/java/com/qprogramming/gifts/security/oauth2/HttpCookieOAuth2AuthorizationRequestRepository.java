package com.qprogramming.gifts.security.oauth2;

import com.nimbusds.oauth2.sdk.util.StringUtils;
import com.qprogramming.gifts.security.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class HttpCookieOAuth2AuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
    public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    public static final String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri";

    private final TokenService _tokenService;
    private static final int cookieExpireSeconds = 180;

    @Autowired
    public HttpCookieOAuth2AuthorizationRequestRepository(TokenService tokenService) {
        _tokenService = tokenService;
    }

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return _tokenService.getDeserializedCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) {
            removeAuthorizationRequestCookies(request, response);
            return;
        }
        _tokenService.addSerializedCookie(response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, authorizationRequest, cookieExpireSeconds);
        String redirectUriAfterLogin = request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME);
        if (StringUtils.isNotBlank(redirectUriAfterLogin)) {
            _tokenService.addCookie(response, REDIRECT_URI_PARAM_COOKIE_NAME, redirectUriAfterLogin, cookieExpireSeconds);
        }
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request) {
        return this.loadAuthorizationRequest(request);
    }

    public void removeAuthorizationRequestCookies(HttpServletRequest request, HttpServletResponse response) {
        _tokenService.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
        _tokenService.deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME);
    }
}
