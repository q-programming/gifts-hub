package com.qprogramming.gifts.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Jakub Romaniszyn on 19.07.2018.
 */
public class AuthUtils {
    private static final String ROOT_MATCHER = "/";
    private static final String FAVICON_MATCHER = "/favicon.ico";
    private static final String HTML_MATCHER = "/**/*.html";
    private static final String CSS_MATCHER = "/**/*.css";
    private static final String JS_MATCHER = "/**/*.js";
    private static final String FONT_MATCHER = "/**/*.woff2";

    private static final String IMG_MATCHER = "/assets/*";
    private static final String LOGIN_MATCHER = "/login";
    private static final String LOGOUT_MATCHER = "/logout";

    private static List<String> PATHS_TO_SKIP = Arrays.asList(
            ROOT_MATCHER,
            HTML_MATCHER,
            FAVICON_MATCHER,
            CSS_MATCHER,
            JS_MATCHER,
            IMG_MATCHER,
            LOGIN_MATCHER,
            LOGOUT_MATCHER,
            FONT_MATCHER
    );

    public static boolean skipPathRequest(HttpServletRequest request) {
        Assert.notNull(PATHS_TO_SKIP, "path cannot be null.");
        List<RequestMatcher> m = PATHS_TO_SKIP.stream().map(AntPathRequestMatcher::new).collect(Collectors.toList());
        OrRequestMatcher matchers = new OrRequestMatcher(m);
        return matchers.matches(request);
    }

    /**
     * By default if user is not authenticated or is using
     * authentication is null or it's princiapl is
     *
     * @return true if user correctly previously authenticated in filter chain
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getPrincipal() != null;
    }
}
