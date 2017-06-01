package com.qprogramming.gifts.api.user;

import com.qprogramming.gifts.MockSecurityContext;
import com.qprogramming.gifts.TestUtil;
import com.qprogramming.gifts.account.*;
import com.qprogramming.gifts.account.event.AccountEvent;
import com.qprogramming.gifts.account.event.AccountEventType;
import com.qprogramming.gifts.account.family.*;
import com.qprogramming.gifts.config.mail.Mail;
import com.qprogramming.gifts.config.mail.MailService;
import com.qprogramming.gifts.gift.Gift;
import com.qprogramming.gifts.gift.GiftService;
import com.qprogramming.gifts.messages.MessagesService;
import com.qprogramming.gifts.support.ResultData;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.collection.IsCollectionWithSize;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.qprogramming.gifts.TestUtil.USER_RANDOM_ID;
import static org.junit.Assert.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserRestControllerTest {

    public static final String API_USER_REGISTER = "/api/user/register";
    public static final String API_USER_SETTINGS = "/api/user/settings";
    public static final String API_USER_VALIDATE_EMAIL = "/api/user/validate-email";
    public static final String API_USER_VALIDATE_USERNAME = "/api/user/validate-username";
    public static final String API_USER_UPDATE_AVATAR = "/api/user/avatar-upload";
    public static final String API_USER_FAMILY_CREATE = "/api/user/family-create";
    public static final String API_USER_FAMILY_UPDATE = "/api/user/family-update";
    public static final String API_USER_KID_ADD = "/api/user/kid-add";
    public static final String API_USER_KID_UPDATE = "/api/user/kid-update";
    public static final String API_USER_USER_DELETE = "/api/user/delete/";
    public static final String API_USER_SHARE = "/api/user/share";
    public static final String API_USER_ADMINS = "/api/user/admins";
    public static final String KID_ID = "KID-ID";
    private static final String API_USER = "/api/user";
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
    @Mock
    private AnonymousAuthenticationToken annonymousTokenMock;
    @Mock
    private GiftService giftServiceMock;
    @Mock
    private MailService mailServiceMock;

    private Account testAccount;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        UserRestController userCtrl = new UserRestController(accSrvMock, msgSrvMock, familyServiceMock, giftServiceMock, mailServiceMock);
        testAccount = TestUtil.createAccount();
        when(securityMock.getAuthentication()).thenReturn(authMock);
        when(authMock.getPrincipal()).thenReturn(testAccount);
        when(msgSrvMock.getMessage(anyString())).thenReturn("MESSAGE");
        SecurityContextHolder.setContext(securityMock);
        this.userRestCtrl = MockMvcBuilders.standaloneSetup(userCtrl).build();
    }

    @Test
    public void registerSuccess() throws Exception {
        when(accSrvMock.createLocalAccount(any(Account.class))).thenReturn(testAccount);
        RegisterForm form = new RegisterForm();
        form.setName(testAccount.getName());
        form.setSurname(testAccount.getSurname());
        form.setUsername(testAccount.getUsername());
        form.setEmail(testAccount.getEmail());
        form.setPassword("PasswordPassword!23");
        form.setConfirmpassword("PasswordPassword!23");
        userRestCtrl.perform(post(API_USER_REGISTER).contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(form)))
                .andExpect(status().isCreated());
        verify(accSrvMock, times(1)).createLocalAccount(any(Account.class));

    }

//    @Test
//    public void registerFormNotComplete() throws Exception {
//        RegisterForm form = new RegisterForm();
//        form.setEmail("notvalid");
//        userRestCtrl.perform(post(API_USER_REGISTER).contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(form)))
//                .andExpect(status().isBadRequest());
//    }

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
        userRestCtrl.perform(post(API_USER_SETTINGS)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(object.toString())).andExpect(status().isOk());
        verify(accSrvMock, times(1)).update(testAccount);
    }

    @Test
    public void languageChangedButNoUserFound() throws Exception {
        JSONObject object = new JSONObject();
        object.put("id", USER_RANDOM_ID);
        object.put("language", "pl");
        userRestCtrl.perform(post(API_USER_SETTINGS)
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
        verify(familyServiceMock, times(1)).inviteAccount(memberAndAdmin, result, AccountEventType.FAMILY_MEMEBER);
        verify(mailServiceMock, times(1)).sendConfirmMail(any(Mail.class), any(AccountEvent.class));
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

    @Test
    public void updateKidNotFound() throws Exception {
        KidForm form = new KidForm();
        form.setName("name");
        form.setSurname("surname");
        form.setUsername("username");
        form.setId(KID_ID);
        Family family = new Family();
        family.setId(1L);
        family.getMembers().add(testAccount);
        family.getAdmins().add(testAccount);
        when(familyServiceMock.getFamily(testAccount)).thenReturn(family);
        userRestCtrl.perform(post(API_USER_KID_UPDATE)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(form))).andExpect(status().isNotFound());
    }

    @Test
    public void updateKidFamilyNotFount() throws Exception {
        KidForm form = new KidForm();
        form.setName("name");
        form.setSurname("surname");
        form.setUsername("username");
        form.setId(KID_ID);
        userRestCtrl.perform(post(API_USER_KID_UPDATE)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(form))).andExpect(status().isBadRequest());
    }

    @Test
    public void updateKidFamilyNotAdmin() throws Exception {
        KidForm form = new KidForm();
        form.setName("name");
        form.setSurname("surname");
        form.setUsername("username");
        form.setId(KID_ID);
        Family family = new Family();
        family.setId(1L);
        family.getMembers().add(testAccount);
        when(familyServiceMock.getFamily(testAccount)).thenReturn(family);
        userRestCtrl.perform(post(API_USER_KID_UPDATE)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(form))).andExpect(status().isBadRequest());
    }

    @Test
    public void updateKidFamily() throws Exception {
        Account kidAccount = TestUtil.createAccount("name", "surname");
        kidAccount.setId(KID_ID);
        ClassLoader loader = this.getClass().getClassLoader();
        try (InputStream avatarFile = loader.getResourceAsStream("static/images/logo-white.png")) {
            String imgStream = Base64.encodeBase64String(IOUtils.toByteArray(avatarFile));
            KidForm form = new KidForm();
            form.setName("new-name");
            form.setSurname("new-surname");
            form.setUsername("username");
            form.setId(KID_ID);
            form.setAvatar(imgStream);
            Family family = new Family();
            family.setId(1L);
            family.getMembers().add(testAccount);
            family.getAdmins().add(testAccount);
            when(familyServiceMock.getFamily(testAccount)).thenReturn(family);
            when(accSrvMock.findById(KID_ID)).thenReturn(kidAccount);
            MvcResult mvcResult = userRestCtrl.perform(post(API_USER_KID_UPDATE)
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(form))).andExpect(status().isOk()).andReturn();
            String contentAsString = mvcResult.getResponse().getContentAsString();
            Account result = TestUtil.convertJsonToObject(contentAsString, Account.class);
            verify(accSrvMock, times(1)).updateAvatar(any(Account.class), any(byte[].class));
            verify(accSrvMock, times(1)).update(any(Account.class));
            assertEquals(result.getName(), form.getName());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void getUserById() throws Exception {
        when(accSrvMock.findById(testAccount.getId())).thenReturn(testAccount);
        MvcResult mvcResult = userRestCtrl.perform(get(API_USER + "?identification=" + testAccount.getId())).andExpect(status().isOk()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        Account result = TestUtil.convertJsonToObject(contentAsString, Account.class);
        assertEquals(testAccount, result);

    }

    @Test
    public void getUserByIdAnnonymousNotPublic() throws Exception {
        when(accSrvMock.findById(testAccount.getId())).thenReturn(testAccount);
        when(securityMock.getAuthentication()).thenReturn(annonymousTokenMock);
        MvcResult mvcResult = userRestCtrl.perform(get(API_USER + "?identification=" + testAccount.getId())).andExpect(status().isOk()).andReturn();
        assertTrue(StringUtils.isEmpty(mvcResult.getResponse().getContentAsString()));

    }

    @Test
    public void getUserByIdAnnonymousPublic() throws Exception {
        testAccount.setPublicList(true);
        when(accSrvMock.findById(testAccount.getId())).thenReturn(testAccount);
        when(securityMock.getAuthentication()).thenReturn(annonymousTokenMock);
        MvcResult mvcResult = userRestCtrl.perform(get(API_USER + "?identification=" + testAccount.getId())).andExpect(status().isOk()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        Account result = TestUtil.convertJsonToObject(contentAsString, Account.class);
        assertEquals(testAccount, result);
    }

    @Test
    public void deleteKidNotFound() throws Exception {
        userRestCtrl.perform(delete(API_USER_USER_DELETE + "RANDOMID")).andExpect(status().isNotFound());
    }

    @Test
    public void deleteKidNotFamilyAdmin() throws Exception {
        Account kid = TestUtil.createAccount("Little", "Kid");
        kid.setId(TestUtil.USER_RANDOM_ID);
        kid.setType(AccountType.KID);
        Family family = new Family();
        family.setId(1L);
        family.getMembers().add(kid);
        when(accSrvMock.findById(kid.getId())).thenReturn(kid);
        when(familyServiceMock.getFamily(kid)).thenReturn(family);
        userRestCtrl.perform(delete(API_USER_USER_DELETE + kid.getId())).andExpect(status().isBadRequest());
    }

    @Test
    public void deleteKidAdminButAccountNotKid() throws Exception {
        Account kid = TestUtil.createAccount("Little", "Kid");
        kid.setId(TestUtil.USER_RANDOM_ID);
        kid.setType(AccountType.LOCAL);
        Family family = new Family();
        family.setId(1L);
        family.getMembers().add(kid);
        family.getMembers().add(testAccount);
        family.getAdmins().add(testAccount);
        when(accSrvMock.findById(kid.getId())).thenReturn(kid);
        when(familyServiceMock.getFamily(kid)).thenReturn(family);
        userRestCtrl.perform(delete(API_USER_USER_DELETE + kid.getId())).andExpect(status().isBadRequest());
    }

    @Test
    public void deleteKid() throws Exception {
        Account kid = TestUtil.createAccount("Little", "Kid");
        kid.setId(TestUtil.USER_RANDOM_ID);
        kid.setType(AccountType.KID);
        Family family = new Family();
        family.setId(1L);
        family.getMembers().add(kid);
        family.getMembers().add(testAccount);
        family.getAdmins().add(testAccount);
        List<Gift> giftList = Arrays.asList(TestUtil.createGift(1L, kid), TestUtil.createGift(2L, kid), TestUtil.createGift(3L, kid));
        when(accSrvMock.findById(kid.getId())).thenReturn(kid);
        when(familyServiceMock.getFamily(kid)).thenReturn(family);
        userRestCtrl.perform(delete(API_USER_USER_DELETE + kid.getId())).andExpect(status().isOk());
        verify(giftServiceMock, times(1)).deleteUserGifts(kid);
        verify(accSrvMock, times(1)).delete(kid);
    }

    @Test
    public void deleteAccount() throws Exception {
        Family family = new Family();
        family.setId(1L);
        family.getMembers().add(testAccount);
        family.getMembers().add(testAccount);
        family.getAdmins().add(testAccount);
        List<Gift> giftList = Arrays.asList(TestUtil.createGift(1L, testAccount), TestUtil.createGift(2L, testAccount), TestUtil.createGift(3L, testAccount));
        when(accSrvMock.findById(testAccount.getId())).thenReturn(testAccount);
        when(familyServiceMock.getFamily(testAccount)).thenReturn(family);
        userRestCtrl.perform(delete(API_USER_USER_DELETE + testAccount.getId())).andExpect(status().isOk());
        verify(giftServiceMock, times(1)).deleteUserGifts(testAccount);
        verify(giftServiceMock, times(1)).deleteClaims(testAccount);
        verify(accSrvMock, times(1)).delete(testAccount);
    }

    @Test
    public void shareGiftList() throws Exception {
        testAccount.setPublicList(true);
        userRestCtrl.perform(post(API_USER_SHARE).content("valid@email.com;invalid@;alsovalid@email.pl")).andExpect(status().isOk());
        verify(mailServiceMock, times(1)).shareGiftList((List<Mail>) argThat(IsCollectionWithSize.<Mail>hasSize(2)));

    }

    @Test
    public void shareGiftListNotPublic() throws Exception {
        testAccount.setPublicList(false);
        userRestCtrl.perform(post(API_USER_SHARE).content("valid@email.com;invalid@;alsovalid@email.pl")).andExpect(status().isBadRequest());
    }

    @Test
    public void getAdminsNotAdmin() throws Exception {
        testAccount.setRole(Roles.ROLE_USER);
        userRestCtrl.perform(get(API_USER_ADMINS)).andExpect(status().isForbidden());
    }

    @Test
    public void getAdmins() throws Exception {
        testAccount.setRole(Roles.ROLE_ADMIN);
        when(accSrvMock.findAdmins()).thenReturn(Collections.singletonList(testAccount));
        MvcResult mvcResult = userRestCtrl.perform(get(API_USER_ADMINS)).andExpect(status().isOk()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        List<Account> result = TestUtil.convertJsonToList(contentAsString, List.class, Account.class);
        assertTrue(result.contains(testAccount));
    }


}