package com.qprogramming.gifts.gift.category;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class CategoryService {

    private CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category findByName(String name) {
        return categoryRepository.findByName(name);
    }

    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public Set<Category> findByNameContainingIgnoreCase(String term) {
        return categoryRepository.findByNameContainingIgnoreCase(term);
    }

    public void update(List<Category> categories) {
        categoryRepository.saveAll(categories);
    }

    public Category findById(Long id) {
        return id != null ? categoryRepository.findById(id).orElse(null) : null;
    }

    public void remove(Category category) {
        categoryRepository.delete(category);
    }
}
