package com.qprogramming.gifts.account.group;

import com.qprogramming.gifts.MockSecurityContext;
import com.qprogramming.gifts.TestUtil;
import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.AccountType;
import com.qprogramming.gifts.account.event.AccountEventRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static junit.framework.TestCase.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Mockito.*;

/**
 * Created by Khobar on 04.04.2017.
 */
public class GroupServiceTest {
    Account testAccount;
    GroupService groupService;
    @Mock
    private GroupRepository groupRepositoryMock;
    @Mock
    private AccountEventRepository accountEventRepositoryMock;
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
        groupService = new GroupService(groupRepositoryMock, accountEventRepositoryMock);
    }

    @Test
    public void createGroup() throws Exception {
        when(groupRepositoryMock.save(any(Group.class))).then(returnsFirstArg());
        Group result = groupService.createGroup(anyString());
        assertNotNull(result);
        verify(groupRepositoryMock, times(1)).save(any(Group.class));
    }

    @Test
    public void addAccountToGroupAdminsWithId() throws Exception {
        Group group = new Group();
        group.setId(2L);
        when(groupRepositoryMock.findById(2L)).thenReturn(Optional.of(group));
        when(groupRepositoryMock.save(any(Group.class))).then(returnsFirstArg());
        Group result = groupService.addAccountToGroupAdmins(testAccount, 2L);
        assertNotNull(result);
        assertTrue(result.getAdmins().contains(testAccount));
        verify(groupRepositoryMock, times(1)).save(any(Group.class));
    }

    @Test
    public void addAccountToGroupAdminsWithIdNotFound() throws Exception {
        when(groupRepositoryMock.findById(2L)).thenReturn(Optional.empty());
        Group group = groupService.addAccountToGroupAdmins(testAccount, 2L);
        assertNull(group);
    }

    @Test
    public void removeFromGroupLastMember() throws Exception {
        Group group = new Group();
        group.setId(1L);
        group.addMember(testAccount);
        group.getAdmins().add(testAccount);
        groupService.removeFromGroup(testAccount, group);
        verify(groupRepositoryMock, times(1)).delete(group);
    }

    @Test
    public void removeFromGroupGrantAdmin() throws Exception {
        Account account = TestUtil.createAccount("John", "Doe");
        account.setId("USER_ID");
        account.setType(AccountType.LOCAL);
        Account kid = TestUtil.createAccount("Little", "Kid");
        kid.setId("USER_ID2");
        kid.setType(AccountType.KID);
        Group group = new Group();
        group.setId(1L);
        group.addMember(testAccount);
        group.addMember(account);
        group.addMember(kid);
        group.getAdmins().add(testAccount);
        groupService.removeFromGroup(testAccount, group);
        assertTrue(group.getAdmins().contains(account));
    }

    @Test
    public void removeFromGroupLastAccountButKidLeft() throws Exception {
        Account kid = TestUtil.createAccount("Little", "Kid");
        kid.setId("USER_ID2");
        kid.setType(AccountType.KID);
        Group group = new Group();
        group.setId(1L);
        group.addMember(testAccount);
        group.addMember(kid);
        group.getAdmins().add(testAccount);
        groupService.removeFromGroup(testAccount, group);
        assertTrue(group.getAdmins().isEmpty());
        assertTrue(group.getMembers().isEmpty());
        verify(groupRepositoryMock, times(1)).delete(group);

    }


    @Test
    public void removeFromGroupOtherMembersLeft() throws Exception {
        Account account = TestUtil.createAccount("John", "Doe");
        account.setId("USER_ID");
        account.setType(AccountType.LOCAL);
        Account kid = TestUtil.createAccount("Little", "Kid");
        kid.setId("USER_ID2");
        kid.setType(AccountType.KID);
        Group group = new Group();
        group.setId(1L);
        group.addMember(testAccount);
        group.addMember(account);
        group.addMember(kid);
        group.getAdmins().add(account);
        groupService.removeFromGroup(testAccount, group);
        assertFalse(group.getMembers().contains(testAccount));
    }

    @Test
    public void sendGroupInvite() {

    }


}