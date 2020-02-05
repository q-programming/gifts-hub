package com.qprogramming.gifts.security.oauth2;

import com.qprogramming.gifts.TestUtil;
import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.security.TokenService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

import static com.qprogramming.gifts.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class OAuth2AuthenticationSuccessHandlerTest {

    @Mock
    private TokenService tokenServiceMock;
    @Mock
    private HttpCookieOAuth2AuthorizationRequestRepository repositoryMock;
    @Mock
    private HttpServletResponse responseMock;
    @Mock
    private HttpServletRequest requestMock;


    private OAuth2AuthenticationSuccessHandler authSuccessHandler;
    private Account testAccount;


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        authSuccessHandler = new OAuth2AuthenticationSuccessHandler(tokenServiceMock, repositoryMock);
        testAccount = TestUtil.createAccount();
    }


    @Test
    public void onAuthenticationSuccess() throws Exception {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(testAccount, "credentials");
        when(requestMock.getContextPath()).thenReturn("/");
        String url = "http://localhost";
        when(tokenServiceMock.getCookie(any(HttpServletRequest.class), anyString())).thenReturn(Optional.of(new Cookie(REDIRECT_URI_PARAM_COOKIE_NAME, url)));
        when(responseMock.encodeRedirectURL(anyString())).then(returnsFirstArg());
        authSuccessHandler.onAuthenticationSuccess(requestMock, responseMock, authentication);
        verify(responseMock, times(1)).sendRedirect(url);
    }


    class TestException extends AuthenticationException {
        public TestException(String msg) {
            super(msg);
        }
    }
}