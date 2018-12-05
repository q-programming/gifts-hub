package com.qprogramming.gifts.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Khobar on 05.03.2017.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Account findOneByEmail(String email);

    Account findOneByUsername(String username);

    Account findOneById(String id);

    List<Account> findByIdIn(List<String> list);

    List<Account> findByRole(Roles role);

    List<Account> findByTypeNot(AccountType type);

    List<Account> findByNewsletterIsTrueAndEmailNotNullAndTypeIsNot(AccountType type);

}
