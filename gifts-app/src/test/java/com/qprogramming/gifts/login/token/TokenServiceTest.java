package com.qprogramming.gifts.login.token;

import com.qprogramming.gifts.TestUtil;
import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.AccountService;
import com.qprogramming.gifts.security.TokenService;
import com.qprogramming.gifts.support.TimeProvider;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by Khobar on 08.04.2017.
 */
public class TokenServiceTest {

    private static final String TOKEN_COOKIE = "token";
    private static final String USER_COOKIE = "user_cookie";
    private static final String AUTH_HEADER = "auth_header";
    private static final String AUTH_COOKIE = "auth_cookie";
    private static final String MY_SECRET = "mySecret";
    private static final String APP = "app";
    private static final int EXPIRES_IN = 3000;
    private static final String COOKIE_1 = "Cookie1";
    private static final String COOKIE_2 = "Cookie2";
    @Mock
    private HttpServletResponse responseMock;
    @Mock
    private HttpServletRequest requestMock;
    @Mock
    private ServletContext servletContextMock;
    @Mock
    private AccountService accountServiceMock;
    @Mock
    private TimeProvider timeProviderMock;


    private TokenService tokenService;
    private Account testAccount;


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        tokenService = new TokenService(servletContextMock, timeProviderMock, accountServiceMock);
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
    public void getUsernameFromToken() {
        when(timeProviderMock.getCurrentTimeMillis())
                .thenReturn(new DateTime().getMillis());
        String token = createToken();
        assertThat(tokenService.getUserIdFromToken(token)).isEqualTo(TestUtil.EMAIL);
    }

    @Test
    public void getUsernameFromExpiredToken() throws Exception {
        ReflectionTestUtils.setField(tokenService, "EXPIRES_IN", -1);
        ReflectionTestUtils.setField(tokenService, "SECRET", "secret");
        String token = tokenService.generateToken(testAccount.getUsername());
        String result = tokenService.getUserIdFromToken(token);
        assertNull(result);
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
    public void validateToken() {
        when(timeProviderMock.getCurrentTimeMillis()).thenReturn(new Date().getTime());
        assertTrue(tokenService.validateToken(createToken()));
    }

    @Test
    public void validateTokenExpired() {
        assertFalse(tokenService.validateToken(createToken()));
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

    @Test
    public void testAddTokenCookies() {
        tokenService.addTokenCookies(responseMock, testAccount);
        verify(responseMock, times(2)).addCookie(any(Cookie.class));
    }


    @Test
    public void tokenCannotBeRefreshedTest() {
        String token = createToken();
        when(accountServiceMock.loadUserByUsername(testAccount.getUsername())).thenThrow(UsernameNotFoundException.class);
        assertFalse(tokenService.canTokenBeRefreshed(token));
    }

    @Test
    public void tokenCanBeRefreshedTest() {
        ReflectionTestUtils.setField(tokenService, "EXPIRES_IN", 5000);
        when(timeProviderMock.getCurrentTimeMillis()).thenReturn(new Date().getTime());
        String token = createToken();
        assertTrue(tokenService.canTokenBeRefreshed(token));
    }


    @Test
    public void refreshTokenSuccess() {
        when(timeProviderMock.getCurrentTimeMillis()).thenReturn(new Date().getTime());
        String token = createToken();
        assertNotNull(tokenService.refreshToken(token));
    }

    @Test
    public void refreshTokenFailed() {
        ReflectionTestUtils.setField(tokenService, "EXPIRES_IN", 5000);
        String token = createToken();
        ReflectionTestUtils.setField(tokenService, "EXPIRES_IN", -1);
        String refreshToken = tokenService.refreshToken(token);
        assertNull(refreshToken);
    }

    @Test
    public void refreshCookieTest() {
        String token = createToken();
        tokenService.refreshCookie(token, responseMock);
        verify(responseMock, times(1)).addCookie(any(Cookie.class));
    }

    @Test
    public void invalidateCookieTest() {
        ReflectionTestUtils.setField(tokenService, "TOKEN_COOKIE", COOKIE_1);
        TestCookie cookie1 = new TestCookie(COOKIE_1, "1");
        TestCookie cookie2 = new TestCookie(COOKIE_2, "2");
        TestCookie resultCookie = new TestCookie(COOKIE_1, "");
        resultCookie.setMaxAge(0);
        resultCookie.setPath("/");
        when(requestMock.getCookies()).thenReturn(new TestCookie[]{cookie1, cookie2});
        tokenService.invalidateTokenCookie(requestMock, responseMock);
        verify(responseMock, times(1)).addCookie(resultCookie);
    }

    @Test
    public void addSerializedCookieAndDeseralized() {
        ArgumentCaptor<Cookie> valueCapture = ArgumentCaptor.forClass(Cookie.class);
        OAuth2AuthorizationRequest authorizationRequest = OAuth2AuthorizationRequest.authorizationCode().clientId("1").redirectUri("/").authorizationUri("http://localhost").build();
        doNothing().when(responseMock).addCookie(valueCapture.capture());
        tokenService.addSerializedCookie(responseMock, COOKIE_1, authorizationRequest, 5000);
        Cookie capturedValue = valueCapture.getValue();
        when(requestMock.getCookies()).thenReturn(new Cookie[]{capturedValue});
        OAuth2AuthorizationRequest deserializedCookie = tokenService.getDeserializedCookie(requestMock, COOKIE_1);
        assertEquals(authorizationRequest.getClientId(), deserializedCookie.getClientId());
        assertEquals(authorizationRequest.getAuthorizationUri(), deserializedCookie.getAuthorizationUri());
    }


    private String createToken() {
        return tokenService.generateToken(TestUtil.EMAIL);
    }

    static class TestCookie extends Cookie {

        TestCookie(String name, String value) {
            super(name, value);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestCookie testCookie = (TestCookie) o;
            return getMaxAge() == testCookie.getMaxAge() &&
                    Objects.equals(this.getName(), testCookie.getName()) &&
                    Objects.equals(getValue(), testCookie.getValue());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getName(), getValue(), getMaxAge());
        }

    }

}