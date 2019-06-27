package com.qprogramming.gifts.gift.category;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Set<Category> findByNameContainingIgnoreCase(String term);

    Optional<Category> findByNameIgnoreCase(String category);

}
