package com.qprogramming.gifts.gift;

import com.qprogramming.gifts.MockSecurityContext;
import com.qprogramming.gifts.TestUtil;
import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.AccountService;
import com.qprogramming.gifts.config.property.PropertyService;
import com.qprogramming.gifts.gift.category.Category;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.qprogramming.gifts.settings.Settings.APP_GIFT_AGE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
    @Mock
    private PropertyService propertyServiceMock;

    private GiftService giftService;
    private Account testAccount;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        giftService = new GiftService(giftRepositoryMock, propertyServiceMock);
        testAccount = TestUtil.createAccount();
        when(securityMock.getAuthentication()).thenReturn(authMock);
        when(authMock.getPrincipal()).thenReturn(testAccount);
        when(propertyServiceMock.getProperty(APP_GIFT_AGE)).thenReturn("14");
        SecurityContextHolder.setContext(securityMock);
    }

    @Test
    public void createGiftSuccess() {
        Gift gift = createGift(1L);
        when(giftRepositoryMock.save(gift)).thenReturn(gift);
        Gift expected = giftService.create(gift);
        assertEquals(expected, gift);
    }

    private Gift createGift(long id) {
        Gift gift = new Gift();
        gift.setId(id);
        gift.setName("name");
        gift.setLink("http://google.ocm");
        gift.setUserId(testAccount.getId());
        return gift;
    }

    @Test
    public void findAllByCurrentUser() throws Exception {
        Gift gift1 = createGift(1L);
        Gift gift2 = createGift(2L);
        gift2.setCreated(new LocalDate().minusMonths(3).toDate());
        Gift gift3 = createGift(3L);
        gift3.setCreated(new LocalDate().minusDays(13).toDate());
        Gift gift4 = createGift(4L);
        gift3.setCreated(new LocalDate().minusDays(14).toDate());
        List<Gift> giftList = Arrays.asList(gift1, gift2, gift3, gift4);
        when(giftRepositoryMock.findByUserIdOrderByCreatedDesc(TestUtil.USER_RANDOM_ID)).thenReturn(giftList);
        Map<Category, List<Gift>> result = giftService.findAllByCurrentUser();
        assertTrue(result.get(gift1.getCategory())
                .stream()
                .filter(gift -> gift.getStatus() == GiftStatus.NEW).toArray().length == 2);
    }

    @Test
    public void findAllByUser() throws Exception {
        Gift gift1 = createGift(1L);
        Gift gift2 = createGift(2L);
        gift2.setCreated(new LocalDate().minusMonths(3).toDate());
        List<Gift> giftList = Arrays.asList(gift1, gift2);
        when(giftRepositoryMock.findByUserIdOrderByCreatedDesc(TestUtil.USER_RANDOM_ID)).thenReturn(giftList);
        Map<Category, List<Gift>> result = giftService.findAllByUser(testAccount.getId());
        assertTrue(result.get(gift1.getCategory())
                .stream()
                .filter(gift -> gift.getStatus() == GiftStatus.NEW).toArray().length == 1);
    }


}