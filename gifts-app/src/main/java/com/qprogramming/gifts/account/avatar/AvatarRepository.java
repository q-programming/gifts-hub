package com.qprogramming.gifts.account.avatar;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Khobar on 05.03.2017.
 */
@Repository
public interface AvatarRepository extends JpaRepository<Avatar, Long> {

    Avatar findOneById(String id);
}
