package com.qprogramming.gifts.gift;

import com.qprogramming.gifts.account.Account;
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.qprogramming.gifts.TestUtil.createAccount;
import static com.qprogramming.gifts.TestUtil.createGift;
import static org.junit.Assert.assertEquals;

/**
 * Created by XE050991499 on 2017-04-14.
 */
public class GiftComparatorTest {
    @Test
    public void sortGiftList() throws Exception {
        Account testAccount = createAccount();
        //DATA
        Gift oldestGift = createGift(1L, testAccount);
        oldestGift.setName("Oldest");
        oldestGift.setCreated(new DateTime().minusDays(10).toDate());
        Gift gift2days = createGift(2L, testAccount);
        gift2days.setCreated(new DateTime().minusDays(2).toDate());
        gift2days.setName("2 days ago");
        Gift giftZ = createGift(3L, testAccount);
        giftZ.setName("Z gift");
        giftZ.setCreated(new DateTime().minusDays(1).toDate());
        Gift giftA = createGift(4L, testAccount);
        giftA.setCreated(new DateTime().minusDays(1).toDate());
        giftA.setName("A gift");
        Gift claimedGift = createGift(5L, testAccount);
        claimedGift.setCreated(new DateTime().minusDays(1).toDate());
        claimedGift.setClaimed(testAccount);
        claimedGift.setName("Claimed");
        Gift claimedGift2 = createGift(6L, testAccount);
        claimedGift2.setCreated(new DateTime().minusDays(2).toDate());
        claimedGift2.setClaimed(testAccount);
        claimedGift2.setName("Claimed 2 days ago");
        Gift newestGift = createGift(7L, testAccount);
        newestGift.setName("Newest");
        //ADD TO LIST
        List<Gift> list = new ArrayList<>();
        list.add(oldestGift);
        list.add(gift2days);
        list.add(giftZ);
        list.add(giftA);
        list.add(claimedGift);
        list.add(newestGift);
        list.add(claimedGift2);
        //SORT
        GiftComparator.sortGiftList(list);
        //RESULTS CHECK
        assertEquals(list.get(0), newestGift);
        assertEquals(list.get(0), newestGift);
        assertEquals(list.get(6), claimedGift2);
        assertEquals(list.get(4), oldestGift);
    }
}