package com.qprogramming.gifts.account.group;

import com.qprogramming.gifts.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;

/**
 * Created by Khobar on 04.04.2017.
 */
public interface GroupRepository extends JpaRepository<Group, Long> {

    Set<Group> findAllByMembers(Account account);
}
