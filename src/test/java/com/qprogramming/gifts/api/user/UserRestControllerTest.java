package com.qprogramming.gifts.api.user;

import com.qprogramming.gifts.MockSecurityContext;
import com.qprogramming.gifts.TestUtil;
import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.AccountService;
import com.qprogramming.gifts.account.RegisterForm;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by Khobar on 05.03.2017.
 */
public class UserRestControllerTest {

    public static final String API_USER_REGISTER = "/api/user/register";
    public static final String API_USER_LANGUAGE = "/api/user/language";
    private MockMvc userRestCtrl;
    @Mock
    private AccountService accSrvMock;
    @Mock
    private MockSecurityContext securityMock;
    @Mock
    private Authentication authMock;


    private Account testAccount;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        UserRestController userCtrl = new UserRestController(accSrvMock);
        testAccount = TestUtil.createAccount();
        when(securityMock.getAuthentication()).thenReturn(authMock);
        when(authMock.getPrincipal()).thenReturn(testAccount);
        SecurityContextHolder.setContext(securityMock);
        this.userRestCtrl = MockMvcBuilders.standaloneSetup(userCtrl).build();
    }

    @Test
    public void registerSuccess() throws Exception {
        RegisterForm form = new RegisterForm();
        form.setName("Name");
        form.setSurname("Surname");
        userRestCtrl.perform(post(API_USER_REGISTER).contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(form)))
                .andExpect(status().isOk());
    }

    @Test
    public void registerEmailInvalid() throws Exception {
        RegisterForm form = new RegisterForm();
        form.setEmail("notvalid");
        userRestCtrl.perform(post(API_USER_REGISTER).contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(form)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void registerEmailUsed() throws Exception {
        RegisterForm form = new RegisterForm();
        form.setEmail("email@mail.com");
        userRestCtrl.perform(post(API_USER_REGISTER).contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(form)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void registerPasswordNotMatch() throws Exception {
        RegisterForm form = new RegisterForm();
        form.setPassword("password");
        form.setConfirmpassword("password2");
        userRestCtrl.perform(post(API_USER_REGISTER).contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(form)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void registerPasswordTooWeak() throws Exception {
        RegisterForm form = new RegisterForm();
        form.setPassword("password");
        form.setConfirmpassword("password");
        userRestCtrl.perform(post(API_USER_REGISTER).contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(form)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void languageChangedForUser() throws Exception {
        when(accSrvMock.findById(TestUtil.USER_RANDOM_ID)).thenReturn(testAccount);
        JSONObject object = new JSONObject();
        object.put("id", TestUtil.USER_RANDOM_ID);
        object.put("language", "pl");
        userRestCtrl.perform(post(API_USER_LANGUAGE)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(object.toString())).andExpect(status().isOk());
        verify(accSrvMock, times(1)).update(testAccount);
    }

    @Test
    public void languageChangedButNoUserFound() throws Exception {
        JSONObject object = new JSONObject();
        object.put("id", TestUtil.USER_RANDOM_ID);
        object.put("language", "pl");
        userRestCtrl.perform(post(API_USER_LANGUAGE)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(object.toString())).andExpect(status().isNotFound());
    }


    @Test
    public void validateEmail() throws Exception {
    }

    @Test
    public void user() throws Exception {
    }

}