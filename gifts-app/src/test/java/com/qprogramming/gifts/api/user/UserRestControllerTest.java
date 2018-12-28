package com.qprogramming.gifts.api.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.uuid.Generators;
import com.qprogramming.gifts.MockedAccountTestBase;
import com.qprogramming.gifts.TestUtil;
import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.AccountService;
import com.qprogramming.gifts.account.AccountType;
import com.qprogramming.gifts.account.RegisterForm;
import com.qprogramming.gifts.account.event.AccountEvent;
import com.qprogramming.gifts.account.event.AccountEventType;
import com.qprogramming.gifts.account.group.Group;
import com.qprogramming.gifts.account.group.GroupForm;
import com.qprogramming.gifts.account.group.GroupService;
import com.qprogramming.gifts.account.group.KidForm;
import com.qprogramming.gifts.config.MappingConfiguration;
import com.qprogramming.gifts.config.mail.Mail;
import com.qprogramming.gifts.config.mail.MailService;
import com.qprogramming.gifts.exceptions.AccountNotFoundException;
import com.qprogramming.gifts.exceptions.GroupNotAdminException;
import com.qprogramming.gifts.exceptions.GroupNotFoundException;
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
    private static final String API_USER_GROUP_CREATE = "/api/account/group-create";
    private static final String API_USER_GROUP_UPDATE = "/api/account/group-update";
    private static final String API_USER_GROUP_LEAVE = "/api/account/group-leave";
    private static final String API_USER_KID_ADD = "/api/account/kid-add";
    private static final String API_USER_KID_UPDATE = "/api/account/kid-update";
    private static final String API_USER_USER_DELETE = "/api/account/delete/";
    private static final String API_USER_SHARE = "/api/account/share";
    private static final String API_USER_ADMINS = "/api/account/admins";
    private static final String KID_ID = "KID-ID";
    private static final String API_USER_CONFIRM = "/api/account/confirm";
    private static final String API_USER_FETCH = "/api/account/userList";
    private static final String API_USER = "/api/account";
    private static final String API_ALLOWED_ACCOUNT_ADD = API_USER + "/allowed/account/add";
    private static final String API_ALLOWED_ACCOUNT_REMOVE = API_USER + "/allowed/account/remove";
    private MockMvc userRestCtrl;
    @Mock
    private AccountService accSrvMock;
    @Mock
    private MessagesService msgSrvMock;
    @Mock
    private GroupService groupServiceMock;
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
        UserRestController userCtrl = new UserRestController(accSrvMock, msgSrvMock, groupServiceMock, giftServiceMock, mailServiceMock, eventServiceMock, logoutHandlerMock);
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
    public void createGroupNoMembers() throws Exception {
        GroupForm form = new GroupForm();
        Group group = new Group();
        group.setId(1L);
        group.addMember(testAccount);
        group.getAdmins().add(testAccount);
        when(groupServiceMock.createGroup(anyString())).thenReturn(group);
        when(groupServiceMock.update(group)).then(returnsFirstArg());
        userRestCtrl.perform(post(API_USER_GROUP_CREATE)
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(form))).andExpect(status().isOk());
        verify(groupServiceMock, times(1)).update(any(Group.class));
    }

    @Test
    public void createGroupMembersAndAdmins() throws Exception {
        GroupForm form = new GroupForm();
        form.setAdmins(Collections.singleton(USERNAME + "1"));
        form.setMembers(Collections.singleton((USERNAME + "1")));
        Group group = new Group();
        group.setId(1L);
        group.addMember(testAccount);
        group.getAdmins().add(testAccount);
        Account memberAndAdmin = createAccount("John", "Doe");
        memberAndAdmin.setUsername(USERNAME + "1");
        AccountEvent event = new AccountEvent();
        event.setAccount(testAccount);
        event.setGroup(group);
        event.setType(AccountEventType.GROUP_MEMEBER);
        event.setToken("aaa");
        when(accSrvMock.findByEmailsOrUsernames(Collections.singleton(USERNAME + "1"))).thenReturn(Collections.singleton(memberAndAdmin));
        when(groupServiceMock.createGroup(anyString())).thenReturn(group);
        when(groupServiceMock.inviteAccount(any(Account.class), any(Group.class), any(AccountEventType.class))).thenReturn(event);
        when(groupServiceMock.update(group)).then(returnsFirstArg());
        MvcResult mvcResult = userRestCtrl.perform(post(API_USER_GROUP_CREATE)
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(form))).andExpect(status().isOk()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        verify(groupServiceMock, times(1)).update(any(Group.class));
        verify(groupServiceMock, times(1)).inviteAccount(memberAndAdmin, group, AccountEventType.GROUP_MEMEBER);
        verify(mailServiceMock, times(1)).sendConfirmMail(any(Mail.class), any(AccountEvent.class));
    }

    @Test
    public void updateGroupNotFound() throws Exception {
        GroupForm form = new GroupForm();
        form.setId(1L);
        when(groupServiceMock.getGroupAsGroupAdmin(form.getId())).thenThrow(GroupNotFoundException.class);
        userRestCtrl.perform(put(API_USER_GROUP_UPDATE)
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(form))).andExpect(status().isNotFound());
    }

    @Test
    public void updateGroupAddMemberAndAdmin() throws Exception {
        GroupForm form = new GroupForm();
        form.setId(1L);
        HashSet<String> members = new HashSet<>();
        HashSet<String> admins = new HashSet<>();
        String newUsername = USERNAME + "1";
        members.add(newUsername);
        members.add(testAccount.getUsername());
        admins.add(newUsername);
        admins.add(testAccount.getUsername());
        form.setAdmins(admins);
        form.setMembers(members);
        Group group = new Group();
        group.setId(1L);
        group.addMember(testAccount);
        group.getAdmins().add(testAccount);
        Account memberAndAdmin = createAdminAccount();
        memberAndAdmin.setUsername(newUsername);
        HashSet<Account> dbMembers = new HashSet<>();
        dbMembers.add(testAccount);
        dbMembers.add(memberAndAdmin);
        HashSet<Account> dbAdmins = new HashSet<>();
        dbAdmins.add(testAccount);
        dbAdmins.add(memberAndAdmin);
        when(accSrvMock.findByEmailsOrUsernames(members)).thenReturn(dbMembers).thenReturn(dbAdmins);
        when(groupServiceMock.getGroupAsGroupAdmin(form.getId())).thenReturn(group);
        when(groupServiceMock.update(group)).then(returnsFirstArg());
        MvcResult mvcResult = userRestCtrl.perform(put(API_USER_GROUP_UPDATE)
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(form))).andExpect(status().isOk()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        verify(groupServiceMock, times(1)).inviteAccount(memberAndAdmin, group, AccountEventType.GROUP_ADMIN);
        assertTrue(group.getMembers().size() == 1);
    }

    @Test
    public void updateGroupNotAdmin() throws Exception {
        GroupForm form = new GroupForm();
        form.setId(1L);
        form.setAdmins(Collections.singleton(USER_RANDOM_ID + "1"));
        form.setMembers(Collections.singleton(USER_RANDOM_ID + "1"));
        Group group = new Group();
        group.setId(1L);
        group.addMember(testAccount);
        when(groupServiceMock.getGroupAsGroupAdmin(form.getId())).thenThrow(GroupNotAdminException.class);
        userRestCtrl.perform(put(API_USER_GROUP_UPDATE)
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(form))).andExpect(status().isForbidden());
    }

    @Test
    public void leaveGroupNoGroup() throws Exception {
        GroupForm form = new GroupForm();
        form.setId(1L);
        userRestCtrl.perform(put(API_USER_GROUP_LEAVE).contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(form))).andExpect(status().isNotFound());

    }

    @Test
    public void leaveGroup() throws Exception {
        GroupForm form = new GroupForm();
        form.setId(1L);
        Group group = new Group();
        group.setId(1L);
        group.addMember(testAccount);
        when(groupServiceMock.getGroupById(1L)).thenReturn(Optional.of(group));
        when(groupServiceMock.update(group)).then(returnsFirstArg());
        MvcResult mvcResult = userRestCtrl.perform(put(API_USER_GROUP_LEAVE).contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(form))).andExpect(status().isOk()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        verify(groupServiceMock, times(1)).removeFromGroup(testAccount, group);

    }

    @Test
    public void addKidNoGroup() throws Exception {
        KidForm form = new KidForm();
        form.setName("John");
        form.setSurname("Doe");
        form.setUsername("john");
        form.setGroupId(1L);
        when(groupServiceMock.getGroupAsGroupAdmin(1L)).thenThrow(GroupNotFoundException.class);
        userRestCtrl.perform(post(API_USER_KID_ADD)
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(form))).andExpect(status().is4xxClientError());
    }

    @Test
    public void addKidNotGroupAdmin() throws Exception {
        Group group = new Group();
        group.setId(1L);
        group.addMember(testAccount);
        when(groupServiceMock.getGroupAsGroupAdmin(group.getId())).thenThrow(GroupNotAdminException.class);
        KidForm form = new KidForm();
        form.setName("John");
        form.setSurname("Doe");
        form.setUsername("john");
        form.setGroupId(group.getId());
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
        form.setGroupId(1L);
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
            form.setGroupId(1L);
            Group group = new Group();
            group.setId(1L);
            group.addMember(testAccount);
            group.getAdmins().add(testAccount);
            when(groupServiceMock.getGroupAsGroupAdmin(form.getGroupId())).thenReturn(group);
            Account kid = form.createAccount();
            kid.setId(USER_RANDOM_ID + (Math.random() * 100));
            kid.setType(AccountType.KID);
            when(accSrvMock.createKidAccount(any(Account.class))).thenReturn(kid);
            userRestCtrl.perform(post(API_USER_KID_ADD)
                    .contentType(APPLICATION_JSON_UTF8)
                    .content(convertObjectToJsonBytes(form))).andExpect(status().isOk());
            group.addMember(kid);
            verify(accSrvMock, times(1)).createKidAccount(any(Account.class));
            verify(accSrvMock, times(1)).updateAvatar(kid, imgBytes);
            verify(groupServiceMock, times(1)).update(group);
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
        form.setGroupId(1L);
        Group group = new Group();
        group.setId(1L);
        group.addMember(testAccount);
        group.getAdmins().add(testAccount);
        when(groupServiceMock.getGroupAsGroupAdmin(1L)).thenReturn(group);
        when(accSrvMock.findById(KID_ID)).thenThrow(AccountNotFoundException.class);
        userRestCtrl.perform(post(API_USER_KID_UPDATE)
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(form))).andExpect(status().isNotFound());
    }

    @Test
    public void updateKidGroupNotFount() throws Exception {
        KidForm form = new KidForm();
        form.setName("name");
        form.setSurname("surname");
        form.setUsername("username");
        form.setId(KID_ID);
        form.setGroupId(1L);
        when(groupServiceMock.getGroupAsGroupAdmin(form.getGroupId())).thenThrow(GroupNotFoundException.class);
        userRestCtrl.perform(post(API_USER_KID_UPDATE)
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(form))).andExpect(status().is4xxClientError());
    }

    @Test
    public void updateKidGroupNotAdmin() throws Exception {
        KidForm form = new KidForm();
        form.setName("name");
        form.setSurname("surname");
        form.setUsername("username");
        form.setId(KID_ID);
        form.setGroupId(1L);
        Group group = new Group();
        group.setId(1L);
        group.addMember(testAccount);
        when(groupServiceMock.getGroupAsGroupAdmin(form.getGroupId())).thenThrow(GroupNotAdminException.class);
        userRestCtrl.perform(post(API_USER_KID_UPDATE)
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(form))).andExpect(status().is4xxClientError());
    }

    @Test
    public void updateKidGroup() throws Exception {
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
            form.setGroupId(1L);
            Group group = new Group();
            group.setId(1L);
            group.addMember(testAccount);
            group.addMember(kidAccount);
            group.getAdmins().add(testAccount);
            when(groupServiceMock.getGroupAsGroupAdmin(form.getGroupId())).thenReturn(group);
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
    public void testDeserialization() throws JsonProcessingException {
        Account kidAccount = createAccount("name", "surname");
        kidAccount.setId(KID_ID);
        Group group = new Group();
        group.setId(1L);
        group.addMember(testAccount);
        group.addMember(kidAccount);
        group.getAdmins().add(testAccount);
        ObjectMapper mapper = new ObjectMapper();
        String result = mapper
                .writerWithView(MappingConfiguration.Public.class)
                .writeValueAsString(group);
        System.out.println(result);
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

//TODO needs fix
    //    @Test
//    public void deleteKidNotGroupAdmin() throws Exception {
//        Account kid = createAccount("Little", "Kid");
//        kid.setId(USER_RANDOM_ID);
//        kid.setType(AccountType.KID);
//        Group group = new Group();
//        group.setId(1L);
//        group.getMembers().add(kid);
//        when(accSrvMock.findById(kid.getId())).thenReturn(kid);
//        when(groupServiceMock.getGroup(kid)).thenReturn(Optional.of(group));
//        userRestCtrl.perform(delete(API_USER_USER_DELETE + kid.getId())).andExpect(status().is4xxClientError());
//    }

    @Test
    public void deleteKidAdminButAccountNotKid() throws Exception {
        Account kid = createAccount("Little", "Kid");
        kid.setId(USER_RANDOM_ID);
        kid.setType(AccountType.LOCAL);
        Group group = new Group();
        group.setId(1L);
        group.addMember(kid);
        group.addMember(testAccount);
        group.getAdmins().add(testAccount);
        when(accSrvMock.findById(kid.getId())).thenReturn(kid);
        when(accSrvMock.isAccountGroupAdmin(kid)).thenReturn(false);
        userRestCtrl.perform(delete(API_USER_USER_DELETE + kid.getId())).andExpect(status().is4xxClientError());
    }

    @Test
    public void deleteKid() throws Exception {
        Account kid = createAccount("Little", "Kid");
        kid.setId(USER_RANDOM_ID);
        kid.setType(AccountType.KID);
        Group group = new Group();
        group.setId(1L);
        group.addMember(kid);
        group.addMember(testAccount);
        group.getAdmins().add(testAccount);
        List<Gift> giftList = Arrays.asList(createGift(1L, kid), createGift(2L, kid), createGift(3L, kid));
        when(accSrvMock.findById(kid.getId())).thenReturn(kid);
        when(accSrvMock.isAccountGroupAdmin(kid)).thenReturn(true);
        userRestCtrl.perform(delete(API_USER_USER_DELETE + kid.getId())).andExpect(status().isOk());
        verify(giftServiceMock, times(1)).deleteUserGifts(kid);
        verify(accSrvMock, times(1)).delete(kid);
    }

    @Test
    public void deleteAccount() throws Exception {
        Group group = new Group();
        group.setId(1L);
        group.addMember(testAccount);
        group.addMember(testAccount);
        group.getAdmins().add(testAccount);
        List<Gift> giftList = Arrays.asList(createGift(1L, testAccount), createGift(2L, testAccount), createGift(3L, testAccount));
        when(accSrvMock.findById(testAccount.getId())).thenReturn(testAccount);
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
    public void confirmGroupmemberTokenExpired() throws Exception {
        String token = "09011a27-478c-11e7-bcf7-930b1424157e";
        AccountEvent event = new AccountEvent();
        event.setToken(token);
        event.setAccount(testAccount);
        when(accSrvMock.findEvent(token)).thenReturn(Optional.of(event));
        userRestCtrl.perform(post(API_USER_CONFIRM).content(token)).andExpect(status().isConflict());
    }

    @Test
    public void confirmGroupmemberTokenNotFound() throws Exception {
        String token = "09011a27-478c-11e7-bcf7-930b1424157e";
        when(accSrvMock.findEvent(token)).thenReturn(Optional.empty());
        userRestCtrl.perform(post(API_USER_CONFIRM).content(token)).andExpect(status().isNotFound());
    }

    @Test
    public void confirmGroupmemberSuccessTest() throws Exception {
        String token = Generators.timeBasedGenerator().generate().toString();
        Group group = new Group();
        AccountEvent event = new AccountEvent();
        event.setToken(token);
        event.setAccount(testAccount);
        event.setGroup(group);
        event.setType(AccountEventType.GROUP_MEMEBER);
        when(accSrvMock.findEvent(token)).thenReturn(Optional.of(event));
        when(accSrvMock.getCurrentAccount()).thenReturn(testAccount);
        when(groupServiceMock.getGroupFromEvent(event)).thenReturn(group);
        userRestCtrl.perform(post(API_USER_CONFIRM).content(token)).andExpect(status().isOk());
        assertTrue(group.getMembers().contains(testAccount));
    }

    @Test
    public void confirmGroupmemberEventNotFoundTest() throws Exception {
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
        when(accSrvMock.findAllFromGroups(testAccount)).thenReturn(new HashSet<>(accountList));
        MvcResult mvcResult = userRestCtrl.perform(get(API_USER_FETCH)).andExpect(status().isOk()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        List<Account> result = convertJsonToList(contentAsString, List.class, Account.class);
        assertTrue(result.contains(testAccount));
    }

    @Test
    public void userSearchListForOtherUserTest() throws Exception {
        List<Account> accountList = createAccountList();
        accountList.add(testAccount);
        when(accSrvMock.findAllFromGroups(testAccount)).thenReturn(new HashSet<>(accountList));
        when(accSrvMock.findByUsername(testAccount.getUsername())).thenReturn(Optional.of(testAccount));
        MvcResult mvcResult = userRestCtrl.perform(get(API_USER_FETCH).param("username", testAccount.getUsername()))
                .andExpect(status().isOk()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        List<Account> result = convertJsonToList(contentAsString, List.class, Account.class);
        assertTrue(result.contains(testAccount));
    }
//
//    @Test
//    public void addAccountToAllowed() throws Exception {
//        Account account = createAdminAccount();
//        when(accSrvMock.findById(account.getId())).thenReturn(account);
//        when(accSrvMock.getCurrentAccount()).thenReturn(testAccount);
//        userRestCtrl.perform(put(API_ALLOWED_ACCOUNT_ADD).content(account.getId()))
//                .andExpect(status().isOk());
//        verify(accSrvMock, times(1)).update(testAccount);
//        assertTrue(testAccount.getAllowed().contains(account.getId()));
//    }
//
//    @Test
//    public void addAccountToAllowedNotFound() throws Exception {
//        Account account = createAdminAccount();
//        when(accSrvMock.findById(account.getId())).thenThrow(AccountNotFoundException.class);
//        when(accSrvMock.getCurrentAccount()).thenReturn(testAccount);
//        userRestCtrl.perform(put(API_ALLOWED_ACCOUNT_ADD).content(account.getId()))
//                .andExpect(status().isNotFound());
//        assertFalse(testAccount.getAllowed().contains(account.getId()));
//    }
//
//    @Test
//    public void removeAccountFromAllowedNotFound() throws Exception {
//        testAccount.getAllowed().add(ADMIN_RANDOM_ID);
//        when(accSrvMock.findById(ADMIN_RANDOM_ID)).thenThrow(AccountNotFoundException.class);
//        when(accSrvMock.getCurrentAccount()).thenReturn(testAccount);
//        userRestCtrl.perform(delete(API_ALLOWED_ACCOUNT_REMOVE).content(ADMIN_RANDOM_ID))
//                .andExpect(status().isNotFound());
//        assertTrue(testAccount.getAllowed().contains(ADMIN_RANDOM_ID));
//    }
//
//    @Test
//    public void removeAccountFromAllowed() throws Exception {
//        Account account = createAdminAccount();
//        testAccount.getAllowed().add(account.getId());
//        when(accSrvMock.findById(account.getId())).thenReturn(account);
//        when(accSrvMock.getCurrentAccount()).thenReturn(testAccount);
//        userRestCtrl.perform(delete(API_ALLOWED_ACCOUNT_REMOVE).content(account.getId()))
//                .andExpect(status().isOk());
//        verify(accSrvMock, times(1)).update(testAccount);
//        assertFalse(testAccount.getAllowed().contains(account.getId()));
//    }

//    @Test
//    public void confirmAddGroupToAllowedNotFound() throws Exception {
//        String token = Generators.timeBasedGenerator().generate().toString();
//        AccountEvent event = new AccountEvent();
//        event.setType(GROUP_ALLOW_GROUP);
//        event.setAccount(testAccount);
//        when(accSrvMock.findEvent(token)).thenReturn(Optional.of(event));
//        when(groupServiceMock.getGroupAsGroupAdmin()).thenThrow(GroupNotFoundException.class);
//        userRestCtrl.perform(put(API_USER_CONFIRM)
//                .contentType(APPLICATION_JSON_UTF8)
//                .content(token))
//                .andExpect(status().isNotFound());
//    }
//
//    @Test
//    public void confirmAddGroupToAllowedNotAdmin() throws Exception {
//        String token = Generators.timeBasedGenerator().generate().toString();
//        AccountEvent event = new AccountEvent();
//        event.setAccount(testAccount);
//        event.setType(GROUP_ALLOW_GROUP);
//        when(accSrvMock.findEvent(token)).thenReturn(Optional.of(event));
//        when(groupServiceMock.getGroupAsGroupAdmin()).thenThrow(GroupNotAdminException.class);
//        userRestCtrl.perform(put(API_USER_CONFIRM)
//                .contentType(APPLICATION_JSON_UTF8)
//                .content(token))
//                .andExpect(status().isForbidden());
//    }
//
//    @Test
//    public void confirmAddGroupToAllowedNotFoundEvent() throws Exception {
//        String token = Generators.timeBasedGenerator().generate().toString();
//        when(accSrvMock.findEvent(anyString())).thenReturn(Optional.empty());
//        Group group = new Group();
//        when(groupServiceMock.getGroupAsGroupAdmin()).thenReturn(group);
//        when(groupServiceMock.getGroupById(1L)).thenReturn(Optional.empty());
//        userRestCtrl.perform(put(API_USER_CONFIRM)
//                .contentType(APPLICATION_JSON_UTF8)
//                .content(token))
//                .andExpect(status().isNotFound());
//    }
//
//    @Test
//    public void confirmAddGroupToAllowed() throws Exception {
//        Account adminAccount = createAdminAccount();
//        Account normalAccount = createAccount("John", "Doe");
//        Group targetGroup = new Group();
//        targetGroup.getMembers().add(testAccount);
//        targetGroup.getMembers().add(normalAccount);
//        Group sourceGroup = new Group();
//        sourceGroup.getMembers().add(adminAccount);
//        String token = Generators.timeBasedGenerator().generate().toString();
//        AccountEvent event = new AccountEvent();
//        event.setAccount(testAccount);
//        event.setGroup(sourceGroup);
//        event.setType(GROUP_ALLOW_GROUP);
//        when(accSrvMock.findEvent(token)).thenReturn(Optional.of(event));
//        when(groupServiceMock.getGroupAsGroupAdmin()).thenReturn(targetGroup);
//        doCallRealMethod().when(accSrvMock).addAllowedToGroup(any(Group.class), anySet());
//        userRestCtrl.perform(put(API_USER_CONFIRM)
//                .contentType(APPLICATION_JSON_UTF8)
//                .content(token))
//                .andExpect(status().isOk());
//        verify(groupServiceMock, times(2)).update(any());
//        assertTrue(testAccount.getAllowed().contains(adminAccount.getId()));
//        assertTrue(adminAccount.getAllowed().contains(testAccount.getId()));
//    }
}