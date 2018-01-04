package com.qprogramming.gifts.api.user;

import com.fasterxml.uuid.Generators;
import com.qprogramming.gifts.MockSecurityContext;
import com.qprogramming.gifts.account.*;
import com.qprogramming.gifts.account.event.AccountEvent;
import com.qprogramming.gifts.account.event.AccountEventRepository;
import com.qprogramming.gifts.account.event.AccountEventType;
import com.qprogramming.gifts.account.family.Family;
import com.qprogramming.gifts.account.family.FamilyForm;
import com.qprogramming.gifts.account.family.FamilyService;
import com.qprogramming.gifts.account.family.KidForm;
import com.qprogramming.gifts.config.mail.Mail;
import com.qprogramming.gifts.config.mail.MailService;
import com.qprogramming.gifts.config.property.PropertyService;
import com.qprogramming.gifts.gift.Gift;
import com.qprogramming.gifts.gift.GiftService;
import com.qprogramming.gifts.messages.MessagesService;
import com.qprogramming.gifts.schedule.AppEventService;
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
import java.util.HashSet;
import java.util.List;

import static com.qprogramming.gifts.TestUtil.*;
import static org.junit.Assert.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserRestControllerTest {

    private static final String API_USER_REGISTER = "/api/user/register";
    private static final String API_USER_SETTINGS = "/api/user/settings";
    private static final String API_USER_VALIDATE_EMAIL = "/api/user/validate-email";
    private static final String API_USER_VALIDATE_USERNAME = "/api/user/validate-username";
    private static final String API_USER_UPDATE_AVATAR = "/api/user/avatar-upload";
    private static final String API_USER_FAMILY_CREATE = "/api/user/family-create";
    private static final String API_USER_FAMILY_UPDATE = "/api/user/family-update";
    private static final String API_USER_FAMILY_LEAVE = "/api/user/family-leave";
    private static final String API_USER_KID_ADD = "/api/user/kid-add";
    private static final String API_USER_KID_UPDATE = "/api/user/kid-update";
    private static final String API_USER_USER_DELETE = "/api/user/delete/";
    private static final String API_USER_SHARE = "/api/user/share";
    private static final String API_USER_ADMINS = "/api/user/admins";
    private static final String KID_ID = "KID-ID";
    private static final String API_USER_CONFIRM = "/api/user/confirm";
    private static final String API_USER_FETCH = "/api/user/userList";
    private static final String API_USER = "/api/user";
    private MockMvc userRestCtrl;
    @Mock
    private AccountService accSrvMock;
    @Mock
    private AccountEventRepository accountEventRepositoryMock;
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
    @Mock
    private PropertyService propertyServiceMock;
    @Mock
    private AppEventService eventServiceMock;

    private Account testAccount;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        UserRestController userCtrl = new UserRestController(accSrvMock, accountEventRepositoryMock, msgSrvMock, familyServiceMock, giftServiceMock, mailServiceMock, propertyServiceMock, eventServiceMock);
        testAccount = createAccount();
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
        when(accSrvMock.findByEmail(testAccount.getEmail())).thenReturn(testAccount);
        MvcResult mvcResult = userRestCtrl.perform(post(API_USER_REGISTER).contentType(APPLICATION_JSON_UTF8).content(convertObjectToJsonBytes(form)))
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
        MvcResult mvcResult = userRestCtrl.perform(post(API_USER_REGISTER).contentType(APPLICATION_JSON_UTF8).content(convertObjectToJsonBytes(form)))
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
        MvcResult mvcResult = userRestCtrl.perform(post(API_USER_REGISTER).contentType(APPLICATION_JSON_UTF8).content(convertObjectToJsonBytes(form)))
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
                .contentType(APPLICATION_JSON_UTF8)
                .content(object.toString())).andExpect(status().isOk());
        verify(accSrvMock, times(1)).update(testAccount);
    }

    @Test
    public void languageChangedButNoUserFound() throws Exception {
        JSONObject object = new JSONObject();
        object.put("id", USER_RANDOM_ID);
        object.put("language", "pl");
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
        when(accSrvMock.findByEmail(testAccount.getEmail())).thenReturn(testAccount);
        MvcResult mvcResult = userRestCtrl.perform(post(API_USER_VALIDATE_EMAIL)
                .contentType(APPLICATION_JSON_UTF8)
                .content(testAccount.getEmail())).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        assertTrue(contentAsString.contains(ResultData.Code.ERROR.toString()));
    }

    @Test
    public void validateUsernameExists() throws Exception {
        when(accSrvMock.findByUsername(testAccount.getUsername())).thenReturn(testAccount);
        MvcResult mvcResult = userRestCtrl.perform(post(API_USER_VALIDATE_USERNAME)
                .contentType(APPLICATION_JSON_UTF8)
                .content(testAccount.getUsername())).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        assertTrue(contentAsString.contains(ResultData.Code.ERROR.toString()));
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
        userRestCtrl.perform(put(API_USER_FAMILY_CREATE)
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(form))).andExpect(status().isOk());
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
        Account memberAndAdmin = createAccount("John", "Doe");
        memberAndAdmin.setId(USER_RANDOM_ID + "1");
        when(accSrvMock.findByIds(Collections.singletonList(USER_RANDOM_ID + "1"))).thenReturn(Collections.singletonList(memberAndAdmin));
        when(familyServiceMock.createFamily()).thenReturn(family);
        when(familyServiceMock.update(family)).then(returnsFirstArg());
        MvcResult mvcResult = userRestCtrl.perform(put(API_USER_FAMILY_CREATE)
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
        when(familyServiceMock.getFamily(testAccount)).thenReturn(family);
        userRestCtrl.perform(put(API_USER_FAMILY_CREATE)
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(form))).andExpect(status().isBadRequest());
    }


    @Test
    public void updateFamilyNotFound() throws Exception {
        FamilyForm form = new FamilyForm();
        Family family = new Family();
        family.setId(1L);
        userRestCtrl.perform(put(API_USER_FAMILY_UPDATE)
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(form))).andExpect(status().isNotFound());
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
        Account memberAndAdmin = createAccount("John", "Doe");
        memberAndAdmin.setId(USER_RANDOM_ID + "1");
        when(accSrvMock.findByIds(Arrays.asList(USER_RANDOM_ID + "1", testAccount.getId()))).thenReturn(Arrays.asList(memberAndAdmin, testAccount));
        when(familyServiceMock.getFamily(testAccount)).thenReturn(family);
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
        form.setAdmins(Collections.singletonList(USER_RANDOM_ID + "1"));
        form.setMembers(Collections.singletonList(USER_RANDOM_ID + "1"));
        Family family = new Family();
        family.setId(1L);
        family.getMembers().add(testAccount);
        when(familyServiceMock.getFamily(testAccount)).thenReturn(family);
        userRestCtrl.perform(put(API_USER_FAMILY_UPDATE)
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(form))).andExpect(status().isBadRequest());
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
        when(familyServiceMock.getFamily(testAccount)).thenReturn(family);
        when(familyServiceMock.update(family)).then(returnsFirstArg());
        MvcResult mvcResult = userRestCtrl.perform(put(API_USER_FAMILY_LEAVE)).andExpect(status().isOk()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        Family result = convertJsonToObject(contentAsString, Family.class);
        verify(familyServiceMock, times(1)).update(result);
        assertTrue(result.getMembers().size() == 0);

    }

    @Test
    public void addKidNoFamily() throws Exception {
        KidForm form = new KidForm();
        form.setName("John");
        form.setSurname("Doe");
        form.setUsername("john");
        userRestCtrl.perform(post(API_USER_KID_ADD)
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(form))).andExpect(status().isBadRequest());
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
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(form))).andExpect(status().isBadRequest());
    }

    @Test
    public void addKidUsernameExists() throws Exception {
        when(accSrvMock.findByUsername(testAccount.getUsername())).thenReturn(testAccount);
        KidForm form = new KidForm();
        form.setName("John");
        form.setSurname("Doe");
        form.setUsername(testAccount.getUsername());
        userRestCtrl.perform(post(API_USER_KID_ADD)
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(form))).andExpect(status().isBadRequest());
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
        when(familyServiceMock.getFamily(testAccount)).thenReturn(family);
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
        userRestCtrl.perform(post(API_USER_KID_UPDATE)
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(form))).andExpect(status().isBadRequest());
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
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(form))).andExpect(status().isBadRequest());
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
            when(familyServiceMock.getFamily(testAccount)).thenReturn(family);
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
        when(familyServiceMock.getFamily(kid)).thenReturn(family);
        userRestCtrl.perform(delete(API_USER_USER_DELETE + kid.getId())).andExpect(status().isBadRequest());
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
        when(familyServiceMock.getFamily(kid)).thenReturn(family);
        userRestCtrl.perform(delete(API_USER_USER_DELETE + kid.getId())).andExpect(status().isBadRequest());
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
        List<Gift> giftList = Arrays.asList(createGift(1L, testAccount), createGift(2L, testAccount), createGift(3L, testAccount));
        when(accSrvMock.findById(testAccount.getId())).thenReturn(testAccount);
        when(familyServiceMock.getFamily(testAccount)).thenReturn(family);
        userRestCtrl.perform(delete(API_USER_USER_DELETE + testAccount.getId())).andExpect(status().isOk());
        verify(eventServiceMock, times(1)).deleteUserEvents(testAccount);
        verify(giftServiceMock, times(1)).deleteUserGifts(testAccount);
        verify(giftServiceMock, times(1)).deleteClaims(testAccount);
        verify(accSrvMock, times(1)).delete(testAccount);
    }

    @Test
    public void shareGiftListTest() throws Exception {
        testAccount.setPublicList(true);
        userRestCtrl.perform(post(API_USER_SHARE).content("valid@email.com;invalid@;alsovalid@email.pl")).andExpect(status().isOk());
        verify(mailServiceMock, times(1)).shareGiftList((List<Mail>) argThat(IsCollectionWithSize.<Mail>hasSize(2)));

    }

    @Test
    public void shareGiftListNotPublicTest() throws Exception {
        testAccount.setPublicList(false);
        userRestCtrl.perform(post(API_USER_SHARE).content("valid@email.com;invalid@;alsovalid@email.pl")).andExpect(status().isBadRequest());
    }

    @Test
    public void getAdminsNotAdminTest() throws Exception {
        testAccount.setRole(Roles.ROLE_USER);
        userRestCtrl.perform(get(API_USER_ADMINS)).andExpect(status().isForbidden());
    }

    @Test
    public void getAdminsTest() throws Exception {
        testAccount.setRole(Roles.ROLE_ADMIN);
        when(accSrvMock.findAdmins()).thenReturn(Collections.singletonList(testAccount));
        MvcResult mvcResult = userRestCtrl.perform(get(API_USER_ADMINS)).andExpect(status().isOk()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        List<Account> result = convertJsonToList(contentAsString, List.class, Account.class);
        assertTrue(result.contains(testAccount));
    }

    //TODO uncomment after 10.06.2017 :)
//    @Test
//    public void confirmFamilymemberTokenExpired() throws Exception {
//        String token = "09011a27-478c-11e7-bcf7-930b1424157e";
//        userRestCtrl.perform(post(API_USER_CONFIRM).content(token)).andExpect(status().isBadRequest());
//    }

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
        when(familyServiceMock.getFamily(testAccount)).thenReturn(family);
        when(accSrvMock.findEvent(token)).thenReturn(event);
        userRestCtrl.perform(post(API_USER_CONFIRM).content(token)).andExpect(status().isBadRequest());
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
        when(accSrvMock.findEvent(token)).thenReturn(event);
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
        when(accSrvMock.findByUsername(testAccount.getUsername())).thenReturn(testAccount);
        MvcResult mvcResult = userRestCtrl.perform(get(API_USER_FETCH).param("username", testAccount.getUsername()))
                .andExpect(status().isOk()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        List<Account> result = convertJsonToList(contentAsString, List.class, Account.class);
        assertTrue(result.contains(testAccount));
    }


}