package com.qprogramming.gifts.gift;

import org.hibernate.validator.constraints.NotBlank;

import java.util.List;

/**
 * Created by Khobar on 23.03.2017.
 */
public class GiftForm {
    @NotBlank
    private String name;
    private String description;
    private String link;
    private List<Long> searchEngines;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    private String category;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public List<Long> getSearchEngines() {
        return searchEngines;
    }

    public void setSearchEngines(List<Long> searchEngines) {
        this.searchEngines = searchEngines;
    }

    public Gift createGift() {
        Gift gift = new Gift();
        gift.setName(getName());
        gift.setDescription(getDescription());
        gift.setLink(getLink());
        return gift;
    }
}
