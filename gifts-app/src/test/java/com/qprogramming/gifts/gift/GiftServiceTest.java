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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.qprogramming.gifts.TestUtil.createGift;
import static com.qprogramming.gifts.settings.Settings.APP_GIFT_AGE;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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
        Gift gift = createGift(1L, testAccount);
        when(giftRepositoryMock.save(gift)).thenReturn(gift);
        Gift expected = giftService.create(gift);
        assertEquals(expected, gift);
    }

    @Test
    public void findAllByCurrentUser() throws Exception {
        Gift newGift = createGift(1L, testAccount);
        newGift.setName("New");
        Gift oldestGift = createGift(2L, testAccount);
        oldestGift.setCreated(new LocalDate().minusMonths(3).toDate());
        oldestGift.setName("Oldest");
        Gift newButNotNewest = createGift(3L, testAccount);
        newButNotNewest.setCreated(new LocalDate().minusDays(13).toDate());
        newButNotNewest.setName("13 days old gift");
        Gift oldGift = createGift(4L, testAccount);
        oldGift.setCreated(new LocalDate().minusDays(14).toDate());
        oldGift.setName("14 days old gift");
        List<Gift> giftList = Arrays.asList(newGift, oldestGift, newButNotNewest, oldGift);
        when(giftRepositoryMock.findByUserIdOrderByCreatedDesc(TestUtil.USER_RANDOM_ID)).thenReturn(giftList);
        Map<Category, List<Gift>> result = giftService.findAllByCurrentUser();
        assertTrue(result.get(newGift.getCategory())
                .stream()
                .filter(gift -> gift.getStatus() == GiftStatus.NEW).toArray().length == 2);
    }

    @Test
    public void findAllByUser() throws Exception {
        Gift gift1 = createGift(1L, testAccount);
        Gift gift2 = createGift(2L, testAccount);
        gift2.setCreated(new LocalDate().minusMonths(3).toDate());
        List<Gift> giftList = Arrays.asList(gift1, gift2);
        when(giftRepositoryMock.findByUserIdOrderByCreatedDesc(TestUtil.USER_RANDOM_ID)).thenReturn(giftList);
        Map<Category, List<Gift>> result = giftService.findAllByUser(testAccount.getId());
        assertTrue(result.get(gift1.getCategory())
                .stream()
                .filter(gift -> gift.getStatus() == GiftStatus.NEW).toArray().length == 1);
    }

    @Test
    public void deleteClaims() throws Exception {
        Account account = TestUtil.createAccount("John", "Doe");
        account.setId("USER");
        Gift gift1 = createGift(1L, account);
        gift1.setClaimed(testAccount);
        when(giftRepositoryMock.findByClaimed(testAccount)).thenReturn(Collections.singletonList(gift1));
        giftService.deleteClaims(testAccount);
        verify(giftRepositoryMock, times(1)).saveAll(anyCollection());
        assertNull(gift1.getClaimed());
    }

    @Test
    public void deleteCategory() {
        Category category = new Category("category");
        category.setId(1L);
        Gift gift1 = createGift(1L, testAccount);
        gift1.setCategory(category);
        Gift gift2 = createGift(2L, testAccount);
        gift2.setCategory(category);
        Gift gift3 = createGift(3L, testAccount);
        gift3.setCategory(category);
        Gift gift4 = createGift(4L, testAccount);
        gift4.setCategory(category);
        when(giftRepositoryMock.findAllByCategory(category)).thenReturn(Arrays.asList(gift1, gift2, gift3, gift4));
        giftService.removeCategory(category);
        verify(giftRepositoryMock, times(1)).saveAll(anyList());
        assertNull(gift1.getCategory().getId());
        assertNull(gift2.getCategory().getId());
        assertNull(gift3.getCategory().getId());
        assertNull(gift4.getCategory().getId());
    }


}