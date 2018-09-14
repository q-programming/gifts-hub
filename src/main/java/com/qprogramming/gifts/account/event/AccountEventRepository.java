package com.qprogramming.gifts.account.event;

import com.qprogramming.gifts.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountEventRepository extends JpaRepository<AccountEvent, Long> {

    AccountEvent findById(Long id);

    AccountEvent findByToken(String token);

    List<AccountEvent> findAllByAccount(Account account);
}
