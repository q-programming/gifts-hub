package com.qprogramming.gifts.config.property;

import com.qprogramming.gifts.MockSecurityContext;
import com.qprogramming.gifts.TestUtil;
import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.messages.MessagesService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Locale;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Created by XE050991499 on 2017-03-20.
 */
public class PropertyServiceTest {


    @Mock
    private PropertyRepository propertyRepositoryMock;
    @Mock
    private MessagesService messagesServiceMock;
    @Mock
    private MockSecurityContext securityMock;
    @Mock
    private Authentication authMock;
    private PropertyService propertyService;
    private Account testAccount;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        testAccount = TestUtil.createAccount();
        when(securityMock.getAuthentication()).thenReturn(authMock);
        when(authMock.getPrincipal()).thenReturn(testAccount);
        SecurityContextHolder.setContext(securityMock);
        propertyService = new PropertyService(propertyRepositoryMock, messagesServiceMock);
    }

    @Test
    public void update() throws Exception {
    }

    @Test
    public void getLanguages() throws Exception {
        when(messagesServiceMock.getMessage(
                anyString()
                , any(Object[].class)
                , anyString()
                , any(Locale.class)))
                .thenReturn(PropertyService.LANG_DEFAULT_MSG);
        when(messagesServiceMock.getMessage("main.language.name"
                , null
                , PropertyService.LANG_DEFAULT_MSG
                , new Locale("pl")))
                .thenReturn("found");
        when(messagesServiceMock.getMessage("main.language.name"
                , null
                , PropertyService.LANG_DEFAULT_MSG
                , new Locale("en")))
                .thenReturn("found");
        Map<String, String> languages = propertyService.getLanguages();
        assertTrue(languages.size() == 2);
    }

}