package com.qprogramming.gifts.api.user;

import com.qprogramming.gifts.MockSecurityContext;
import com.qprogramming.gifts.TestUtil;
import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.AccountService;
import com.qprogramming.gifts.account.AccountType;
import com.qprogramming.gifts.account.RegisterForm;
import com.qprogramming.gifts.account.family.Family;
import com.qprogramming.gifts.account.family.FamilyForm;
import com.qprogramming.gifts.account.family.FamilyService;
import com.qprogramming.gifts.account.family.KidForm;
import com.qprogramming.gifts.messages.MessagesService;
import com.qprogramming.gifts.support.ResultData;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import static com.qprogramming.gifts.TestUtil.USER_RANDOM_ID;
import static org.junit.Assert.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserRestControllerTest {
    public static final String API_USER_REGISTER = "/api/user/register";
    public static final String API_USER_LANGUAGE = "/api/user/language";
    public static final String API_USER_VALIDATE_EMAIL = "/api/user/validate-email";
    public static final String API_USER_VALIDATE_USERNAME = "/api/user/validate-username";
    public static final String API_USER_UPDATE_AVATAR = "/api/user/avatar-upload";
    public static final String API_USER_FAMILY_CREATE = "/api/user/family-create";
    public static final String API_USER_FAMILY_UPDATE = "/api/user/family-update";
    public static final String API_USER_KID_ADD = "/api/user/kid-add";
    public static final String API_USER_KID_UPDATE = "/api/user/kid-update";


    private MockMvc userRestCtrl;
    @Mock
    private AccountService accSrvMock;
    @Mock
    private MockSecurityContext securityMock;
    @Mock
    private Authentication authMock;
    @Mock
    private MessagesService msgSrvMock;
    @Mock
    private FamilyService familyServiceMock;

    private Account testAccount;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        UserRestController userCtrl = new UserRestController(accSrvMock, msgSrvMock, familyServiceMock);
        testAccount = TestUtil.createAccount();
        when(securityMock.getAuthentication()).thenReturn(authMock);
        when(authMock.getPrincipal()).thenReturn(testAccount);
        when(msgSrvMock.getMessage(anyString())).thenReturn("MESSAGE");
        SecurityContextHolder.setContext(securityMock);
        this.userRestCtrl = MockMvcBuilders.standaloneSetup(userCtrl).build();
    }

    @Test
    public void registerSuccess() throws Exception {
        when(accSrvMock.create(any(Account.class))).thenReturn(testAccount);
        RegisterForm form = new RegisterForm();
        form.setName(testAccount.getName());
        form.setSurname(testAccount.getSurname());
        form.setUsername(testAccount.getUsername());
        form.setEmail(testAccount.getEmail());
        form.setPassword("PasswordPassword!23");
        form.setConfirmpassword("PasswordPassword!23");
        userRestCtrl.perform(post(API_USER_REGISTER).contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(form)))
                .andExpect(status().isCreated());
        verify(accSrvMock, times(1)).create(any(Account.class));

    }

    @Test
    public void registerFormNotComplete() throws Exception {
        RegisterForm form = new RegisterForm();
        form.setEmail("notvalid");
        userRestCtrl.perform(post(API_USER_REGISTER).contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(form)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void registerEmailUsed() throws Exception {
        RegisterForm form = new RegisterForm();
        form.setName(testAccount.getName());
        form.setSurname(testAccount.getSurname());
        form.setUsername(testAccount.getUsername());
        form.setEmail(testAccount.getEmail());
        form.setPassword("password");
        form.setConfirmpassword("password");
        when(accSrvMock.findByEmail(testAccount.getEmail())).thenReturn(testAccount);
        MvcResult mvcResult = userRestCtrl.perform(post(API_USER_REGISTER).contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(form)))
                .andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        assertTrue(contentAsString.contains(ResultData.Code.ERROR.toString()));
    }

    @Test
    public void registerPasswordNotMatch() throws Exception {
        RegisterForm form = new RegisterForm();
        form.setName(testAccount.getName());
        form.setSurname(testAccount.getSurname());
        form.setUsername(testAccount.getUsername());
        form.setEmail(testAccount.getEmail());
        form.setPassword("password");
        form.setConfirmpassword("password2");
        MvcResult mvcResult = userRestCtrl.perform(post(API_USER_REGISTER).contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(form)))
                .andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        assertTrue(contentAsString.contains(ResultData.Code.ERROR.toString()));
    }

    @Test
    public void registerPasswordTooWeak() throws Exception {
        RegisterForm form = new RegisterForm();
        form.setName(testAccount.getName());
        form.setSurname(testAccount.getSurname());
        form.setUsername(testAccount.getUsername());
        form.setEmail(testAccount.getEmail());
        form.setPassword("password");
        form.setConfirmpassword("password");
        MvcResult mvcResult = userRestCtrl.perform(post(API_USER_REGISTER).contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(form)))
                .andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        assertTrue(contentAsString.contains(ResultData.Code.ERROR.toString()));
    }

    @Test
    public void languageChangedForUser() throws Exception {
        when(accSrvMock.findById(USER_RANDOM_ID)).thenReturn(testAccount);
        JSONObject object = new JSONObject();
        object.put("id", USER_RANDOM_ID);
        object.put("language", "pl");
        userRestCtrl.perform(post(API_USER_LANGUAGE)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(object.toString())).andExpect(status().isOk());
        verify(accSrvMock, times(1)).update(testAccount);
    }

    @Test
    public void languageChangedButNoUserFound() throws Exception {
        JSONObject object = new JSONObject();
        object.put("id", USER_RANDOM_ID);
        object.put("language", "pl");
        userRestCtrl.perform(post(API_USER_LANGUAGE)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(object.toString())).andExpect(status().isNotFound());
    }


    @Test
    public void validateEmailOk() throws Exception {
        MvcResult mvcResult = userRestCtrl.perform(post(API_USER_VALIDATE_EMAIL)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(testAccount.getEmail())).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        assertFalse(contentAsString.contains(ResultData.Code.ERROR.toString()));
    }

    @Test
    public void validateEmailExists() throws Exception {
        when(accSrvMock.findByEmail(testAccount.getEmail())).thenReturn(testAccount);
        MvcResult mvcResult = userRestCtrl.perform(post(API_USER_VALIDATE_EMAIL)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(testAccount.getEmail())).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        assertTrue(contentAsString.contains(ResultData.Code.ERROR.toString()));
    }

    @Test
    public void validateUsernameExists() throws Exception {
        when(accSrvMock.findByUsername(testAccount.getUsername())).thenReturn(testAccount);
        MvcResult mvcResult = userRestCtrl.perform(post(API_USER_VALIDATE_USERNAME)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(testAccount.getUsername())).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        assertTrue(contentAsString.contains(ResultData.Code.ERROR.toString()));
    }

    @Test
    public void validateUsernameOk() throws Exception {
        MvcResult mvcResult = userRestCtrl.perform(post(API_USER_VALIDATE_USERNAME)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(testAccount.getUsername())).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        assertFalse(contentAsString.contains(ResultData.Code.ERROR.toString()));
    }

    @Test
    public void updateAvatar() throws Exception {
        ClassLoader loader = this.getClass().getClassLoader();
        byte[] imgBytes;
        try (InputStream avatarFile = loader.getResourceAsStream("static/images/logo-white.png")) {
            imgBytes = IOUtils.toByteArray(avatarFile);
            String imgStream = Base64.encodeBase64String(imgBytes);
            userRestCtrl.perform(post(API_USER_UPDATE_AVATAR)
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(imgStream)).andExpect(status().isOk());
            verify(accSrvMock, times(1)).updateAvatar(testAccount, imgBytes);
        } catch (IOException e) {
            fail("IOEXception thrown " + e);
        }
    }

    @Test
    public void createFamilyNoMembers() throws Exception {
        FamilyForm form = new FamilyForm();
        Family family = new Family();
        family.setId(1L);
        family.getMembers().add(testAccount);
        family.getAdmins().add(testAccount);
        when(familyServiceMock.createFamily()).thenReturn(family);
        when(familyServiceMock.update(family)).then(returnsFirstArg());
        userRestCtrl.perform(post(API_USER_FAMILY_CREATE)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(form))).andExpect(status().isOk());
        verify(familyServiceMock, times(1)).update(any(Family.class));
    }

    @Test
    public void createFamilyMembersAndAdmins() throws Exception {
        FamilyForm form = new FamilyForm();
        form.setAdmins(Collections.singletonList(USER_RANDOM_ID + "1"));
        form.setMembers(Collections.singletonList(USER_RANDOM_ID + "1"));
        Family family = new Family();
        family.setId(1L);
        family.getMembers().add(testAccount);
        family.getAdmins().add(testAccount);
        Account memberAndAdmin = TestUtil.createAccount("John", "Doe");
        memberAndAdmin.setId(USER_RANDOM_ID + "1");
        when(accSrvMock.findByIds(Collections.singletonList(USER_RANDOM_ID + "1"))).thenReturn(Collections.singletonList(memberAndAdmin));
        when(familyServiceMock.createFamily()).thenReturn(family);
        when(familyServiceMock.update(family)).then(returnsFirstArg());
        MvcResult mvcResult = userRestCtrl.perform(post(API_USER_FAMILY_CREATE)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(form))).andExpect(status().isOk()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        Family result = TestUtil.convertJsonToObject(contentAsString, Family.class);
        verify(familyServiceMock, times(1)).update(any(Family.class));
        assertTrue(result.getMembers().size() == 2);
        assertTrue(result.getAdmins().contains(memberAndAdmin));
    }


    @Test
    public void createFamilyAlreadyExists() throws Exception {
        FamilyForm form = new FamilyForm();
        Family family = new Family();
        family.setId(1L);
        when(familyServiceMock.getFamily(testAccount)).thenReturn(family);
        userRestCtrl.perform(post(API_USER_FAMILY_CREATE)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(form))).andExpect(status().isBadRequest());
    }


    @Test
    public void updateFamilyNotFound() throws Exception {
        FamilyForm form = new FamilyForm();
        Family family = new Family();
        family.setId(1L);
        userRestCtrl.perform(post(API_USER_FAMILY_UPDATE)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(form))).andExpect(status().isNotFound());
    }

    @Test
    public void updateFamilyAddMemberAndAdmin() throws Exception {
        FamilyForm form = new FamilyForm();
        form.setAdmins(Collections.singletonList(USER_RANDOM_ID + "1"));
        form.setMembers(Collections.singletonList(USER_RANDOM_ID + "1"));
        Family family = new Family();
        family.setId(1L);
        family.getMembers().add(testAccount);
        family.getAdmins().add(testAccount);
        Account memberAndAdmin = TestUtil.createAccount("John", "Doe");
        memberAndAdmin.setId(USER_RANDOM_ID + "1");
        when(accSrvMock.findByIds(Collections.singletonList(USER_RANDOM_ID + "1"))).thenReturn(Collections.singletonList(memberAndAdmin));
        when(familyServiceMock.getFamily(testAccount)).thenReturn(family);
        when(familyServiceMock.update(family)).then(returnsFirstArg());
        MvcResult mvcResult = userRestCtrl.perform(post(API_USER_FAMILY_UPDATE)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(form))).andExpect(status().isOk()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        Family result = TestUtil.convertJsonToObject(contentAsString, Family.class);
        verify(familyServiceMock, times(1)).update(any(Family.class));
        assertTrue(result.getMembers().size() == 2);
        assertTrue(result.getAdmins().contains(memberAndAdmin));
    }

    @Test
    public void updateFamilyNotAdmin() throws Exception {
        FamilyForm form = new FamilyForm();
        form.setAdmins(Collections.singletonList(USER_RANDOM_ID + "1"));
        form.setMembers(Collections.singletonList(USER_RANDOM_ID + "1"));
        Family family = new Family();
        family.setId(1L);
        family.getMembers().add(testAccount);
        when(familyServiceMock.getFamily(testAccount)).thenReturn(family);
        userRestCtrl.perform(post(API_USER_FAMILY_UPDATE)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(form))).andExpect(status().isBadRequest());
    }

    @Test
    public void addKidNoFamily() throws Exception {
        KidForm form = new KidForm();
        form.setName("John");
        form.setSurname("Doe");
        form.setUsername("john");
        userRestCtrl.perform(post(API_USER_KID_ADD)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(form))).andExpect(status().isBadRequest());
    }

    @Test
    public void addKidNotFamilyAdmin() throws Exception {
        Family family = new Family();
        family.setId(1L);
        family.getMembers().add(testAccount);
        when(familyServiceMock.getFamily(testAccount)).thenReturn(family);
        KidForm form = new KidForm();
        form.setName("John");
        form.setSurname("Doe");
        form.setUsername("john");
        userRestCtrl.perform(post(API_USER_KID_ADD)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(form))).andExpect(status().isBadRequest());
    }

    @Test
    public void addKidUsernameExists() throws Exception {
        when(accSrvMock.findByUsername(testAccount.getUsername())).thenReturn(testAccount);
        KidForm form = new KidForm();
        form.setName("John");
        form.setSurname("Doe");
        form.setUsername(testAccount.getUsername());
        userRestCtrl.perform(post(API_USER_KID_ADD)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(form))).andExpect(status().isBadRequest());
    }

    @Test
    public void addKidSuccess() throws Exception {
        byte[] imgBytes;
        ClassLoader loader = this.getClass().getClassLoader();
        try (InputStream avatarFile = loader.getResourceAsStream("static/images/logo-white.png")) {
            imgBytes = IOUtils.toByteArray(avatarFile);
            String imgStream = Base64.encodeBase64String(imgBytes);
            KidForm form = new KidForm();
            form.setName("John");
            form.setSurname("Doe");
            form.setUsername("john");
            form.setAvatar(imgStream);
            Family family = new Family();
            family.setId(1L);
            family.getMembers().add(testAccount);
            family.getAdmins().add(testAccount);
            when(familyServiceMock.getFamily(testAccount)).thenReturn(family);
            Account kid = form.createAccount();
            kid.setId(TestUtil.USER_RANDOM_ID + (Math.random() * 100));
            kid.setType(AccountType.KID);
            when(accSrvMock.createKidAccount(any(Account.class))).thenReturn(kid);
            userRestCtrl.perform(post(API_USER_KID_ADD)
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(form))).andExpect(status().isOk());
            family.getMembers().add(kid);
            verify(accSrvMock, times(1)).createKidAccount(any(Account.class));
            verify(accSrvMock, times(1)).updateAvatar(kid, imgBytes);
            verify(familyServiceMock, times(1)).update(family);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }


}