package com.qprogramming.gifts.account.family;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by Khobar on 04.04.2017.
 */
public interface FamilyEventRepository extends JpaRepository<FamilyEvent, Long> {

    FamilyEvent findById(Long id);

    FamilyEvent findByUuid(String uuid);
}
