package com.qprogramming.gifts.login;

import com.qprogramming.gifts.TestUtil;
import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.AccountService;
import com.qprogramming.gifts.account.AccountType;
import com.qprogramming.gifts.account.authority.AuthorityService;
import com.qprogramming.gifts.config.property.PropertyService;
import com.qprogramming.gifts.login.token.TokenService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.social.facebook.api.User;
import org.springframework.social.facebook.api.UserOperations;
import org.springframework.social.facebook.api.impl.FacebookTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;

/**
 * Created by XE050991499 on 2017-03-14.
 */
public class OAuthLoginSuccessHandlerTest {

    Map<String, String> details;
    @Mock
    private AccountService accSrvMock;
    @Mock
    private FacebookTemplate facebookTemplateMock;
    @Mock
    private UserOperations userOperationsMock;
    @Mock
    private OAuth2Authentication authMock;
    @Mock
    private Authentication authenticationMock;
    @Mock
    private OAuth2AuthenticationDetails oauthDetailsMock;
    @Mock
    private HttpServletResponse responseMock;
    @Mock
    private HttpServletRequest requestMock;
    @Mock
    private TokenService tokenServiceMock;
    @Mock
    private PropertyService propertyServiceMock;
    @Mock
    private AuthorityService authorityServiceMock;

    private OAuthLoginSuccessHandler handler;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        details = new HashMap<>();
        when(authMock.getUserAuthentication()).thenReturn(authenticationMock);
        when((OAuth2AuthenticationDetails) authMock.getDetails()).thenReturn(oauthDetailsMock);
        when(authenticationMock.getDetails()).thenReturn(details);
        when(propertyServiceMock.getLanguages()).thenReturn(languageList());
        handler = spy(new OAuthLoginSuccessHandler(accSrvMock, tokenServiceMock, authorityServiceMock));
    }

    @Test
    public void onAuthenticationSuccessGoogleUserExists() throws Exception {
        Account testAccount = TestUtil.createAccount();
        when(accSrvMock.findByEmail(TestUtil.EMAIL)).thenReturn(Optional.of(testAccount));
        details.put(OAuthLoginSuccessHandler.G.SUB, TestUtil.USER_RANDOM_ID);
        details.put(OAuthLoginSuccessHandler.EMAIL, TestUtil.EMAIL);
        handler.onAuthenticationSuccess(requestMock, responseMock, authMock);
        verify(accSrvMock, times(1)).signin(testAccount);
    }

    @Test
    public void onAuthenticationSuccessGoogleUserCreated() throws Exception {
        Account testAccount = TestUtil.createAccount();
        testAccount.setType(AccountType.GOOGLE);
        details.put(OAuthLoginSuccessHandler.G.SUB, TestUtil.USER_RANDOM_ID);
        details.put(OAuthLoginSuccessHandler.G.GIVEN_NAME, testAccount.getName());
        details.put(OAuthLoginSuccessHandler.G.FAMILY_NAME, testAccount.getSurname());
        details.put(OAuthLoginSuccessHandler.EMAIL, TestUtil.EMAIL);
        details.put(OAuthLoginSuccessHandler.LOCALE, "en");
        details.put(OAuthLoginSuccessHandler.G.PICTURE, "link");
        when(accSrvMock.createAcount(any(Account.class))).thenReturn(testAccount);
        when(accSrvMock.generateID()).thenReturn(TestUtil.USER_RANDOM_ID);
        handler.onAuthenticationSuccess(requestMock, responseMock, authMock);
        verify(accSrvMock, times(1)).createAcount(any(Account.class));
        verify(accSrvMock, times(1)).signin(testAccount);
    }

    @Test
    public void onAuthenticationSuccessFacebookUserExists() throws Exception {
        Account testAccount = TestUtil.createAccount();
        when(accSrvMock.findByEmail(TestUtil.EMAIL)).thenReturn(Optional.of(testAccount));
        doReturn(facebookTemplateMock).when(handler).getFacebookTemplate(anyString());
        String[] fields = {OAuthLoginSuccessHandler.FB.ID, OAuthLoginSuccessHandler.EMAIL
                , OAuthLoginSuccessHandler.FB.FIRST_NAME, OAuthLoginSuccessHandler.FB.LAST_NAME
                , OAuthLoginSuccessHandler.LOCALE};
        User fbUser = spy(new User(testAccount.getId(), testAccount.getFullname()
                , testAccount.getName(), testAccount.getSurname()
                , "Male", new Locale("fr")));
        when(fbUser.getEmail()).thenReturn(testAccount.getEmail());
        when(facebookTemplateMock.fetchObject(OAuthLoginSuccessHandler.FB.ME, User.class, fields)).thenReturn(fbUser);
        when(facebookTemplateMock.userOperations()).thenReturn(userOperationsMock);
        details.put(OAuthLoginSuccessHandler.FB.ID, TestUtil.USER_RANDOM_ID);
        //TODO this connects to FB , we don't need to do this in test?
//        handler.onAuthenticationSuccess(requestMock, responseMock, authMock);
//        verify(accSrvMock, times(1)).signin(testAccount);
    }

    @Test
    public void onAuthenticationSuccessFacebookUserCreated() throws Exception {
        Account testAccount = TestUtil.createAccount();
        testAccount.setType(AccountType.FACEBOOK);
        details.put(OAuthLoginSuccessHandler.FB.ID, TestUtil.USER_RANDOM_ID);
        doReturn(facebookTemplateMock).when(handler).getFacebookTemplate(anyString());
        String[] fields = {OAuthLoginSuccessHandler.FB.ID, OAuthLoginSuccessHandler.EMAIL
                , OAuthLoginSuccessHandler.FB.FIRST_NAME, OAuthLoginSuccessHandler.FB.LAST_NAME
                , OAuthLoginSuccessHandler.LOCALE};
        User fbUser = spy(new User(testAccount.getId(), testAccount.getFullname()
                , testAccount.getName(), testAccount.getSurname()
                , "Male", new Locale("fr")));
        when(oauthDetailsMock.getTokenValue()).thenReturn("RANDOM_TOKEN");
        when(accSrvMock.createAcount(any(Account.class))).thenReturn(testAccount);
        when(fbUser.getEmail()).thenReturn(testAccount.getEmail());
//        doReturn(testAccount.getEmail()).when(fbUser.getEmail());
        when(facebookTemplateMock.fetchObject(OAuthLoginSuccessHandler.FB.ME, User.class, fields)).thenReturn(fbUser);
        when(facebookTemplateMock.userOperations()).thenReturn(userOperationsMock);
        when(userOperationsMock.getUserProfileImage()).thenReturn(new byte[2]);
        when(accSrvMock.createAcount(any(Account.class))).thenReturn(testAccount);
        when(accSrvMock.generateID()).thenReturn(TestUtil.USER_RANDOM_ID);
        when(propertyServiceMock.getDefaultLang()).thenReturn("en");
        handler.onAuthenticationSuccess(requestMock, responseMock, authMock);
        verify(accSrvMock, times(1)).createAcount(any(Account.class));
        verify(accSrvMock, times(1)).signin(testAccount);

    }

    private Map<String, String> languageList() {
        Map<String, String> langs = new HashMap<>();
        langs.put("en", "English");
        langs.put("pl", "Polish");
        return langs;
    }

}