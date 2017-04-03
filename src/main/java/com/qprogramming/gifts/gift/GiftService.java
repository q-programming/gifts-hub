package com.qprogramming.gifts.gift;

import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.config.property.PropertyService;
import com.qprogramming.gifts.gift.category.Category;
import com.qprogramming.gifts.support.Utils;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.qprogramming.gifts.settings.Settings.APP_GIFT_AGE;

@Service
public class GiftService {

    private GiftRepository giftRepository;
    private PropertyService propertyService;

    public GiftService(GiftRepository giftRepository, PropertyService propertyService) {
        this.giftRepository = giftRepository;
        this.propertyService = propertyService;
    }

    public Gift create(Gift gift) {
        Account currentAccount = Utils.getCurrentAccount();
        gift.setUserId(currentAccount.getId());
        gift.setCreated(new Date());
        return giftRepository.save(gift);
    }

    public Map<Category, List<Gift>> findAllByCurrentUser() {
        int giftAge = Integer.valueOf(propertyService.getProperty(APP_GIFT_AGE));
        List<Gift> giftList = giftRepository.findByUserIdOrderByCreatedDesc(Utils.getCurrentAccount().getId());
        giftList.forEach(gift -> {
            setGiftStatus(gift, giftAge);
            gift.setClaimed(null);//remove claimed as current user shouldn't see it
        });
        return Utils.toGiftTreeMap(giftList);
    }

    public Map<Category, List<Gift>> findAllByUser(String id) {
        int giftAge = Integer.valueOf(propertyService.getProperty(APP_GIFT_AGE));
        List<Gift> giftList = giftRepository.findByUserIdOrderByCreatedDesc(id);
        giftList.forEach(gift -> setGiftStatus(gift, giftAge));
        return Utils.toGiftTreeMap(giftList);
    }

    public Gift findById(Long id) {
        return giftRepository.findOne(id);
    }

    public Gift update(Gift gift) {
        return giftRepository.save(gift);
    }

    private void setGiftStatus(Gift gift, int giftAge) {
        //TODO set realised
        if (!GiftStatus.REALISED.equals(gift.getStatus())) {
            Days giftDays = Days.daysBetween(new LocalDate(gift.getCreated()), new LocalDate());
            if (giftDays.getDays() < giftAge) {
                gift.setStatus(GiftStatus.NEW);
            }
        }
    }
}
