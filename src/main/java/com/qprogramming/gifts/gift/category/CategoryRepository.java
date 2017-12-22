package com.qprogramming.gifts.gift.category;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Set<Category> findByNameContainingIgnoreCase(String term);

    Category findByName(String category);

    Category findById(Long id);
}
