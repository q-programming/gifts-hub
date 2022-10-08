package com.qprogramming.gifts.account;

import com.qprogramming.gifts.account.authority.Authority;
import com.qprogramming.gifts.account.group.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Created by Khobar on 05.03.2017.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findOneByEmail(String email);

    Optional<Account> findOneByUsername(String username);

    Optional<Account> findOneById(String id);

    Set<Account> findByIdIn(Set<String> list);

    Set<Account> findByEmailIn(Set<String> list);

    Set<Account> findByUsernameIn(Set<String> list);

    List<Account> findByTypeNot(AccountType type);

    List<Account> findByNotificationsIsTrueAndEmailNotNullAndTypeIsNot(AccountType type);

    List<Account> findByAuthorities(Authority roles);

    Set<Account> findByGroupsIn(Set<Group> groups);

    List<Account> findByBirthdayDayAndBirthdayMonth(int day, int month);
}
