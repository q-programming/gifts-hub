package com.qprogramming.gifts.login.token;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qprogramming.gifts.TestUtil;
import com.qprogramming.gifts.account.Account;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by Khobar on 08.04.2017.
 */
public class TokenServiceTest {

    public static final String TOKEN_COOKIE = "token";
    public static final String USER_COOKIE = "user_cookie";
    public static final String AUTH_HEADER = "auth_header";
    public static final String AUTH_COOKIE = "auth_cookie";
    public static final String MY_SECRET = "mySecret";
    public static final String APP = "app";
    public static final int EXPIRES_IN = 3000;
    @Mock
    private HttpServletResponse responseMock;
    @Mock
    private HttpServletRequest requestMock;
    @Mock
    private PrintWriter printWriterMock;
    @Mock
    private ServletContext servletContextMock;

    private TokenService tokenService;
    private Account testAccount;


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        ObjectMapper mapper = new ObjectMapper();
        tokenService = new TokenService(mapper, servletContextMock);
        ReflectionTestUtils.setField(tokenService, "APP_NAME", APP);
        ReflectionTestUtils.setField(tokenService, "SECRET", MY_SECRET);
        ReflectionTestUtils.setField(tokenService, "EXPIRES_IN", EXPIRES_IN);
        ReflectionTestUtils.setField(tokenService, "TOKEN_COOKIE", TOKEN_COOKIE);
        ReflectionTestUtils.setField(tokenService, "USER_COOKIE", USER_COOKIE);
        ReflectionTestUtils.setField(tokenService, "AUTH_HEADER", AUTH_HEADER);
        ReflectionTestUtils.setField(tokenService, "AUTH_COOKIE", AUTH_COOKIE);
        testAccount = TestUtil.createAccount();

    }

    @Test
    public void getUsernameFromToken() throws Exception {
        String token = tokenService.generateToken(testAccount.getUsername());
        String result = tokenService.getUsernameFromToken(token);
        assertEquals(testAccount.getUsername(), result);
    }

    @Test
    public void getUsernameFromExpiredToken() throws Exception {
        ReflectionTestUtils.setField(tokenService, "EXPIRES_IN", -1);
        String token = tokenService.generateToken(testAccount.getUsername());
        String result = tokenService.getUsernameFromToken(token);
        assertNull(result);
    }

    @Test
    public void createTokenCookies() throws Exception {
        when(responseMock.getWriter()).thenReturn(printWriterMock);
        tokenService.createTokenCookies(responseMock, testAccount);
        verify(responseMock, times(2)).addCookie(any(Cookie.class));
        verify(printWriterMock, times(1)).write(anyString());


    }

    @Test
    public void getToken() throws Exception {
        String token = tokenService.generateToken(testAccount.getUsername());
        Cookie authCookie = new Cookie(AUTH_COOKIE, (token));
        authCookie.setPath("/");
        authCookie.setHttpOnly(true);
        authCookie.setMaxAge(EXPIRES_IN);
        when(requestMock.getCookies()).thenReturn(new Cookie[]{authCookie});
        String result = tokenService.getToken(requestMock);
        assertEquals(token, result);
    }

    @Test
    public void getTokenNoCookies() throws Exception {
        when(requestMock.getCookies()).thenReturn(new Cookie[]{});
        String result = tokenService.getToken(requestMock);
        assertNull(result);
    }


    @Test
    public void getTokenNotFound() throws Exception {
        String token = tokenService.generateToken(testAccount.getUsername());
        Cookie authCookie = new Cookie(USER_COOKIE, (token));
        authCookie.setPath("/");
        authCookie.setHttpOnly(true);
        authCookie.setMaxAge(EXPIRES_IN);
        when(requestMock.getCookies()).thenReturn(new Cookie[]{authCookie});
        String result = tokenService.getToken(requestMock);
        assertNull(result);
    }


}