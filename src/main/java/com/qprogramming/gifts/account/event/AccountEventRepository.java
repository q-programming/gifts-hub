package com.qprogramming.gifts.account.event;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountEventRepository extends JpaRepository<AccountEvent, Long> {

    AccountEvent findById(Long id);

    AccountEvent findByUuid(String uuid);
}
