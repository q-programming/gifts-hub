package com.qprogramming.gifts.settings;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by Remote on 19.03.2017.
 */
public interface SearchEngineRepository extends JpaRepository<SearchEngine, Long> {

    SearchEngine findById(Long id);

    SearchEngine findByName(String name);
}
