package com.qprogramming.gifts.account.authority;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by Jakub Romaniszyn on 20.07.2018.
 */
public interface AuthorityRepository extends JpaRepository<Authority, Long> {
    Authority findByName(Role name);
}
