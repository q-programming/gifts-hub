package com.qprogramming.gifts.gift.category;

import io.jsonwebtoken.lang.Collections;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CategoriesDTO {

    private List<Category> categories;
    private String name;

    public List<Category> getCategories() {
        if (Collections.isEmpty(categories)) {
            categories = new ArrayList<>();
        }
        return categories;
    }
}
