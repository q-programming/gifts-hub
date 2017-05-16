package com.qprogramming.gifts.api.messages;

import com.qprogramming.gifts.MockSecurityContext;
import com.qprogramming.gifts.TestUtil;
import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.messages.MessagesService;
import com.qprogramming.gifts.messages.ResourceMessageBundle;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Locale;
import java.util.Properties;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by Khobar on 17.04.2017.
 */
public class MessagesRestControllerTest {

    public static final String LANGUAGE = "pl";
    public static final String SOME_VALUE = "Some value";
    public static final String DEFAULT_LANG_KEY = "default.lang";
    public static final String OTHER_KEY = "other.key";
    @Mock
    private ResourceMessageBundle messageBundleMock;
    @Mock
    private MockSecurityContext securityMock;
    @Mock
    private Authentication authMock;
    private MockMvc messagesRestCtrl;
    private MessagesService messagesService;
    private MessagesRestController controller;
    private Account testAccount;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        testAccount = TestUtil.createAccount();
        when(securityMock.getAuthentication()).thenReturn(authMock);
        when(authMock.getPrincipal()).thenReturn(testAccount);
        SecurityContextHolder.setContext(securityMock);
        messagesService = new MessagesService(messageBundleMock);
        controller = new MessagesRestController(messagesService);
        this.messagesRestCtrl = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void listLangPL() throws Exception {
        Locale locale = new Locale(LANGUAGE);
        Properties messages = new Properties();
        messages.setProperty(DEFAULT_LANG_KEY, LANGUAGE);
        messages.setProperty(OTHER_KEY, SOME_VALUE);
        when(messageBundleMock.getMessages(locale)).thenReturn(messages);
        MvcResult mvcResult = messagesRestCtrl.perform(get("/api/messages?lang=pl")).andExpect(status().isOk()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        Properties result = TestUtil.convertJsonToObject(contentAsString, Properties.class);
        assertNotNull(result);
        assertEquals(LANGUAGE, result.get(DEFAULT_LANG_KEY));
    }

    @Test
    public void listAccountLang() throws Exception {
        testAccount.setLanguage("en");
        Locale locale = new Locale(testAccount.getLanguage());
        Properties messages = new Properties();
        messages.setProperty(DEFAULT_LANG_KEY, LANGUAGE);
        messages.setProperty(OTHER_KEY, SOME_VALUE);
        when(messageBundleMock.getMessages(locale)).thenReturn(messages);
        MvcResult mvcResult = messagesRestCtrl.perform(get("/api/messages")).andExpect(status().isOk()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        Properties result = TestUtil.convertJsonToObject(contentAsString, Properties.class);
        assertNotNull(result);
        assertEquals(SOME_VALUE, result.get(OTHER_KEY));
    }
}