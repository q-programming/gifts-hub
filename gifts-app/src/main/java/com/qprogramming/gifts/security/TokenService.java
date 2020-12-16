package com.qprogramming.gifts.security;

import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.AccountService;
import com.qprogramming.gifts.support.TimeProvider;
import io.jsonwebtoken.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * @author Jakub Romaniszyn
 * <p>
 * Service to handle all operations with JWT token, and cookies related to it
 */
@Service
public class TokenService {

    @Value("${app.name}")
    private String APP_NAME;
    @Value("${jwt.secret}")
    private String SECRET;
    @Value("${jwt.expires_in}")
    private int EXPIRES_IN;
    @Value("${jwt.cookie}")
    private String TOKEN_COOKIE;
    @Value("${jwt.user_cookie}")
    private String USER_COOKIE;
    @Value("${jwt.header}")
    private String AUTH_HEADER;
    @Value("${jwt.cookie}")
    private String AUTH_COOKIE;

    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);
    private static final SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS512;
    private static final String BEARER = "Bearer ";
    private String contextPath;

    private final ServletContext _servletContext;
    private final TimeProvider _timeProvider;
    private final AccountService _accountService;

    @Autowired
    public TokenService(ServletContext servletContext, TimeProvider timeProvider, AccountService accountService) {
        _servletContext = servletContext;
        _timeProvider = timeProvider;
        _accountService = accountService;
    }

    /**
     * Creates token for authentication
     *
     * @param authentication auth data containing principal of type {@link Account}
     * @return token
     */
    public String createToken(Authentication authentication) {
        Account account = (Account) authentication.getPrincipal();
        return generateToken(account.getEmail());
    }

    /**
     * Retrieves Token from cookies, if nothing is found there, searches for auth header
     */
    public String getToken(HttpServletRequest request) {
        Optional<Cookie> optionalCookie = getCookie(request, AUTH_COOKIE);
        if (optionalCookie.isPresent()) {
            return optionalCookie.get().getValue();
        } else {
            String authHeader = request.getHeader(AUTH_HEADER);
            if (authHeader != null && authHeader.startsWith(BEARER)) {
                return authHeader.substring(7);
            }
            return null;
        }
    }

    /**
     * Generate token for email
     *
     * @param email email for which token will be generated
     * @return generated token
     */
    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuer(APP_NAME)
                .setIssuedAt(new Date())
                .setExpiration(generateExpirationDate())
                .signWith(SIGNATURE_ALGORITHM, SECRET)
                .compact();
    }

    /**
     * Retrieves username ( email) from token
     */
    public String getUserIdFromToken(String token) {
        try {
            final Claims claims = getClaimsFromToken(token);
            return claims.getSubject();
        } catch (ExpiredJwtException e) {
            logger.debug("Token has expired, {}", e.getMessage());
            return null;
        }
    }

    /**
     * Validates if passed token is valid
     *
     * @param authToken token to be verified
     * @return true if valid, false if there was exception during parsing
     */
    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(SECRET).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            logger.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty.");
        }
        return false;
    }

    /**
     * Refreshes current token replacing it with newer
     *
     * @param token token to be refreshed
     * @return refreshed token, null if there were error during refreshing token
     */
    public String refreshToken(String token) {
        String refreshedToken;
        try {
            final Claims claims = getClaimsFromToken(token);
            claims.setIssuedAt(generateCurrentDate());
            refreshedToken = generateToken(claims);
        } catch (Exception e) {
            logger.debug("Failed to refresh token: {}", e.getMessage());
            refreshedToken = null;
        }
        return refreshedToken;
    }

    private String generateToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(generateExpirationDate())
                .signWith(SIGNATURE_ALGORITHM, SECRET)
                .compact();
    }

    /**
     * Checks if token can be retrieved. Also checks if username for which token was created exists in database
     *
     * @param token token to be checked
     * @return true if token can be refreshed
     */
    public Boolean canTokenBeRefreshed(String token) {
        try {
            final Date expirationDate = getClaimsFromToken(token).getExpiration();
            String username = getUserIdFromToken(token);
            UserDetails userDetails = _accountService.loadUserByUsername(username);
            return expirationDate.compareTo(generateCurrentDate()) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Adds cookie with serialized value
     *
     * @see #addCookie(HttpServletResponse, String, String, int)
     */
    public void addSerializedCookie(HttpServletResponse response, String name, Object value, int maxAge) {
        addCookie(response, name, serialize(value), maxAge);
    }

    /**
     * Adds cookie with name and value , and set's max age for it. Path will always be set as well to correct context one
     */
    public void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath(getPath());
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    /**
     * Adds token cookies for account
     * First token is generated and then it's written into cookie
     *
     * @see #addTokenCookies(HttpServletResponse, Account, String)
     */
    public void addTokenCookies(HttpServletResponse response, Account account) {
        String tokenValue = generateToken(account.getEmail());
        addTokenCookies(response, account, tokenValue);
    }

    /**
     * Creates cookies {@link #TOKEN_COOKIE} and {@link #USER_COOKIE} with token and user
     * Both cookies are created for context path and with max age defined in {@link #EXPIRES_IN}
     */
    public void addTokenCookies(HttpServletResponse response, Account account, String tokenValue) {
        Cookie authCookie = new Cookie(TOKEN_COOKIE, (tokenValue));
        authCookie.setPath(getPath());
        authCookie.setHttpOnly(true);
        authCookie.setMaxAge(EXPIRES_IN);
        Cookie userCookie = new Cookie(USER_COOKIE, (account.getId()));
        userCookie.setPath(getPath());
        userCookie.setMaxAge(EXPIRES_IN);
        response.addCookie(authCookie);
        response.addCookie(userCookie);

    }

    /**
     * Regenerate cookies. Essentially replaces the old cookie with new authToken and expiration date
     *
     * @param authToken token to be stored in cookie
     * @param response  HttpResponse to write cookie to
     */
    public void refreshCookie(String authToken, HttpServletResponse response) {
        Cookie authCookie = new Cookie(TOKEN_COOKIE, authToken);
        authCookie.setPath(getPath());
        authCookie.setHttpOnly(true);
        authCookie.setMaxAge(EXPIRES_IN);
        // Add cookie to response
        response.addCookie(authCookie);
    }

    /**
     * Invalidates cookies auth cookie by removing it
     *
     * @param request
     * @param response
     */
    public void invalidateTokenCookie(HttpServletRequest request, HttpServletResponse response) {
        deleteCookie(request, response, TOKEN_COOKIE);
    }

    /**
     * Retrieves cookie with name  from request
     *
     * @param request HttpRequest to search all cookies
     * @param name    name of cookie to be retrieved
     * @return Optional.of(Cookie) if cookie with that name is present in request
     */
    public Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            return Arrays.stream(cookies).filter(cookie -> cookie.getName().equals(name)).findFirst();
        }
        return Optional.empty();
    }

    /**
     * Returns deserialize cookie with given name ( if present)
     *
     * @param request HttpRequest to retrieve cookie
     * @param name    name of cookie value searched
     * @return Authorization request from cookie, null if not present
     */
    public OAuth2AuthorizationRequest getDeserializedCookie(HttpServletRequest request, String name) {
        return getCookie(request, name)
                .map(cookie -> deserialize(cookie, OAuth2AuthorizationRequest.class))
                .orElse(null);
    }


    /**
     * Delete cookie with name, by replacing it with same cookie but empty value and max age 0
     */
    public void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        Optional<Cookie> optionalCookie = getCookie(request, name);
        optionalCookie.ifPresent(cookie -> {
            cookie.setValue("");
            cookie.setPath(getPath());
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        });
        Cookie[] cookies = request.getCookies();
    }


    private String getPath() {
        if (StringUtils.isBlank(contextPath)) {
            String path = _servletContext.getContextPath();
            contextPath = StringUtils.isEmpty(path) ? "/" : path;
        }
        return contextPath;
    }

    private Claims getClaimsFromToken(String token) throws ExpiredJwtException {
        return Jwts.parser()
                .setSigningKey(this.SECRET)
                .parseClaimsJws(token)
                .getBody();
    }

    private long getCurrentTimeMillis() {
        return _timeProvider.getCurrentTimeMillis();
    }

    private Date generateCurrentDate() {
        return new Date(getCurrentTimeMillis());
    }

    private Date generateExpirationDate() {
        return new Date(getCurrentTimeMillis() + this.EXPIRES_IN * 1000);
    }

    private String serialize(Object object) {
        return Base64.getUrlEncoder()
                .encodeToString(SerializationUtils.serialize(object));
    }

    private <T> T deserialize(Cookie cookie, Class<T> cls) {
        return cls.cast(SerializationUtils.deserialize(
                Base64.getUrlDecoder().decode(cookie.getValue())));
    }


}
