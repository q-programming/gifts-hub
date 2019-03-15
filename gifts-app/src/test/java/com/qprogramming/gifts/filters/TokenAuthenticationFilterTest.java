package com.qprogramming.gifts.filters;

import com.qprogramming.gifts.MockSecurityContext;
import com.qprogramming.gifts.TestUtil;
import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.AccountService;
import com.qprogramming.gifts.login.AnonAuthentication;
import com.qprogramming.gifts.login.token.TokenBasedAuthentication;
import com.qprogramming.gifts.login.token.TokenService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.*;

/**
 * Created by Khobar on 08.04.2017.
 */
public class TokenAuthenticationFilterTest {

    public static final String TOKEN = "TOKEN";
    @Mock
    private AccountService accountServiceMock;
    @Mock
    private TokenService tokenServiceMock;
    @Mock
    private HttpServletRequest requestMock;
    @Mock
    private HttpServletResponse responseMock;
    @Mock
    private FilterChain chainMock;
    @Mock
    private MockSecurityContext securityMock;
    @Mock
    private Authentication authMock;
    @Mock
    private AnonAuthentication anonAuthMock;

    private TokenAuthenticationFilter tokenAuthenticationFilter;

    private Account testAccount;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        tokenAuthenticationFilter = new TokenAuthenticationFilter(accountServiceMock, tokenServiceMock);
        SecurityContextHolder.setContext(securityMock);
        testAccount = TestUtil.createAccount();
    }

    @Test
    public void doFilterInternalNoAuth() throws Exception {
        when(securityMock.getAuthentication()).thenReturn(null);
        tokenAuthenticationFilter.doFilterInternal(requestMock, responseMock, chainMock);
        verify(securityMock, times(1)).setAuthentication(any(AnonAuthentication.class));
        verify(tokenServiceMock, times(1)).getToken(requestMock);
    }

    @Test
    public void doFilterInternalAnonAuth() throws Exception {
        when(securityMock.getAuthentication()).thenReturn(anonAuthMock);
        tokenAuthenticationFilter.doFilterInternal(requestMock, responseMock, chainMock);
        verify(securityMock, times(1)).setAuthentication(any(AnonAuthentication.class));
    }

    @Test
    public void doFilterInternalTokenButNoUser() throws Exception {
        when(securityMock.getAuthentication()).thenReturn(null);
        when(tokenServiceMock.getToken(requestMock)).thenReturn(TOKEN);
        when(tokenServiceMock.getUsernameFromToken(TOKEN)).thenReturn(testAccount.getUsername());
        when(requestMock.getServletPath()).thenReturn("/");
        tokenAuthenticationFilter.doFilterInternal(requestMock, responseMock, chainMock);
        verify(securityMock, times(1)).setAuthentication(any(AnonAuthentication.class));
    }

    @Test
    public void doFilterInternalTokenAndUserFound() throws Exception {
        when(securityMock.getAuthentication()).thenReturn(null);
        when(tokenServiceMock.getToken(requestMock)).thenReturn(TOKEN);
        when(tokenServiceMock.getUsernameFromToken(TOKEN)).thenReturn(testAccount.getUsername());
        when(accountServiceMock.loadUserByUsername(testAccount.getUsername())).thenReturn(testAccount);
        when(requestMock.getServletPath()).thenReturn("/");
        when(requestMock.getPathInfo()).thenReturn("accounts");
        tokenAuthenticationFilter.doFilterInternal(requestMock, responseMock, chainMock);
        verify(securityMock, times(1)).setAuthentication(any(TokenBasedAuthentication.class));
    }


}