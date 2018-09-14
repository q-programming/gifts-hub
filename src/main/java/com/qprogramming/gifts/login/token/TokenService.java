package com.qprogramming.gifts.login.token;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qprogramming.gifts.account.Account;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

/**
 * Based on
 * https://github.com/bfwg/springboot-jwt-starter
 */
@Service
public class TokenService {
    private static final String BEARER = "Bearer ";
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
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

    private ObjectMapper objectMapper;
    private ServletContext servletContext;

    private SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS512;

    @Autowired
    public TokenService(ObjectMapper objectMapper, ServletContext servletContext) {
        this.objectMapper = objectMapper;
        this.servletContext = servletContext;
    }

    public String getUsernameFromToken(String token) {
        String username;
        try {
            final Claims claims = this.getClaimsFromToken(token);
            username = claims.getSubject();
        } catch (Exception e) {
            username = null;
        }
        return username;
    }

    public void createTokenCookies(HttpServletResponse response, Account account) throws IOException {
        String tokenValue = generateToken(account.getUsername());
        Cookie authCookie = new Cookie(TOKEN_COOKIE, (tokenValue));
        authCookie.setPath(getPath());
        authCookie.setHttpOnly(true);
        authCookie.setMaxAge(EXPIRES_IN);
        Cookie userCookie = new Cookie(USER_COOKIE, (account.getId()));
        userCookie.setPath(getPath());
        userCookie.setMaxAge(EXPIRES_IN);
        response.addCookie(authCookie);
        response.addCookie(userCookie);
        UserTokenState userTokenState = new UserTokenState(tokenValue, EXPIRES_IN);
        String jwtResponse = objectMapper.writeValueAsString(userTokenState);
        response.setContentType("application/json");
        response.getWriter().write(jwtResponse);
    }

    String generateToken(String username) {
        return Jwts.builder()
                .setIssuer(APP_NAME)
                .setSubject(username)
                .setIssuedAt(generateCurrentDate())
                .setExpiration(generateExpirationDate())
                .signWith(SIGNATURE_ALGORITHM, SECRET)
                .compact();
    }

    private Claims getClaimsFromToken(String token) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(this.SECRET)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            claims = null;
        }
        return claims;
    }

    public String getToken(HttpServletRequest request) {
        Cookie authCookie = getCookieValueByName(request, AUTH_COOKIE);
        if (authCookie != null) {
            return authCookie.getValue();
        }
        String authHeader = request.getHeader(AUTH_HEADER);
        if (authHeader != null && authHeader.startsWith(BEARER)) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * Find a specific HTTP cookie in a request.
     *
     * @param request The HTTP request object.
     * @param name    The cookie name to look for.
     * @return The cookie, or <code>null</code> if not found.
     */
    private Cookie getCookieValueByName(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return null;
        }
        for (int i = 0; i < request.getCookies().length; i++) {
            if (request.getCookies()[i].getName().equals(name)) {
                return request.getCookies()[i];
            }
        }
        return null;
    }


    private long getCurrentTimeMillis() {
        return new DateTime().getMillis();
    }

    private Date generateCurrentDate() {
        return new Date(getCurrentTimeMillis());
    }

    private Date generateExpirationDate() {
        return new Date(getCurrentTimeMillis() + this.EXPIRES_IN * 1000);
    }

    private String getPath() {
        String contextPath = servletContext.getContextPath();
        return StringUtils.isEmpty(contextPath) ? "/" : contextPath;
    }

}
