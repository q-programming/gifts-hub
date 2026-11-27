package com.qprogramming.gifts.security.oauth2;

import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.AccountService;
import com.qprogramming.gifts.config.mail.MailService;
import com.qprogramming.gifts.config.property.PropertyService;
import com.qprogramming.gifts.exceptions.OAuth2AuthenticationProcessingException;
import com.qprogramming.gifts.security.TokenService;
import com.qprogramming.gifts.settings.Settings;
import com.qprogramming.gifts.support.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import javax.mail.MessagingException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * Verifies kid's Google account when kid account is converted to regular one. Kid is not signed in,
 * after conversion regular login flow is used. Independent from generic OAuth2 login
 */
@Slf4j
@Service
public class KidConvertGoogleService {

    private static final String TOKEN_COOKIE = "kid_convert_token";
    private static final String REDIRECT_COOKIE = "kid_convert_redirect";
    private static final int COOKIE_SECONDS = 600;
    private static final String CALLBACK_PATH = "/oauth2/kid-convert/callback";
    private static final String RESPONSE_TYPE = "response_type";
    private static final String CLIENT_ID = "client_id";
    private static final String SCOPE = "scope";
    private static final String REDIRECT_URI = "redirect_uri";
    private static final String STATE = "state";
    private static final String GOOGLE = "google";
    private static final String ERROR = "error";
    private static final String CONVERTED = "converted";
    private static final String EMAIL = "email";

    private final ClientRegistrationRepository _clientRegistrationRepository;
    private final TokenService _tokenService;
    private final AccountService _accountService;
    private final MailService _mailService;

    private final PropertyService _propertyService;

    public KidConvertGoogleService(ClientRegistrationRepository clientRegistrationRepository, TokenService tokenService,
                                   AccountService accountService, MailService mailService, PropertyService propertyService) {
        this._propertyService = propertyService;
        this._clientRegistrationRepository = clientRegistrationRepository;
        this._tokenService = tokenService;
        this._accountService = accountService;
        this._mailService = mailService;
    }

    /**
     * Keeps invite token and return address in cookies and sends kid to Google. Invalid invites return straight to landing page
     */
    public void start(String token, String redirectUri, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!isLocalRedirect(redirectUri, request)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        if (_accountService.findValidKidConvertEvent(token).isEmpty()) {
            response.sendRedirect(redirect(redirectUri, ERROR, AccountService.KID_CONVERT_INVALID));
            return;
        }
        _tokenService.addCookie(response, TOKEN_COOKIE, token, COOKIE_SECONDS);
        _tokenService.addCookie(response, REDIRECT_COOKIE, redirectUri, COOKIE_SECONDS);
        ClientRegistration google = _clientRegistrationRepository.findByRegistrationId(GOOGLE);
        response.sendRedirect(UriComponentsBuilder.fromUriString(google.getProviderDetails().getAuthorizationUri())
                .queryParam(RESPONSE_TYPE, "code")
                .queryParam(CLIENT_ID, google.getClientId())
                .queryParam(SCOPE, String.join(" ", google.getScopes()))
                .queryParam(REDIRECT_URI, callbackUri(request))
                .queryParam(STATE, token)
                .build().encode().toUriString());
    }

    /**
     * Handles Google callback. Verifies Google account, converts kid account and returns to landing page
     */
    public void complete(String code, String state, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String redirectUri = _tokenService.getCookie(request, REDIRECT_COOKIE).map(Cookie::getValue)
                .filter(uri -> isLocalRedirect(uri, request)).orElse("/");
        String token = _tokenService.getCookie(request, TOKEN_COOKIE).map(Cookie::getValue).orElse(null);
        _tokenService.deleteCookie(request, response, TOKEN_COOKIE);
        _tokenService.deleteCookie(request, response, REDIRECT_COOKIE);
        if (code == null || token == null || !token.equals(state)) {
            response.sendRedirect(redirect(redirectUri, ERROR, AccountService.KID_CONVERT_INVALID));
            return;
        }
        try {
            Account converted = _accountService.convertKidAccountWithGoogle(token, loadGoogleEmail(code, state, request));
            sendConfirmation(converted);
            response.sendRedirect(redirect(redirectUri, CONVERTED, "true"));
        } catch (OAuth2AuthenticationProcessingException e) {
            response.sendRedirect(redirect(redirectUri, ERROR, e.getMessage()));
        } catch (OAuth2AuthenticationException e) {
            log.error("Kid conversion with Google failed", e);
            response.sendRedirect(redirect(redirectUri, ERROR, GOOGLE));
        }
    }

    private String loadGoogleEmail(String code, String state, HttpServletRequest request) {
        ClientRegistration google = _clientRegistrationRepository.findByRegistrationId(GOOGLE);
        String callback = callbackUri(request);
        OAuth2AuthorizationRequest authorizationRequest = OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri(google.getProviderDetails().getAuthorizationUri()).clientId(google.getClientId())
                .redirectUri(callback).state(state).build();
        OAuth2AuthorizationResponse authorizationResponse = OAuth2AuthorizationResponse.success(code).redirectUri(callback).state(state).build();
        OAuth2AccessTokenResponse tokenResponse = new DefaultAuthorizationCodeTokenResponseClient().getTokenResponse(
                new OAuth2AuthorizationCodeGrantRequest(google, new OAuth2AuthorizationExchange(authorizationRequest, authorizationResponse)));
        OAuth2User googleUser = new DefaultOAuth2UserService().loadUser(new OAuth2UserRequest(google, tokenResponse.getAccessToken()));
        return Boolean.TRUE.equals(googleUser.getAttribute("email_verified")) ? googleUser.getAttribute(EMAIL) : null;
    }

    // return address has to belong to this application, otherwise endpoint could be used as open redirect
    boolean isLocalRedirect(String redirectUri, HttpServletRequest request) {
        if (redirectUri == null) {
            return false;
        }
        String application = _propertyService.getProperty(Settings.APP_URL);
        if (application != null && !application.isEmpty() && redirectUri.startsWith(application)) {
            return true;
        }
        try {
            String host = URI.create(redirectUri).getHost();
            return host != null && host.equals(request.getServerName());
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private void sendConfirmation(Account converted) {
        try {
            _mailService.sendKidConverted(Utils.createMail(converted));
        } catch (MessagingException | UnsupportedEncodingException | MailException e) {
            log.error("Failed to send kid conversion confirmation", e);
        }
    }

    private String callbackUri(HttpServletRequest request) {
        return ServletUriComponentsBuilder.fromContextPath(request).path(CALLBACK_PATH).build().toUriString();
    }

    // landing page uses hash routing, so the result is added to the route, after '#'
    String redirect(String redirectUri, String param, String value) {
        int fragment = redirectUri.indexOf('#');
        if (fragment >= 0) {
            String separator = redirectUri.indexOf('?', fragment) >= 0 ? "&" : "?";
            return redirectUri + separator + param + "=" + UriUtils.encode(value, StandardCharsets.UTF_8);
        }
        return UriComponentsBuilder.fromUriString(redirectUri).queryParam(param, value).build().toUriString();
    }
}
