package com.qprogramming.gifts.security;

import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.AccountService;
import com.qprogramming.gifts.support.CookieUtils;
import com.qprogramming.gifts.support.TimeProvider;
import io.jsonwebtoken.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Service
public class TokenService {

    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

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

    private String contextPath;
    private ServletContext servletContext;
    private TimeProvider timeProvider;
    private AccountService accountService;

    private static final SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS512;
    private static final String BEARER = "Bearer ";

    @Autowired
    public TokenService(ServletContext servletContext, TimeProvider timeProvider, AccountService accountService) {
        this.servletContext = servletContext;
        this.timeProvider = timeProvider;
        this.accountService = accountService;
    }

    public String createToken(Authentication authentication) {
        Account account = (Account) authentication.getPrincipal();
        return generateToken(account.getEmail());
    }

    public String getToken(HttpServletRequest request) {
        Optional<Cookie> authCookie = CookieUtils.getCookie(request, AUTH_COOKIE);
        if (authCookie.isPresent()) {
            return authCookie.get().getValue();
        }
        String authHeader = request.getHeader(AUTH_HEADER);
        if (authHeader != null && authHeader.startsWith(BEARER)) {
            return authHeader.substring(7);
        }
        return null;
    }

    public String generateToken(String  email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuer(APP_NAME)
                .setIssuedAt(new Date())
                .setExpiration(generateExpirationDate())
                .signWith(SIGNATURE_ALGORITHM, SECRET)
                .compact();
    }
    private String generateToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(generateExpirationDate())
                .signWith(SIGNATURE_ALGORITHM, SECRET)
                .compact();
    }

    public String getUserIdFromToken(String token) {
        final Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }

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


    public Boolean canTokenBeRefreshed(String token) {
        try {
            final Date expirationDate = getClaimsFromToken(token).getExpiration();
            String username = getUserIdFromToken(token);
            UserDetails userDetails = accountService.loadUserByUsername(username);
            return expirationDate.compareTo(generateCurrentDate()) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public String refreshToken(String token) {
        String refreshedToken;
        try {
            final Claims claims = getClaimsFromToken(token);
            claims.setIssuedAt(generateCurrentDate());
            refreshedToken = generateToken(claims);
        } catch (Exception e) {
            refreshedToken = null;
        }
        return refreshedToken;
    }

    public void addCookies(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath(getPath());
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    public void addTokenCookie(HttpServletResponse response, Account account, String tokenValue){
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

    public void refreshCookie(String authToken, HttpServletResponse response) {
        Cookie authCookie = new Cookie(TOKEN_COOKIE, authToken);
        authCookie.setPath(getPath());
        authCookie.setHttpOnly(true);
        authCookie.setMaxAge(EXPIRES_IN);
        // Add cookie to response
        response.addCookie(authCookie);
    }

    public void invalidateCookie(HttpServletResponse response) {
        Cookie authCookie = new Cookie(TOKEN_COOKIE, null);
        authCookie.setPath(getPath());
        authCookie.setHttpOnly(true);
        authCookie.setMaxAge(0);
        response.addCookie(authCookie);
    }

    private String getPath() {
        if (StringUtils.isBlank(contextPath)) {
            String path = servletContext.getContextPath();
            contextPath = StringUtils.isEmpty(path) ? "/" : path;
        }
        return contextPath;
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

    private long getCurrentTimeMillis() {
        return timeProvider.getCurrentTimeMillis();
    }

    private Date generateCurrentDate() {
        return new Date(getCurrentTimeMillis());
    }

    private Date generateExpirationDate() {
        return new Date(getCurrentTimeMillis() + this.EXPIRES_IN * 1000);
    }


}
