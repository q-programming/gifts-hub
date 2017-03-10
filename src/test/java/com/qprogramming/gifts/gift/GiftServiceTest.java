package com.qprogramming.gifts.gift;

import com.qprogramming.gifts.MockSecurityContext;
import com.qprogramming.gifts.TestUtil;
import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.AccountService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Created by Khobar on 10.03.2017.
 */

public class GiftServiceTest {

    @Mock
    private AccountService accSrvMock;
    @Mock
    private GiftRepository giftRepositoryMock;
    @Mock
    private MockSecurityContext securityMock;
    @Mock
    private Authentication authMock;

    private GiftService giftService;
    private Account testAccount;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        giftService = new GiftService(accSrvMock, giftRepositoryMock);
        testAccount = TestUtil.createAccount();
        when(securityMock.getAuthentication()).thenReturn(authMock);
        when(authMock.getPrincipal()).thenReturn(testAccount);
        SecurityContextHolder.setContext(securityMock);
    }

    @Test
    public void createGiftSuccess() {
        Gift gift = new Gift();
        gift.setId(1L);
        gift.setName("name");
        gift.setLink("http://google.ocm");
        gift.setUserId(testAccount.getId());
        gift.setStatus(GiftStatus.NEW);
        when(giftRepositoryMock.save(gift)).thenReturn(gift);
        Gift expected = giftService.create(gift);
        assertEquals(expected, gift);
    }


}