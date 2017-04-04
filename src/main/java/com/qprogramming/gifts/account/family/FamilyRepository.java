package com.qprogramming.gifts.account.family;

import com.qprogramming.gifts.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by Khobar on 04.04.2017.
 */
public interface FamilyRepository extends JpaRepository<Family, Long> {

    Family findById(Long id);

    Family findByMembersContaining(Account account);
}
