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

    /**
     * Finds category by name. If it was not found, it will be created and saved
     *
     * @param name Name of searched category
     * @return found Category, or new Category which was just saved
     */
    public Category findByName(String name) {
        return categoryRepository.findByNameIgnoreCase(name).orElse(save(new Category(name)));
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

    /**
     * Find all categories from list by their id
     *
     * @param ids list of ids of categories
     * @return Category list
     */
    public List<Category> findByIds(List<Long> ids) {
        return categoryRepository.findAllById(ids);
    }

    public void remove(Category category) {
        categoryRepository.delete(category);
    }

    /**
     * Removes all categories in list
     *
     * @param categoriesList list of categories to be removed
     */
    public void removeAll(List<Category> categoriesList) {
        categoryRepository.deleteAll(categoriesList);
    }
}
