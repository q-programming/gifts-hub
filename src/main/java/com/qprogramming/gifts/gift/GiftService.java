package com.qprogramming.gifts.gift;

import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.AccountService;
import com.qprogramming.gifts.support.Utils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Khobar on 10.03.2017.
 */
@Service
public class GiftService {

    private AccountService accountService;
    private GiftRepository giftRepository;

    public GiftService(AccountService accountService, GiftRepository giftRepository) {
        this.accountService = accountService;
        this.giftRepository = giftRepository;
    }

    public Gift create(Gift gift) {
        Account currentAccount = Utils.getCurrentAccount();
        gift.setUserId(currentAccount.getId());
        gift.setStatus(GiftStatus.NEW);
        return giftRepository.save(gift);
    }

    public List<Gift> findAllByCurrentUser() {
        return giftRepository.findByUserId(Utils.getCurrentAccount().getId());
    }

    public List<Gift> findAllByUser(String id) {
        return giftRepository.findByUserId(id);
    }
}
