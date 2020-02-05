package com.qprogramming.gifts.security.oauth2;

import com.qprogramming.gifts.TestUtil;
import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.security.TokenService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.AuthenticationException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class OAuth2AuthenticationFailureHandlerTest {

    @Mock
    private TokenService tokenServiceMock;
    @Mock
    private HttpCookieOAuth2AuthorizationRequestRepository repositoryMock;
    @Mock
    private HttpServletResponse responseMock;
    @Mock
    private HttpServletRequest requestMock;


    private OAuth2AuthenticationFailureHandler authFailureHandler;
    private Account testAccount;


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        authFailureHandler = new OAuth2AuthenticationFailureHandler(tokenServiceMock, repositoryMock);
        testAccount = TestUtil.createAccount();
    }


    @Test
    public void onAuthenticationFailure() throws Exception {
        when(requestMock.getContextPath()).thenReturn("/");
        when(tokenServiceMock.getCookie(any(HttpServletRequest.class), anyString())).thenReturn(Optional.of(new Cookie("name", "value")));
        when(responseMock.encodeRedirectURL(anyString())).then(returnsFirstArg());
        authFailureHandler.onAuthenticationFailure(requestMock, responseMock, new TestException("failed"));
        String result = "/value?error=failed";
        verify(responseMock, times(1)).sendRedirect(result);
    }


    class TestException extends AuthenticationException {
        public TestException(String msg) {
            super(msg);
        }
    }
}