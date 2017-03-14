package com.qprogramming.gifts.gift;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by Khobar on 10.03.2017.
 */
public interface GiftRepository extends JpaRepository<Gift, Long> {

    Gift findOne(Long aLong);
    List<Gift> findByUserIdOrderByCreatedDesc(String id);
}
