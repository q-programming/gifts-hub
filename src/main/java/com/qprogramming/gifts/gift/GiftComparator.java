package com.qprogramming.gifts.gift;

import java.util.Comparator;
import java.util.List;

public class GiftComparator {

    private static Comparator<Gift> giftComparator = Comparator
            .nullsLast((Gift g1, Gift g2) -> {
                if (g1.getClaimed() != null && g2.getClaimed() != null) {
                    return 0;
                }
                if (g1.getClaimed() != null && g2.getClaimed() == null) {
                    return -1;
                }
                if (g2.getClaimed() != null && g1.getClaimed() == null) {
                    return 1;
                }
                return 0;
            })
            .thenComparing(Gift::getCreated).reversed()
            .thenComparing(Gift::getName);

    public static void sortGiftList(List<Gift> list) {
        list.sort(giftComparator);
    }


}
