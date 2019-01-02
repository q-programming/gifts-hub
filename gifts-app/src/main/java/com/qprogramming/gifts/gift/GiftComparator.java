package com.qprogramming.gifts.gift;

import java.util.Comparator;
import java.util.List;

import static java.util.Collections.reverseOrder;
import static java.util.Comparator.naturalOrder;

public class GiftComparator {

    private static Comparator<Gift> giftComparator =
            Comparator
                    .comparing(Gift::getRealised, Comparator.nullsLast(reverseOrder()))
                    .thenComparing(Comparator
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
                            .thenComparing(Gift::getName));
    /**
     * Simple comparator which is not evaluating if gift is claimed. Just realised date, name, created etc. )
     */
    private static Comparator<Gift> simpleGiftComparator =
            Comparator.comparing(Gift::getRealised, Comparator.nullsLast(reverseOrder())).reversed()
                    .thenComparing(Gift::getCreated).reversed()
                    .thenComparing(Gift::getName);

    public static void sortGiftList(List<Gift> list) {
        list.sort(giftComparator);
    }

    public static void simpleSortGiftList(List<Gift> list) {
        list.sort(simpleGiftComparator);
    }


}
