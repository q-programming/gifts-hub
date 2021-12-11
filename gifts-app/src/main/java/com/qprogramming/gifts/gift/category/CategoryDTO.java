package com.qprogramming.gifts.gift.category;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CategoryDTO {

    public CategoryDTO(Category category, Long count) {
        this.category = category;
        this.count = count;
    }

    private Category category;
    private Long count;

    @Override
    public String toString() {
        return "CategoryDTO{" +
                "category=" + category +
                ", count=" + count +
                '}';
    }
}
