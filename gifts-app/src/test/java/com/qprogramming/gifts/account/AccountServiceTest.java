package com.qprogramming.gifts.account;

import com.qprogramming.gifts.MockedAccountTestBase;
import com.qprogramming.gifts.TestUtil;
import com.qprogramming.gifts.account.authority.Authority;
import com.qprogramming.gifts.account.authority.AuthorityRepository;
import com.qprogramming.gifts.account.authority.AuthorityService;
import com.qprogramming.gifts.account.authority.Role;
import com.qprogramming.gifts.account.avatar.Avatar;
import com.qprogramming.gifts.account.avatar.AvatarRepository;
import com.qprogramming.gifts.account.event.AccountEventRepository;
import com.qprogramming.gifts.account.group.Group;
import com.qprogramming.gifts.account.group.GroupService;
import com.qprogramming.gifts.config.property.PropertyService;
import com.qprogramming.gifts.gift.GiftService;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

import static com.qprogramming.gifts.TestUtil.createAccountList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Mockito.*;

public class AccountServiceTest extends MockedAccountTestBase {

    public static final String PASSWORD = "Password";
    public static final String STATIC_IMAGES_LOGO_WHITE_PNG = "static/images/logo-white.png";
    public static final String STATIC_AVATAR_PLACEHOLDER = "static/images/avatar-placeholder.png";
    @Mock
    private GroupService groupServiceMock;
    @Mock
    private AccountRepository accountRepositoryMock;
    @Mock
    private AccountPasswordEncoder passwordEncoderMock;
    @Mock
    private AvatarRepository avatarRepositoryMock;
    @Mock
    private PropertyService propertyServiceMock;
    @Mock
    private HttpServletResponse responseMock;
    @Mock
    private AccountEventRepository accountEventRepositoryMock;
    @Mock
    private GiftService giftServiceMock;
    @Mock
    private AuthorityRepository authorityRepositoryMock;


    private AccountService accountService;

    @Before
    public void setUp() throws Exception {
        super.setup();
        when(giftServiceMock.countAllByAccountId(anyString())).thenReturn(1);
        AuthorityService authorityService = new AuthorityService(authorityRepositoryMock);
        accountService = new AccountService(accountRepositoryMock, passwordEncoderMock, avatarRepositoryMock, groupServiceMock, propertyServiceMock, accountEventRepositoryMock, giftServiceMock, authorityService) {
            @Override
            protected byte[] downloadFromUrl(URL url) {
                ClassLoader loader = getClass().getClassLoader();
                try (InputStream avatarFile = loader.getResourceAsStream(STATIC_AVATAR_PLACEHOLDER)) {
                    return IOUtils.toByteArray(avatarFile);
                } catch (IOException e) {
                    fail();
                }
                return new byte[0];
            }
        };
    }

    @Test
    public void generateIDFails2TimesTest() throws Exception {
        Optional<Account> account1 = Optional.of(TestUtil.createAccount());
        Optional<Account> account2 = Optional.of(TestUtil.createAccount());
        when(accountRepositoryMock.findOneById(anyString()))
                .thenReturn(account1)
                .thenReturn(account2)
                .thenReturn(Optional.empty());
        accountService.generateID();
        verify(accountRepositoryMock, times(3)).findOneById(anyString());
    }


    @Test
    public void createLocalUserAccount() throws Exception {
        Account account = TestUtil.createAccount();
        account.setPassword(PASSWORD);
        when(accountRepositoryMock.findAll()).thenReturn(Collections.singletonList(testAccount));
        when(accountRepositoryMock.findOneById(anyString())).thenReturn(Optional.empty());
        when(accountRepositoryMock.save(any(Account.class))).then(returnsFirstArg());
        when(passwordEncoderMock.encode(any())).thenReturn("ENCODED PASS");
        when(authorityRepositoryMock.save(any(Authority.class))).then(returnsFirstArg());
        Account result = accountService.createLocalAccount(account);
        assertThat(result.getAuthorities().stream().anyMatch(o -> ((Authority) o).getName().equals(Role.ROLE_USER))).isTrue();
        verify(accountRepositoryMock, times(1)).save(any(Account.class));
    }

    @Test
    public void createLocalAdminAccount() throws Exception {
        Account account = TestUtil.createAccount();
        account.setPassword(PASSWORD);
        when(accountRepositoryMock.findAll()).thenReturn(Collections.emptyList());
        when(accountRepositoryMock.save(any(Account.class))).then(returnsFirstArg());
        when(passwordEncoderMock.encode(any())).thenReturn("ENCODED PASS");
        when(authorityRepositoryMock.save(any(Authority.class))).then(returnsFirstArg());
        Account result = accountService.createLocalAccount(account);
        assertNotEquals(PASSWORD, result.getPassword());
        assertEquals(result.getType(), AccountType.LOCAL);
        assertThat(result.getAuthorities().stream().anyMatch(o -> ((Authority) o).getName().equals(Role.ROLE_ADMIN))).isTrue();
        verify(accountRepositoryMock, times(1)).save(any(Account.class));
    }


    @Test
    public void createOAuthAdminAccount() throws Exception {
        Account account = TestUtil.createAccount();
        account.setLanguage("");
        when(accountRepositoryMock.findAll()).thenReturn(Collections.emptyList());
        when(accountRepositoryMock.save(any(Account.class))).then(returnsFirstArg());
        when(authorityRepositoryMock.save(any(Authority.class))).then(returnsFirstArg());
        Account result = accountService.createAcount(account);
        assertThat(result.getIsAdmin()).isTrue();
        verify(accountRepositoryMock, times(1)).save(any(Account.class));
    }

    @Test
    public void createOAuthLocalAccount() throws Exception {
        Account account = TestUtil.createAccount();
        when(accountRepositoryMock.findAll()).thenReturn(Collections.singletonList(testAccount));
        when(accountRepositoryMock.save(any(Account.class))).then(returnsFirstArg());
        when(authorityRepositoryMock.save(any(Authority.class))).then(returnsFirstArg());
        Account result = accountService.createAcount(account);
        assertThat(result.getIsUser()).isTrue();
        assertThat(result.getIsAdmin()).isFalse();
        verify(accountRepositoryMock, times(1)).save(any(Account.class));
    }

    @Test
    public void createKidAccount() throws Exception {
        Account account = TestUtil.createAccount();
        when(accountRepositoryMock.save(any(Account.class))).then(returnsFirstArg());
        Account result = accountService.createKidAccount(account);
        assertEquals(result.getType(), AccountType.KID);
        verify(accountRepositoryMock, times(1)).save(any(Account.class));
    }

    @Test
    public void generateIDFails2Times() throws Exception {
        Account account1 = TestUtil.createAccount();
        Account account2 = TestUtil.createAccount();
        when(accountRepositoryMock.findOneById(anyString()))
                .thenReturn(Optional.of(account1))
                .thenReturn(Optional.of(account2))
                .thenReturn(Optional.empty());
        accountService.generateID();
        verify(accountRepositoryMock, times(3)).findOneById(anyString());
    }

    @Test
    public void loadUserByUsername() throws Exception {
        when(accountRepositoryMock.findOneByUsername(testAccount.getUsername())).thenReturn(Optional.of(testAccount));
        Account userDetails = (Account) accountService.loadUserByUsername(testAccount.getUsername());
        assertEquals(userDetails, testAccount);
    }

    @Test(expected = UsernameNotFoundException.class)
    public void loadUserByUsernameNotFound() throws Exception {
        accountService.loadUserByUsername(testAccount.getUsername());
    }

    @Test
    public void signIn() throws Exception {
        accountService.signin(testAccount);
        verify(securityMock, times(1)).setAuthentication(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    public void findById() throws Exception {
    }

    @Test
    public void getAccountAvatar() throws Exception {
        when(avatarRepositoryMock.findOneById(testAccount.getId())).thenReturn(new Avatar());
        Avatar accountAvatar = accountService.getAccountAvatar(testAccount);
        assertNotNull(accountAvatar);
    }

    @Test
    public void createAvatar() throws Exception {
        ClassLoader loader = this.getClass().getClassLoader();
        try (InputStream avatarFile = loader.getResourceAsStream(STATIC_IMAGES_LOGO_WHITE_PNG)) {
            accountService.updateAvatar(testAccount, IOUtils.toByteArray(avatarFile));
            verify(avatarRepositoryMock, times(1)).save(any(Avatar.class));
        }
    }

    @Test
    public void createAvatarUknownType() throws Exception {
        ClassLoader loader = this.getClass().getClassLoader();
        accountService.updateAvatar(testAccount, STATIC_IMAGES_LOGO_WHITE_PNG.getBytes());
        verify(avatarRepositoryMock, times(1)).save(any(Avatar.class));
    }


    @Test
    public void updateAvatar() throws Exception {
        ClassLoader loader = this.getClass().getClassLoader();
        try (InputStream avatarFile = loader.getResourceAsStream(STATIC_IMAGES_LOGO_WHITE_PNG)) {
            when(avatarRepositoryMock.findOneById(testAccount.getId())).thenReturn(new Avatar());
            accountService.updateAvatar(testAccount, IOUtils.toByteArray(avatarFile));
            verify(avatarRepositoryMock, times(1)).save(any(Avatar.class));
        }
    }

    @Test(expected = IOException.class)
    public void createAvatarFromUrlError() throws Exception {
        accountService.createAvatar(testAccount, STATIC_IMAGES_LOGO_WHITE_PNG);
    }

    @Test
    public void createAvatarFromUrl() throws Exception {
        accountService.createAvatar(testAccount, "http://google.com");
        verify(avatarRepositoryMock, times(1)).save(any(Avatar.class));
    }

    @Test
    public void delete() throws Exception {
        Group group = new Group();
        group.addMember(testAccount);
        group.getAdmins().add(testAccount);
        Avatar avatar = new Avatar();
        when(groupServiceMock.findAllAccountGroups(testAccount)).thenReturn(Collections.singleton(group));
        when(avatarRepositoryMock.findOneById(testAccount.getId())).thenReturn(avatar);
        accountService.delete(testAccount);
        verify(avatarRepositoryMock, times(1)).delete(avatar);
        verify(groupServiceMock, times(1)).removeFromGroup(testAccount, group);
        verify(accountRepositoryMock, times(1)).delete(testAccount);
    }

    @Test
    public void findAllSortByFamily() throws Exception {
        List<Account> all = createAccountList();
        all.add(testAccount);
        Group group1 = new Group();
        Account andyAccount = all.get(1);
        Account bobAccount = all.get(0);
        group1.addMember(andyAccount);
        group1.addMember(bobAccount);
        group1.addMember(testAccount);
        testAccount.setGroups(Collections.singleton(group1));
        when(accountRepositoryMock.findByGroupsIn(testAccount.getGroups())).thenReturn(group1.getMembers());
        when(groupServiceMock.findAllAccountGroups(testAccount)).thenReturn(Collections.singleton(group1));
        Set<Account> result = accountService.findAllFromGroups(testAccount);
        //convert result to array to test order
        Object[] ordered = result.toArray();
        assertEquals(ordered[0], andyAccount);
        assertEquals(ordered[1], bobAccount);
        assertEquals(ordered[2], testAccount);
    }

    @Test
    public void findAllSortByGroupNoGroup() throws Exception {
        when(accountRepositoryMock.findByGroupsIn(testAccount.getGroups())).thenReturn(Collections.emptySet());
        Set<Account> result = accountService.findAllFromGroups(testAccount);
        //convert result to array to test order
        Object[] ordered = result.toArray();
        assertTrue(result.size() == 1);
        result.contains(testAccount);
    }


}
