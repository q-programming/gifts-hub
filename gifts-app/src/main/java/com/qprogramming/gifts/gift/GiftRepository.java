package com.qprogramming.gifts.gift;

import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.gift.category.Category;
import com.qprogramming.gifts.settings.SearchEngine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GiftRepository extends JpaRepository<Gift, Long> {

    List<Gift> findByUserIdOrderByCreatedDesc(String id);

    List<Gift> findByStatusAndRealisedIsNull(GiftStatus status);

    List<Gift> findByUserIdAndStatusNotOrStatusIsNull(String id, GiftStatus status);

    List<Gift> findByClaimed(Account account);

    List<Gift> findAllByCategory(Category category);

    List<Gift> findAllByCategoryIsIn(List<Category> category);

    List<Gift> findByEngines(SearchEngine engines);
}
