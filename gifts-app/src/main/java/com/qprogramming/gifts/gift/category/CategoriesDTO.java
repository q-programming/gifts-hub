package com.qprogramming.gifts.gift.category;

import io.jsonwebtoken.lang.Collections;

import java.util.ArrayList;
import java.util.List;

public class CategoriesDTO {

    private List<Category> categories;
    private String name;

    public List<Category> getCategories() {
        if (Collections.isEmpty(categories)) {
            categories = new ArrayList<>();
        }
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
