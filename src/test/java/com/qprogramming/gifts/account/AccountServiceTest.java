package com.qprogramming.gifts.account;

import com.qprogramming.gifts.MockSecurityContext;
import com.qprogramming.gifts.TestUtil;
import com.qprogramming.gifts.account.avatar.AvatarRepository;
import com.qprogramming.gifts.account.family.Family;
import com.qprogramming.gifts.account.family.FamilyService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.qprogramming.gifts.TestUtil.USER_RANDOM_ID;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Created by Khobar on 10.03.2017.
 */
public class AccountServiceTest {
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


    private Account testAccount;
    private AccountService accountService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        testAccount = TestUtil.createAccount();
        when(securityMock.getAuthentication()).thenReturn(authMock);
        when(authMock.getPrincipal()).thenReturn(testAccount);
        SecurityContextHolder.setContext(securityMock);
        accountService = new AccountService(accountRepositoryMock, passwordEncoderMock, avatarRepositoryMock, familyServiceMock);
    }


    @Test
    public void create() throws Exception {
    }

    @Test
    public void loadUserByUsername() throws Exception {
    }

    @Test
    public void findById() throws Exception {
    }

    @Test
    public void getAccountAvatar() throws Exception {
    }

    @Test
    public void createAvatar() throws Exception {
    }

    @Test
    public void update() throws Exception {
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

}
