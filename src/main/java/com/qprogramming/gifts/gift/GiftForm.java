package com.qprogramming.gifts.gift;

import org.hibernate.validator.constraints.NotBlank;

import java.util.List;

/**
 * Created by Khobar on 23.03.2017.
 */
public class GiftForm {
    private Long id;
    @NotBlank
    private String name;
    private String description;
    private String link;
    private List<Long> searchEngines;
    private String category;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
