package com.qprogramming.gifts.api.user;

import com.fasterxml.uuid.Generators;
import com.qprogramming.gifts.MockedAccountTestBase;
import com.qprogramming.gifts.TestUtil;
import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.AccountService;
import com.qprogramming.gifts.account.AccountType;
import com.qprogramming.gifts.account.RegisterForm;
import com.qprogramming.gifts.account.event.AccountEvent;
import com.qprogramming.gifts.account.event.AccountEventType;
import com.qprogramming.gifts.account.family.Family;
import com.qprogramming.gifts.account.family.FamilyForm;
import com.qprogramming.gifts.account.family.FamilyService;
import com.qprogramming.gifts.account.family.KidForm;
import com.qprogramming.gifts.config.mail.Mail;
import com.qprogramming.gifts.config.mail.MailService;
import com.qprogramming.gifts.exceptions.AccountNotFoundException;
import com.qprogramming.gifts.exceptions.FamilyNotAdminException;
import com.qprogramming.gifts.exceptions.FamilyNotFoundException;
import com.qprogramming.gifts.gift.Gift;
import com.qprogramming.gifts.gift.GiftService;
import com.qprogramming.gifts.messages.MessagesService;
import com.qprogramming.gifts.schedule.AppEventService;
import com.qprogramming.gifts.support.ResultData;
import com.qprogramming.gifts.support.Utils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static com.qprogramming.gifts.TestUtil.*;
import static com.qprogramming.gifts.account.event.AccountEventType.ACCOUNT_CONFIRM;
import static com.qprogramming.gifts.account.event.AccountEventType.FAMILY_ALLOW_FAMILY;
import static org.junit.Assert.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserRestControllerTest extends MockedAccountTestBase {

    private static final String API_USER_REGISTER = "/api/account/register";
    private static final String API_USER_SETTINGS = "/api/account/settings";
    private static final String API_USER_VALIDATE_EMAIL = "/api/account/validate-email";
    private static final String API_USER_VALIDATE_USERNAME = "/api/account/validate-username";
    private static final String API_USER_UPDATE_AVATAR = "/api/account/avatar-upload";
    private static final String API_USER_FAMILY_CREATE = "/api/account/family-create";
    private static final String API_USER_FAMILY_UPDATE = "/api/account/family-update";
    private static final String API_USER_FAMILY_LEAVE = "/api/account/family-leave";
    private static final String API_USER_KID_ADD = "/api/account/kid-add";
    private static final String API_USER_KID_UPDATE = "/api/account/kid-update";
    private static final String API_USER_USER_DELETE = "/api/account/delete/";
    private static final String API_USER_SHARE = "/api/account/share";
    private static final String API_USER_ADMINS = "/api/account/admins";
    private static final String KID_ID = "KID-ID";
    private static final String API_USER_CONFIRM = "/api/account/confirm";
    private static final String API_USER_FETCH = "/api/account/userList";
    private static final String API_USER = "/api/account";
    private static final String API_ALLOWED_FAMILY_CONFIRM_FAMILY = API_USER + "/allowed/family/confirm-family";
    private static final String API_ALLOWED_FAMILY_REMOVE_FAMILY = API_USER + "/allowed/family/remove-family";
    private static final String API_ALLOWED_ACCOUNT_ADD = API_USER + "/allowed/account/add";
    private static final String API_ALLOWED_ACCOUNT_REMOVE = API_USER + "/allowed/account/remove";
    private MockMvc userRestCtrl;
    @Mock
    private AccountService accSrvMock;
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
    @Mock
    private AppEventService eventServiceMock;
    @Mock
    private LogoutHandler logoutHandlerMock;

    @Before
    public void setUp() throws Exception {
        super.setup();
        UserRestController userCtrl = new UserRestController(accSrvMock, msgSrvMock, familyServiceMock, giftServiceMock, mailServiceMock, eventServiceMock, logoutHandlerMock);
        when(msgSrvMock.getMessage(anyString())).thenReturn("MESSAGE");
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
        userRestCtrl.perform(post(API_USER_REGISTER).contentType(APPLICATION_JSON_UTF8).content(convertObjectToJsonBytes(form)))
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
        when(accSrvMock.findByEmail(testAccount.getEmail())).thenReturn(Optional.of(testAccount));
        MvcResult mvcResult = userRestCtrl.perform(post(API_USER_REGISTER).contentType(APPLICATION_JSON_UTF8).content(convertObjectToJsonBytes(form))).andExpect(status().is4xxClientError())
                .andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        assertTrue(contentAsString.contains("email"));
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
        MvcResult mvcResult = userRestCtrl.perform(post(API_USER_REGISTER).contentType(APPLICATION_JSON_UTF8).content(convertObjectToJsonBytes(form))).andExpect(status().is4xxClientError())
                .andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        assertTrue(contentAsString.contains("passwords"));
    }

    @Test
    public void registerPasswordTooWeak() throws Exception {
        RegisterForm form = new RegisterForm();
        form.setName(testAccount.getName());
        form.setSurname(testAccount.getSurname());
        form.setUsername(testAccount.getUsername());
        form.setEmail(testAccount.getEmail());
        form.setPassword("admin");
        form.setConfirmpassword("admin");
        MvcResult mvcResult = userRestCtrl.perform(post(API_USER_REGISTER).contentType(APPLICATION_JSON_UTF8).content(convertObjectToJsonBytes(form))).andExpect(status().is4xxClientError())
                .andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        assertTrue(contentAsString.contains("weak"));
    }

    @Test
    public void languageChangedForUser() throws Exception {
        when(accSrvMock.findById(USER_RANDOM_ID)).thenReturn(testAccount);
        JSONObject object = new JSONObject();
        object.put("id", USER_RANDOM_ID);
        object.put("language", "pl");
        userRestCtrl.perform(post(API_USER_SETTINGS)
                .contentType(APPLICATION_JSON_UTF8)
                .content(object.toString())).andExpect(status().isOk());
        verify(accSrvMock, times(1)).update(testAccount);
    }

    @Test
    public void languageChangedButNoUserFound() throws Exception {
        JSONObject object = new JSONObject();
        object.put("language", "pl");
        when(accSrvMock.findById(Utils.getCurrentAccountId())).thenThrow(AccountNotFoundException.class);
        userRestCtrl.perform(post(API_USER_SETTINGS)
                .contentType(APPLICATION_JSON_UTF8)
                .content(object.toString())).andExpect(status().isNotFound());
    }


    @Test
    public void validateEmailOk() throws Exception {
        MvcResult mvcResult = userRestCtrl.perform(post(API_USER_VALIDATE_EMAIL)
                .contentType(APPLICATION_JSON_UTF8)
                .content(testAccount.getEmail())).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        assertFalse(contentAsString.contains(ResultData.Code.ERROR.toString()));
    }

    @Test
    public void validateEmailExists() throws Exception {
        when(accSrvMock.findByEmail(testAccount.getEmail())).thenReturn(Optional.of(testAccount));
        MvcResult mvcResult = userRestCtrl.perform(post(API_USER_VALIDATE_EMAIL)
                .contentType(APPLICATION_JSON_UTF8)
                .content(testAccount.getEmail())).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        assertTrue(contentAsString.contains(ResultData.Code.ERROR.toString()));
    }

    @Test
    public void validateUsernameExists() throws Exception {
        when(accSrvMock.findByUsername(testAccount.getUsername())).thenReturn(Optional.of(testAccount));
        userRestCtrl.perform(post(API_USER_VALIDATE_USERNAME)
                .contentType(APPLICATION_JSON_UTF8)
                .content(testAccount.getUsername())).andExpect(status().is4xxClientError());
    }

    @Test
    public void validateUsernameOk() throws Exception {
        MvcResult mvcResult = userRestCtrl.perform(post(API_USER_VALIDATE_USERNAME)
                .contentType(APPLICATION_JSON_UTF8)
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
                    .contentType(APPLICATION_JSON_UTF8)
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
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(form))).andExpect(status().isOk());
        verify(familyServiceMock, times(1)).update(any(Family.class));
    }

    @Test
    public void createFamilyMembersAndAdmins() throws Exception {
        FamilyForm form = new FamilyForm();
        form.setAdmins(Collections.singleton(USERNAME + "1"));
        form.setMembers(Collections.singleton((USERNAME + "1")));
        Family family = new Family();
        family.setId(1L);
        family.getMembers().add(testAccount);
        family.getAdmins().add(testAccount);
        Account memberAndAdmin = createAccount("John", "Doe");
        memberAndAdmin.setUsername(USERNAME + "1");
        AccountEvent event = new AccountEvent();
        event.setAccount(testAccount);
        event.setFamily(family);
        event.setType(AccountEventType.FAMILY_MEMEBER);
        event.setToken("aaa");
        when(accSrvMock.findByEmailsOrUsernames(Collections.singleton(USERNAME + "1"))).thenReturn(Collections.singleton(memberAndAdmin));
        when(familyServiceMock.createFamily()).thenReturn(family);
        when(familyServiceMock.inviteAccount(any(Account.class), any(Family.class), any(AccountEventType.class))).thenReturn(event);
        when(familyServiceMock.update(family)).then(returnsFirstArg());
        MvcResult mvcResult = userRestCtrl.perform(post(API_USER_FAMILY_CREATE)
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(form))).andExpect(status().isOk()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        verify(familyServiceMock, times(1)).update(any(Family.class));
        verify(familyServiceMock, times(1)).inviteAccount(memberAndAdmin, family, AccountEventType.FAMILY_MEMEBER);
        verify(mailServiceMock, times(1)).sendConfirmMail(any(Mail.class), any(AccountEvent.class));
    }


    @Test
    public void createFamilyAlreadyExists() throws Exception {
        FamilyForm form = new FamilyForm();
        Family family = new Family();
        family.setId(1L);
        when(familyServiceMock.getFamily(testAccount)).thenReturn(Optional.of(family));
        userRestCtrl.perform(put(API_USER_FAMILY_CREATE)
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(form))).andExpect(status().is4xxClientError());
    }


    @Test
    public void updateFamilyNotFound() throws Exception {
        FamilyForm form = new FamilyForm();
        Family family = new Family();
        family.setId(1L);
        when(familyServiceMock.getFamilyAsFamilyAdmin()).thenThrow(FamilyNotFoundException.class);
        userRestCtrl.perform(put(API_USER_FAMILY_UPDATE)
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(form))).andExpect(status().isNotFound());
    }

    @Test
    public void updateFamilyAddMemberAndAdmin() throws Exception {
        FamilyForm form = new FamilyForm();
        HashSet<String> members = new HashSet<>();
        HashSet<String> admins = new HashSet<>();
        String newUsername = USERNAME + "1";
        members.add(newUsername);
        members.add(testAccount.getUsername());
        admins.add(newUsername);
        admins.add(testAccount.getUsername());
        form.setAdmins(admins);
        form.setMembers(members);
        Family family = new Family();
        family.setId(1L);
        family.getMembers().add(testAccount);
        family.getAdmins().add(testAccount);
        Account memberAndAdmin = createAdminAccount();
        memberAndAdmin.setUsername(newUsername);
        HashSet<Account> dbMembers = new HashSet<>();
        dbMembers.add(testAccount);
        dbMembers.add(memberAndAdmin);
        HashSet<Account> dbAdmins = new HashSet<>();
        dbAdmins.add(testAccount);
        dbAdmins.add(memberAndAdmin);
        when(accSrvMock.findByEmailsOrUsernames(members)).thenReturn(dbMembers).thenReturn(dbAdmins);
        when(familyServiceMock.getFamilyAsFamilyAdmin()).thenReturn(family);
        when(familyServiceMock.update(family)).then(returnsFirstArg());
        MvcResult mvcResult = userRestCtrl.perform(put(API_USER_FAMILY_UPDATE)
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(form))).andExpect(status().isOk()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        verify(familyServiceMock, times(1)).inviteAccount(memberAndAdmin, family, AccountEventType.FAMILY_ADMIN);
        assertTrue(family.getMembers().size() == 1);
    }

    @Test
    public void updateFamilyNotAdmin() throws Exception {
        FamilyForm form = new FamilyForm();
        form.setAdmins(Collections.singleton(USER_RANDOM_ID + "1"));
        form.setMembers(Collections.singleton(USER_RANDOM_ID + "1"));
        Family family = new Family();
        family.setId(1L);
        family.getMembers().add(testAccount);
        when(familyServiceMock.getFamilyAsFamilyAdmin()).thenThrow(FamilyNotAdminException.class);
        userRestCtrl.perform(put(API_USER_FAMILY_UPDATE)
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(form))).andExpect(status().isForbidden());
    }

    @Test
    public void leaveFamilyNoFamily() throws Exception {
        userRestCtrl.perform(put(API_USER_FAMILY_LEAVE)).andExpect(status().isNotFound());

    }

    @Test
    public void leaveFamily() throws Exception {
        Family family = new Family();
        family.setId(1L);
        family.getMembers().add(testAccount);
        when(familyServiceMock.getFamily(testAccount)).thenReturn(Optional.of(family));
        when(familyServiceMock.update(family)).then(returnsFirstArg());
        MvcResult mvcResult = userRestCtrl.perform(put(API_USER_FAMILY_LEAVE)).andExpect(status().isOk()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        verify(familyServiceMock, times(1)).removeFromFamily(testAccount, family);

    }

    @Test
    public void addKidNoFamily() throws Exception {
        KidForm form = new KidForm();
        form.setName("John");
        form.setSurname("Doe");
        form.setUsername("john");
        when(familyServiceMock.getFamilyAsFamilyAdmin()).thenThrow(FamilyNotFoundException.class);
        userRestCtrl.perform(post(API_USER_KID_ADD)
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(form))).andExpect(status().is4xxClientError());
    }

    @Test
    public void addKidNotFamilyAdmin() throws Exception {
        Family family = new Family();
        family.setId(1L);
        family.getMembers().add(testAccount);
        when(familyServiceMock.getFamilyAsFamilyAdmin()).thenThrow(FamilyNotAdminException.class);
        KidForm form = new KidForm();
        form.setName("John");
        form.setSurname("Doe");
        form.setUsername("john");
        userRestCtrl.perform(post(API_USER_KID_ADD)
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(form))).andExpect(status().is4xxClientError());
    }

    @Test
    public void addKidUsernameExists() throws Exception {
        when(accSrvMock.findByUsername(testAccount.getUsername())).thenReturn(Optional.of(testAccount));
        KidForm form = new KidForm();
        form.setName("John");
        form.setSurname("Doe");
        form.setUsername(testAccount.getUsername());
        userRestCtrl.perform(post(API_USER_KID_ADD)
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(form))).andExpect(status().is4xxClientError());
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
            when(familyServiceMock.getFamilyAsFamilyAdmin()).thenReturn(family);
            Account kid = form.createAccount();
            kid.setId(USER_RANDOM_ID + (Math.random() * 100));
            kid.setType(AccountType.KID);
            when(accSrvMock.createKidAccount(any(Account.class))).thenReturn(kid);
            userRestCtrl.perform(post(API_USER_KID_ADD)
                    .contentType(APPLICATION_JSON_UTF8)
                    .content(convertObjectToJsonBytes(form))).andExpect(status().isOk());
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
        when(familyServiceMock.getFamily(testAccount)).thenReturn(Optional.of(family));
        when(accSrvMock.findById(KID_ID)).thenThrow(AccountNotFoundException.class);
        userRestCtrl.perform(post(API_USER_KID_UPDATE)
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(form))).andExpect(status().isNotFound());
    }

    @Test
    public void updateKidFamilyNotFount() throws Exception {
        KidForm form = new KidForm();
        form.setName("name");
        form.setSurname("surname");
        form.setUsername("username");
        form.setId(KID_ID);
        when(familyServiceMock.getFamilyAsFamilyAdmin()).thenThrow(FamilyNotFoundException.class);
        userRestCtrl.perform(post(API_USER_KID_UPDATE)
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(form))).andExpect(status().is4xxClientError());
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
        when(familyServiceMock.getFamilyAsFamilyAdmin()).thenThrow(FamilyNotAdminException.class);
        userRestCtrl.perform(post(API_USER_KID_UPDATE)
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(form))).andExpect(status().is4xxClientError());
    }

    @Test
    public void updateKidFamily() throws Exception {
        Account kidAccount = createAccount("name", "surname");
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
            when(familyServiceMock.getFamilyAsFamilyAdmin()).thenReturn(family);
            when(accSrvMock.findById(KID_ID)).thenReturn(kidAccount);
            MvcResult mvcResult = userRestCtrl.perform(post(API_USER_KID_UPDATE)
                    .contentType(APPLICATION_JSON_UTF8)
                    .content(convertObjectToJsonBytes(form))).andExpect(status().isOk()).andReturn();
            String contentAsString = mvcResult.getResponse().getContentAsString();
            Account result = convertJsonToObject(contentAsString, Account.class);
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
        Account result = convertJsonToObject(contentAsString, Account.class);
        assertEquals(testAccount, result);

    }

    @Test
    public void getUserByIdAnnonymousNotPublic() throws Exception {
        AnonymousAuthenticationToken anonymousAuthenticationMock = mock(AnonymousAuthenticationToken.class);
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
        Account result = convertJsonToObject(contentAsString, Account.class);
        assertEquals(testAccount, result);
    }

    @Test
    public void deleteKidNotFound() throws Exception {
        when(accSrvMock.findById("RANDOMID")).thenThrow(AccountNotFoundException.class);
        userRestCtrl.perform(delete(API_USER_USER_DELETE + "RANDOMID")).andExpect(status().isNotFound());
    }

    @Test
    public void deleteKidNotFamilyAdmin() throws Exception {
        Account kid = createAccount("Little", "Kid");
        kid.setId(USER_RANDOM_ID);
        kid.setType(AccountType.KID);
        Family family = new Family();
        family.setId(1L);
        family.getMembers().add(kid);
        when(accSrvMock.findById(kid.getId())).thenReturn(kid);
        when(familyServiceMock.getFamily(kid)).thenReturn(Optional.of(family));
        userRestCtrl.perform(delete(API_USER_USER_DELETE + kid.getId())).andExpect(status().is4xxClientError());
    }

    @Test
    public void deleteKidAdminButAccountNotKid() throws Exception {
        Account kid = createAccount("Little", "Kid");
        kid.setId(USER_RANDOM_ID);
        kid.setType(AccountType.LOCAL);
        Family family = new Family();
        family.setId(1L);
        family.getMembers().add(kid);
        family.getMembers().add(testAccount);
        family.getAdmins().add(testAccount);
        when(accSrvMock.findById(kid.getId())).thenReturn(kid);
        when(familyServiceMock.getFamily(kid)).thenReturn(Optional.of(family));
        userRestCtrl.perform(delete(API_USER_USER_DELETE + kid.getId())).andExpect(status().is4xxClientError());
    }

    @Test
    public void deleteKid() throws Exception {
        Account kid = createAccount("Little", "Kid");
        kid.setId(USER_RANDOM_ID);
        kid.setType(AccountType.KID);
        Family family = new Family();
        family.setId(1L);
        family.getMembers().add(kid);
        family.getMembers().add(testAccount);
        family.getAdmins().add(testAccount);
        List<Gift> giftList = Arrays.asList(createGift(1L, kid), createGift(2L, kid), createGift(3L, kid));
        when(accSrvMock.findById(kid.getId())).thenReturn(kid);
        when(familyServiceMock.getFamily(kid)).thenReturn(Optional.of(family));
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
        List<Gift> giftList = Arrays.asList(createGift(1L, testAccount), createGift(2L, testAccount), createGift(3L, testAccount));
        when(accSrvMock.findById(testAccount.getId())).thenReturn(testAccount);
        when(familyServiceMock.getFamily(testAccount)).thenReturn(Optional.of(family));
        userRestCtrl.perform(delete(API_USER_USER_DELETE + testAccount.getId())).andExpect(status().isOk());
        verify(eventServiceMock, times(1)).deleteUserEvents(testAccount);
        verify(giftServiceMock, times(1)).deleteUserGifts(testAccount);
        verify(giftServiceMock, times(1)).deleteClaims(testAccount);
        verify(accSrvMock, times(1)).delete(testAccount);
        verify(logoutHandlerMock, times(1)).logout(any(HttpServletRequest.class), any(HttpServletResponse.class), any(Authentication.class));
    }

    @Test
    public void shareGiftListTest() throws Exception {
        testAccount.setPublicList(true);
        userRestCtrl.perform(post(API_USER_SHARE).content("valid@email.com;invalid@;alsovalid@email.pl")).andExpect(status().isOk());
        //TODO check arg count
//        verify(mailServiceMock, times(1)).shareGiftList((List<Mail>) argThat(IsCollectionWithSize.<Mail>hasSize(2)));

    }

    @Test
    public void shareGiftListNotPublicTest() throws Exception {
        testAccount.setPublicList(false);
        userRestCtrl.perform(post(API_USER_SHARE).content("valid@email.com;invalid@;alsovalid@email.pl")).andExpect(status().isBadRequest());
    }

    @Test
    public void getAdminsTest() throws Exception {
        testAccount.addAuthority(TestUtil.createAdminAuthority());
        when(accSrvMock.findAdmins()).thenReturn(Collections.singletonList(testAccount));
        MvcResult mvcResult = userRestCtrl.perform(get(API_USER_ADMINS)).andExpect(status().isOk()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        List<Account> result = convertJsonToList(contentAsString, List.class, Account.class);
        assertTrue(result.contains(testAccount));
    }

    @Test
    public void confirmFamilymemberTokenExpired() throws Exception {
        String token = "09011a27-478c-11e7-bcf7-930b1424157e";
        AccountEvent event = new AccountEvent();
        event.setToken(token);
        event.setAccount(testAccount);
        when(accSrvMock.findEvent(token)).thenReturn(Optional.of(event));
        userRestCtrl.perform(post(API_USER_CONFIRM).content(token)).andExpect(status().isConflict());
    }

    @Test
    public void confirmFamilymemberTokenNotFound() throws Exception {
        String token = "09011a27-478c-11e7-bcf7-930b1424157e";
        when(accSrvMock.findEvent(token)).thenReturn(Optional.empty());
        userRestCtrl.perform(post(API_USER_CONFIRM).content(token)).andExpect(status().isNotFound());
    }


    @Test
    public void confirmFamilymemberAlreadyMemberTest() throws Exception {
        String token = Generators.timeBasedGenerator().generate().toString();
        Family family = new Family();
        family.getMembers().add(testAccount);
        AccountEvent event = new AccountEvent();
        event.setToken(token);
        event.setAccount(testAccount);
        event.setFamily(family);
        event.setType(AccountEventType.FAMILY_MEMEBER);
        when(familyServiceMock.getFamily(testAccount)).thenReturn(Optional.of(family));
        when(accSrvMock.findEvent(token)).thenReturn(Optional.of(event));
        userRestCtrl.perform(post(API_USER_CONFIRM).content(token)).andExpect(status().isConflict());
    }

    @Test
    public void confirmFamilymemberSuccessTest() throws Exception {
        String token = Generators.timeBasedGenerator().generate().toString();
        Family family = new Family();
        AccountEvent event = new AccountEvent();
        event.setToken(token);
        event.setAccount(testAccount);
        event.setFamily(family);
        event.setType(AccountEventType.FAMILY_MEMEBER);
        when(accSrvMock.findEvent(token)).thenReturn(Optional.of(event));
        when(familyServiceMock.addAccountToFamily(testAccount, family)).thenReturn(family);
        userRestCtrl.perform(post(API_USER_CONFIRM).content(token)).andExpect(status().isOk());
        verify(familyServiceMock, times(1)).addAccountToFamily(testAccount, family);
    }

    @Test
    public void confirmFamilymemberEventNotFoundTest() throws Exception {
        String token = Generators.timeBasedGenerator().generate().toString();
        userRestCtrl.perform(post(API_USER_CONFIRM).content(token)).andExpect(status().isNotFound());
    }

    @Test
    public void userSearchListAccountNotFoundTest() throws Exception {
        userRestCtrl.perform(get(API_USER_FETCH).param("username", "test")).andExpect(status().isNotFound());
    }

    @Test
    public void userSearchListForCurrentUserTest() throws Exception {
        List<Account> accountList = createAccountList();
        accountList.add(testAccount);
        when(accSrvMock.findAllSortByFamily(testAccount)).thenReturn(new HashSet<>(accountList));
        MvcResult mvcResult = userRestCtrl.perform(get(API_USER_FETCH)).andExpect(status().isOk()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        List<Account> result = convertJsonToList(contentAsString, List.class, Account.class);
        assertTrue(result.contains(testAccount));
    }

    @Test
    public void userSearchListForOtherUserTest() throws Exception {
        List<Account> accountList = createAccountList();
        accountList.add(testAccount);
        when(accSrvMock.findAllSortByFamily(testAccount)).thenReturn(new HashSet<>(accountList));
        when(accSrvMock.findByUsername(testAccount.getUsername())).thenReturn(Optional.of(testAccount));
        MvcResult mvcResult = userRestCtrl.perform(get(API_USER_FETCH).param("username", testAccount.getUsername()))
                .andExpect(status().isOk()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        List<Account> result = convertJsonToList(contentAsString, List.class, Account.class);
        assertTrue(result.contains(testAccount));
    }

    @Test
    public void addAccountToAllowed() throws Exception {
        Account account = createAdminAccount();
        when(accSrvMock.findById(account.getId())).thenReturn(account);
        when(accSrvMock.getCurrentAccount()).thenReturn(testAccount);
        userRestCtrl.perform(put(API_ALLOWED_ACCOUNT_ADD).content(account.getId()))
                .andExpect(status().isOk());
        verify(accSrvMock, times(1)).update(testAccount);
        assertTrue(testAccount.getAllowed().contains(account.getId()));
    }

    @Test
    public void addAccountToAllowedNotFound() throws Exception {
        Account account = createAdminAccount();
        when(accSrvMock.findById(account.getId())).thenThrow(AccountNotFoundException.class);
        when(accSrvMock.getCurrentAccount()).thenReturn(testAccount);
        userRestCtrl.perform(put(API_ALLOWED_ACCOUNT_ADD).content(account.getId()))
                .andExpect(status().isNotFound());
        assertFalse(testAccount.getAllowed().contains(account.getId()));
    }

    @Test
    public void removeAccountFromAllowedNotFound() throws Exception {
        testAccount.getAllowed().add(ADMIN_RANDOM_ID);
        when(accSrvMock.findById(ADMIN_RANDOM_ID)).thenThrow(AccountNotFoundException.class);
        when(accSrvMock.getCurrentAccount()).thenReturn(testAccount);
        userRestCtrl.perform(delete(API_ALLOWED_ACCOUNT_REMOVE).content(ADMIN_RANDOM_ID))
                .andExpect(status().isNotFound());
        assertTrue(testAccount.getAllowed().contains(ADMIN_RANDOM_ID));
    }

    @Test
    public void removeAccountFromAllowed() throws Exception {
        Account account = createAdminAccount();
        testAccount.getAllowed().add(account.getId());
        when(accSrvMock.findById(account.getId())).thenReturn(account);
        when(accSrvMock.getCurrentAccount()).thenReturn(testAccount);
        userRestCtrl.perform(delete(API_ALLOWED_ACCOUNT_REMOVE).content(account.getId()))
                .andExpect(status().isOk());
        verify(accSrvMock, times(1)).update(testAccount);
        assertFalse(testAccount.getAllowed().contains(account.getId()));
    }

    @Test
    public void confirmAddFamilyToAllowedNotFound() throws Exception {
        String token = Generators.timeBasedGenerator().generate().toString();
        AccountEvent event = new AccountEvent();
        event.setType(FAMILY_ALLOW_FAMILY);
        event.setAccount(testAccount);
        when(accSrvMock.findEvent(token)).thenReturn(Optional.of(event));
        when(familyServiceMock.getFamilyAsFamilyAdmin()).thenThrow(FamilyNotFoundException.class);
        userRestCtrl.perform(put(API_USER_CONFIRM)
                .contentType(APPLICATION_JSON_UTF8)
                .content(token))
                .andExpect(status().isNotFound());
    }

    @Test
    public void confirmAddFamilyToAllowedNotAdmin() throws Exception {
        String token = Generators.timeBasedGenerator().generate().toString();
        AccountEvent event = new AccountEvent();
        event.setAccount(testAccount);
        event.setType(FAMILY_ALLOW_FAMILY);
        when(accSrvMock.findEvent(token)).thenReturn(Optional.of(event));
        when(familyServiceMock.getFamilyAsFamilyAdmin()).thenThrow(FamilyNotAdminException.class);
        userRestCtrl.perform(put(API_USER_CONFIRM)
                .contentType(APPLICATION_JSON_UTF8)
                .content(token))
                .andExpect(status().isForbidden());
    }

    @Test
    public void confirmAddFamilyToAllowedNotFoundEvent() throws Exception {
        String token = Generators.timeBasedGenerator().generate().toString();
        when(accSrvMock.findEvent(anyString())).thenReturn(Optional.empty());
        Family family = new Family();
        when(familyServiceMock.getFamilyAsFamilyAdmin()).thenReturn(family);
        when(familyServiceMock.getFamilyById(1L)).thenReturn(Optional.empty());
        userRestCtrl.perform(put(API_USER_CONFIRM)
                .contentType(APPLICATION_JSON_UTF8)
                .content(token))
                .andExpect(status().isNotFound());
    }

    @Test
    public void confirmAddFamilyToAllowed() throws Exception {
        Account adminAccount = createAdminAccount();
        Account normalAccount = createAccount("John", "Doe");
        Family targetFamily = new Family();
        targetFamily.getMembers().add(testAccount);
        targetFamily.getMembers().add(normalAccount);
        Family sourceFamily = new Family();
        sourceFamily.getMembers().add(adminAccount);
        String token = Generators.timeBasedGenerator().generate().toString();
        AccountEvent event = new AccountEvent();
        event.setAccount(testAccount);
        event.setFamily(sourceFamily);
        event.setType(FAMILY_ALLOW_FAMILY);
        when(accSrvMock.findEvent(token)).thenReturn(Optional.of(event));
        when(familyServiceMock.getFamilyAsFamilyAdmin()).thenReturn(targetFamily);
        doCallRealMethod().when(accSrvMock).addAllowedToFamily(any(Family.class), anySet());
        userRestCtrl.perform(put(API_USER_CONFIRM)
                .contentType(APPLICATION_JSON_UTF8)
                .content(token))
                .andExpect(status().isOk());
        verify(familyServiceMock, times(2)).update(any());
        assertTrue(testAccount.getAllowed().contains(adminAccount.getId()));
        assertTrue(adminAccount.getAllowed().contains(testAccount.getId()));
    }

    @Test
    public void removeFamilyFromAllowed() throws Exception {
        Account adminAccount = createAdminAccount();
        Account normalAccount = createAccount("John", "Doe");
        Family targetFamily = new Family();
        targetFamily.getMembers().add(testAccount);
        targetFamily.getMembers().add(normalAccount);
        Family sourceFamily = new Family();
        sourceFamily.getMembers().add(adminAccount);
        when(familyServiceMock.getFamilyAsFamilyAdmin()).thenReturn(targetFamily);
        when(familyServiceMock.getFamilyById(1L)).thenReturn(Optional.of(sourceFamily));
        doCallRealMethod().when(accSrvMock).addAllowedToFamily(any(Family.class), anySet());
        userRestCtrl.perform(delete(API_ALLOWED_FAMILY_REMOVE_FAMILY)
                .contentType(APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(1L)))
                .andExpect(status().isOk());
        verify(familyServiceMock, times(2)).update(any());
        assertFalse(testAccount.getAllowed().contains(adminAccount.getId()));
        assertFalse(adminAccount.getAllowed().contains(testAccount.getId()));
    }
}