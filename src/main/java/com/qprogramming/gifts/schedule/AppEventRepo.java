package com.qprogramming.gifts.schedule;

import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.gift.Gift;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppEventRepo extends JpaRepository<AppEvent, Long> {


    AppEvent findByAccountAndGiftAndType(Account account, Gift gift, AppEventType type);

}
