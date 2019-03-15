package com.qprogramming.gifts.schedule;

import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.gift.Gift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppEventRepo extends JpaRepository<AppEvent, Long> {


    AppEvent findByAccountAndGiftAndType(Account account, Gift gift, AppEventType type);

    List<AppEvent> findByAccountAndGift(Account account, Gift gift);

    List<AppEvent> findByAccount(Account account);

}
