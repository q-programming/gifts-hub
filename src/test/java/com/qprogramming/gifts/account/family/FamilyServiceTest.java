package com.qprogramming.gifts.account.family;

import com.qprogramming.gifts.MockSecurityContext;
import com.qprogramming.gifts.TestUtil;
import com.qprogramming.gifts.account.Account;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static junit.framework.TestCase.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by Khobar on 04.04.2017.
 */
public class FamilyServiceTest {

    Account testAccount;
    FamilyService familyService;
    @Mock
    FamilyRepository familyRepositoryMock;
    @Mock
    private MockSecurityContext securityMock;
    @Mock
    private Authentication authMock;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        testAccount = TestUtil.createAccount();
        when(securityMock.getAuthentication()).thenReturn(authMock);
        when(authMock.getPrincipal()).thenReturn(testAccount);
        SecurityContextHolder.setContext(securityMock);
        familyService = new FamilyService(familyRepositoryMock);
    }

    @Test
    public void createFamily() throws Exception {
        when(familyRepositoryMock.save(any(Family.class))).then(returnsFirstArg());
        Family result = familyService.createFamily();
        assertNotNull(result);
        assertTrue(result.getAdmins().contains(testAccount));
        assertTrue(result.getMembers().contains(testAccount));
        verify(familyRepositoryMock, times(3)).save(any(Family.class));
    }

    @Test
    public void addAccountToFamilyAdminsWithId() throws Exception {
        Family family = new Family();
        family.setId(2L);
        when(familyRepositoryMock.findById(2L)).thenReturn(family);
        when(familyRepositoryMock.save(any(Family.class))).then(returnsFirstArg());
        Family result = familyService.addAccountToFamilyAdmins(testAccount, 2L);
        assertNotNull(result);
        assertTrue(result.getAdmins().contains(testAccount));
        verify(familyRepositoryMock, times(1)).save(any(Family.class));
    }

    @Test
    public void addAccountToFamilyWithId() throws Exception {
        Family family = new Family();
        family.setId(2L);
        when(familyRepositoryMock.findById(2L)).thenReturn(family);
        when(familyRepositoryMock.save(any(Family.class))).then(returnsFirstArg());
        Family result = familyService.addAccountToFamily(testAccount, 2L);
        assertNotNull(result);
        assertTrue(result.getMembers().contains(testAccount));
        verify(familyRepositoryMock, times(1)).save(any(Family.class));
    }

    @Test
    public void addAccountToFamilyAdminsWithIdNotFound() throws Exception {
        Family family = familyService.addAccountToFamilyAdmins(testAccount, 2L);
        assertNull(family);
    }

    @Test
    public void addAccountToFamilyWithIdNotFound() throws Exception {
        Family result = familyService.addAccountToFamily(testAccount, 2L);
        assertNull(result);
    }

    @Test
    public void getAccountFamily() {
        Family family = new Family();
        family.setId(2L);
        family.getMembers().add(testAccount);
        when(familyRepositoryMock.findByMembersContaining(testAccount)).thenReturn(family);
        Family result = familyService.getFamily(testAccount);
        assertNotNull(result);
        assertTrue(family.getMembers().contains(testAccount));
    }

}