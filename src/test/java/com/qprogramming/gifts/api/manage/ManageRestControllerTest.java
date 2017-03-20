package com.qprogramming.gifts.api.manage;

import com.qprogramming.gifts.MockSecurityContext;
import com.qprogramming.gifts.TestUtil;
import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.Roles;
import com.qprogramming.gifts.config.property.PropertyService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static com.qprogramming.gifts.api.manage.Settings.APP_DEFAULT_LANG;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by Khobar on 20.03.2017.
 */
public class ManageRestControllerTest {
    private static final String API_APPLICATION_SETTINGS = "/api/manage/settings";
    private MockMvc manageRestController;
    @Mock
    private PropertyService propertyServiceMock;
    @Mock
    private MockSecurityContext securityMock;
    @Mock
    private Authentication authMock;

    private Account testAccount;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        ManageRestController mngCtrl = new ManageRestController(propertyServiceMock);
        testAccount = TestUtil.createAccount();
        when(securityMock.getAuthentication()).thenReturn(authMock);
        when(authMock.getPrincipal()).thenReturn(testAccount);
        SecurityContextHolder.setContext(securityMock);
        this.manageRestController = MockMvcBuilders.standaloneSetup(mngCtrl).build();
    }

    @Test
    public void changeSettings() throws Exception {
        Settings settings = new Settings();
        settings.setLanguage("en");
        manageRestController.perform(
                post(API_APPLICATION_SETTINGS)
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(settings)))
                .andExpect(status().isOk());
        verify(propertyServiceMock, times(1)).update(Settings.APP_DEFAULT_LANG, "en");
    }

    @Test
    public void applicationSettingsBadAuth() throws Exception {
        testAccount.setRole(Roles.ROLE_USER);
        manageRestController.perform(get(API_APPLICATION_SETTINGS)).andExpect(status().isForbidden());
    }

    @Test
    public void applicationSettingsDataRecived() throws Exception {
        testAccount.setRole(Roles.ROLE_ADMIN);
        when(propertyServiceMock.getProperty(APP_DEFAULT_LANG)).thenReturn("en");
        MvcResult mvcResult = manageRestController.perform(get(API_APPLICATION_SETTINGS)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        assertTrue(contentAsString.contains("language"));
    }

}