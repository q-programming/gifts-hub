package com.qprogramming.gifts.account.event;

import com.qprogramming.gifts.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountEventRepository extends JpaRepository<AccountEvent, Long> {

    Optional<AccountEvent> findById(Long id);

    AccountEvent findByToken(String token);

    List<AccountEvent> findAllByAccount(Account account);
}
