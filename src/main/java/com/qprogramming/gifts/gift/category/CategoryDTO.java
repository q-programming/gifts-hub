package com.qprogramming.gifts.gift.category;

public class CategoryDTO {

    public CategoryDTO() {
    }

    public CategoryDTO(Category category, Long count) {
        this.category = category;
        this.count = count;
    }

    private Category category;
    private Long count;

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "CategoryDTO{" +
                "category=" + category +
                ", count=" + count +
                '}';
    }
}
