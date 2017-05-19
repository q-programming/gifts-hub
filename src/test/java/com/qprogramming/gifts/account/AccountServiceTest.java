package com.qprogramming.gifts.account;

import com.qprogramming.gifts.MockSecurityContext;
import com.qprogramming.gifts.TestUtil;
import com.qprogramming.gifts.account.avatar.Avatar;
import com.qprogramming.gifts.account.avatar.AvatarRepository;
import com.qprogramming.gifts.account.family.Family;
import com.qprogramming.gifts.account.family.FamilyService;
import com.qprogramming.gifts.config.property.PropertyService;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.qprogramming.gifts.TestUtil.USER_RANDOM_ID;
import static org.junit.Assert.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class AccountServiceTest {

    public static final String PASSWORD = "Password";
    public static final String STATIC_IMAGES_LOGO_WHITE_PNG = "static/images/logo-white.png";
    @Mock
    private FamilyService familyServiceMock;
    @Mock
    private MockSecurityContext securityMock;
    @Mock
    private Authentication authMock;
    @Mock
    private AccountRepository accountRepositoryMock;
    @Mock
    private PasswordEncoder passwordEncoderMock;
    @Mock
    private AvatarRepository avatarRepositoryMock;
    @Mock
    private PropertyService propertyServiceMock;
    @Mock
    private HttpServletResponse responseMock;


    private Account testAccount;
    private AccountService accountService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        testAccount = TestUtil.createAccount();
        when(securityMock.getAuthentication()).thenReturn(authMock);
        when(authMock.getPrincipal()).thenReturn(testAccount);
        SecurityContextHolder.setContext(securityMock);
        accountService = new AccountService(accountRepositoryMock, passwordEncoderMock, avatarRepositoryMock, familyServiceMock, propertyServiceMock);
    }


    @Test
    public void createLocalUserAccount() throws Exception {
        Account account = TestUtil.createAccount();
        account.setPassword(PASSWORD);
        when(accountRepositoryMock.findAll()).thenReturn(Collections.singletonList(testAccount));
        when(accountRepositoryMock.save(any(Account.class))).then(returnsFirstArg());
        when(passwordEncoderMock.encode(any())).thenReturn("ENCODED PASS");
        Account result = accountService.createLocalAccount(account);
        assertEquals(result.getRole(), Roles.ROLE_USER);
        verify(accountRepositoryMock, times(1)).save(any(Account.class));
    }

    @Test
    public void createLocalAdminAccount() throws Exception {
        Account account = TestUtil.createAccount();
        account.setPassword(PASSWORD);
        when(accountRepositoryMock.findAll()).thenReturn(Collections.emptyList());
        when(accountRepositoryMock.save(any(Account.class))).then(returnsFirstArg());
        when(passwordEncoderMock.encode(any())).thenReturn("ENCODED PASS");
        Account result = accountService.createLocalAccount(account);
        assertNotEquals(PASSWORD, result.getPassword());
        assertEquals(result.getType(), AccountType.LOCAL);
        assertEquals(result.getRole(), Roles.ROLE_ADMIN);
        verify(accountRepositoryMock, times(1)).save(any(Account.class));
    }


    @Test
    public void createOAuthAdminAccount() throws Exception {
        Account account = TestUtil.createAccount();
        account.setLanguage("");
        when(accountRepositoryMock.findAll()).thenReturn(Collections.emptyList());
        when(accountRepositoryMock.save(any(Account.class))).then(returnsFirstArg());
        Account result = accountService.createOAuthAcount(account);
        assertEquals(result.getRole(), Roles.ROLE_ADMIN);
        verify(accountRepositoryMock, times(1)).save(any(Account.class));
    }

    @Test
    public void createOAuthLocalAccount() throws Exception {
        Account account = TestUtil.createAccount();
        when(accountRepositoryMock.findAll()).thenReturn(Collections.singletonList(testAccount));
        when(accountRepositoryMock.save(any(Account.class))).then(returnsFirstArg());
        Account result = accountService.createOAuthAcount(account);
        assertEquals(result.getRole(), Roles.ROLE_USER);
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
                .thenReturn(account1)
                .thenReturn(account2)
                .thenReturn(null);
        accountService.generateID();
        verify(accountRepositoryMock, times(3)).findOneById(anyString());
    }

    @Test
    public void loadUserByUsername() throws Exception {
        testAccount.setRole(Roles.ROLE_USER);
        when(accountRepositoryMock.findOneByUsername(testAccount.getUsername())).thenReturn(testAccount);
        Account userDetails = (Account) accountService.loadUserByUsername(testAccount.getUsername());
        assertEquals(userDetails, testAccount);
    }

    @Test(expected = UsernameNotFoundException.class)
    public void loadUserByUsernameNotFound() throws Exception {
        accountService.loadUserByUsername(testAccount.getUsername());
    }

    @Test
    public void signIn() throws Exception {
        testAccount.setRole(Roles.ROLE_USER);
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
    public void findWithoutFamily() throws Exception {
        Account account1 = TestUtil.createAccount("John", "Doe");
        account1.setId(USER_RANDOM_ID + "1");
        Account account2 = TestUtil.createAccount("John", "Doe");
        account2.setId(USER_RANDOM_ID + "2");
        Account account3 = TestUtil.createAccount("John", "Doe");
        account3.setId(USER_RANDOM_ID + "3");
        Account account4 = TestUtil.createAccount("John", "Doe");
        account4.setId(USER_RANDOM_ID + "4");
        Account account5 = TestUtil.createAccount("John", "Doe");
        account5.setId(USER_RANDOM_ID + "5");
        Account account6 = TestUtil.createAccount("John", "Doe");
        account6.setId(USER_RANDOM_ID + "6");
        Family family1 = new Family();
        family1.getMembers().addAll(Arrays.asList(account1, account2));
        Family family2 = new Family();
        family1.getMembers().addAll(Arrays.asList(account3, account4));
        List<Account> all = new ArrayList<>();
        all.add(account1);
        all.add(account2);
        all.add(account3);
        all.add(account4);
        all.add(account5);
        all.add(account6);
        when(familyServiceMock.findAll()).thenReturn(Arrays.asList(family1, family2));
        when(accountRepositoryMock.findAll(any(Sort.class))).thenReturn(all);
        List<Account> withoutFamily = accountService.findWithoutFamily();
        assertTrue(withoutFamily.contains(account5));
        assertTrue(withoutFamily.contains(account6));
        assertFalse(withoutFamily.contains(account1));
    }

    @Test
    public void delete() throws Exception {
        Family family = new Family();
        family.getMembers().add(testAccount);
        family.getAdmins().add(testAccount);
        Avatar avatar = new Avatar();
        when(familyServiceMock.getFamily(testAccount)).thenReturn(family);
        when(avatarRepositoryMock.findOneById(testAccount.getId())).thenReturn(avatar);
        accountService.delete(testAccount);
        verify(avatarRepositoryMock, times(1)).delete(avatar);
        verify(familyServiceMock, times(1)).removeFromFamily(testAccount, family);
        verify(accountRepositoryMock, times(1)).delete(testAccount);
    }
}
