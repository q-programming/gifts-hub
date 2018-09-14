package com.qprogramming.gifts.gift;

import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.gift.category.Category;
import com.qprogramming.gifts.gift.link.Link;
import com.qprogramming.gifts.settings.SearchEngine;
import io.jsonwebtoken.lang.Collections;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Entity
public class Gift implements Serializable, Comparable<Gift> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gift_seq_gen")
    @SequenceGenerator(name = "gift_seq_gen", sequenceName = "gift_id_seq", allocationSize = 1)
    private Long id;

    @Column(columnDefinition = "text")
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(columnDefinition = "text")
    private String link;

    @OneToMany
    private List<Link> links;

    @Column
    private String userId;

    @ManyToMany
    @JoinColumn(name = "search_engines")
    private Set<SearchEngine> engines;

    @ManyToOne
    private Category category;

    @ManyToOne
    private Account claimed;

    @Column
    private Date created;

    @Column
    private GiftStatus status;

    public Gift() {
        this.created = new Date();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public GiftStatus getStatus() {
        return status;
    }

    public void setStatus(GiftStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<SearchEngine> getEngines() {
        return engines;
    }

    public void setEngines(Set<SearchEngine> engines) {
        this.engines = engines;
    }

    public Category getCategory() {
        if (category == null) {
            Category category = new Category(StringUtils.EMPTY);
            category.setPriority(Integer.MIN_VALUE + 1);
            return category;
        }
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Account getClaimed() {
        return claimed;
    }

    public void setClaimed(Account claimed) {
        this.claimed = claimed;
    }

    public List<Link> getLinks() {
        if (Collections.isEmpty(links)) {
            links = new ArrayList<>();
        }
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Gift gift = (Gift) o;

        if (!id.equals(gift.id)) return false;
        if (!name.equals(gift.name)) return false;
        if (!userId.equals(gift.userId)) return false;
        return created != null ? created.equals(gift.created) : gift.created == null;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + userId.hashCode();
        result = 31 * result + (created != null ? created.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Gift{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }

    @Override
    public int compareTo(Gift gift) {

        return 0;
    }

    public void addLink(Link link) {
        this.getLinks().add(link);
    }
}
