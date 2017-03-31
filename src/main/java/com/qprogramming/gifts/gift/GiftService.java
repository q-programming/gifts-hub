package com.qprogramming.gifts.gift;

import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.gift.category.Category;
import com.qprogramming.gifts.support.Utils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class GiftService {

    private GiftRepository giftRepository;

    public GiftService(GiftRepository giftRepository) {
        this.giftRepository = giftRepository;
    }

    public Gift create(Gift gift) {
        Account currentAccount = Utils.getCurrentAccount();
        gift.setUserId(currentAccount.getId());
        gift.setStatus(GiftStatus.NEW);
        return giftRepository.save(gift);
    }

    public Map<Category, List<Gift>> findAllByCurrentUser() {
        return Utils.toGiftTreeMap(giftRepository.findByUserIdOrderByCreatedDesc(Utils.getCurrentAccount().getId()));

    }

    public Map<Category, List<Gift>> findAllByUser(String id) {
        return Utils.toGiftTreeMap(giftRepository.findByUserIdOrderByCreatedDesc(id));
    }

    public Gift findById(Long id) {
        return giftRepository.findOne(id);
    }

    public Gift update(Gift gift) {
        return giftRepository.save(gift);
    }
}
