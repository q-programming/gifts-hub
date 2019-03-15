package com.qprogramming.gifts.account.event;

import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.group.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountEventRepository extends JpaRepository<AccountEvent, Long> {

    Optional<AccountEvent> findById(Long id);

    Optional<AccountEvent> findByToken(String token);

    List<AccountEvent> findAllByAccount(Account account);

    List<AccountEvent> findAllByGroup(Group group);
}
