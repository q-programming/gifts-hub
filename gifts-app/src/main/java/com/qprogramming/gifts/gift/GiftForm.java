package com.qprogramming.gifts.gift;

import io.jsonwebtoken.lang.Collections;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
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
    private List<String> links;
    private List<Long> searchEngines;
    private String category;
    private String username;

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getLinks() {
        if (Collections.isEmpty(links)) {
            links = new ArrayList<>();
        }
        return links;
    }

    public void setLinks(List<String> links) {
        this.links = links;
    }
}
