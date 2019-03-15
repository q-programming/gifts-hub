package com.qprogramming.gifts.gift;

import com.qprogramming.gifts.account.Account;
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
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
        Gift realisedGift = createGift(1L, testAccount);
        realisedGift.setName("Realised");
        realisedGift.setCreated(new DateTime().minusDays(9).toDate());
        realisedGift.setRealised(new DateTime().minusDays(5).toDate());
        Gift oldestGiftButRealised = createGift(1L, testAccount);
        oldestGiftButRealised.setName("Oldest Realised");
        oldestGiftButRealised.setCreated(new DateTime().minusDays(10).toDate());
        oldestGiftButRealised.setRealised(new DateTime().minusDays(4).toDate());
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

        List<Gift> list = Arrays.asList(oldestGift, oldestGiftButRealised, gift2days, giftZ, giftA, claimedGift, newestGift, claimedGift2, realisedGift);
        ArrayList<Gift> simpleSortList = new ArrayList<>(list);
        ArrayList<Gift> sortList = new ArrayList<>(list);

        //SORT
        GiftComparator.sortGiftList(sortList);
        GiftComparator.simpleSortGiftList(simpleSortList);
        //RESULTS CHECK
        assertEquals(realisedGift, sortList.get(1));
        assertEquals(newestGift, sortList.get(2));
        assertEquals(oldestGift, sortList.get(6));
        assertEquals(claimedGift, sortList.get(7));
        assertEquals(claimedGift2, sortList.get(8));
        assertEquals(realisedGift, simpleSortList.get(1));
        assertEquals(newestGift, simpleSortList.get(2));
    }
}