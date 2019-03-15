package com.qprogramming.gifts.gift;

import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.gift.category.Category;
import com.qprogramming.gifts.settings.SearchEngine;
import io.jsonwebtoken.lang.Collections;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static com.qprogramming.gifts.support.Utils.GIFT_COMPARATOR;

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

    @ElementCollection
    private Set<String> links = new HashSet<>();

    @Column
    private String userId;

    @Column
    private String createdBy;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "search_engines")
    private Set<SearchEngine> engines;

    @ManyToOne
    private Category category;

    @ManyToOne
    private Account claimed;

    @Column
    private Date created;

    @Column
    private Date realised;

    @Column
    private GiftStatus status;

    @Column
    private Boolean hidden = false;

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
        if (Collections.isEmpty(engines)) {
            this.engines = new HashSet<>();
        }
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

    public Set<String> getLinks() {
        if (Collections.isEmpty(links)) {
            links = new HashSet<>();
        }
        return links;
    }

    public void setLinks(Set<String> links) {
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
        return GIFT_COMPARATOR.compare(this, gift);
    }

    public void addLink(String link) {
        this.getLinks().add(link);
    }

    public Boolean isHidden() {
        return hidden != null ? hidden : false;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getRealised() {
        return realised;
    }

    public void setRealised(Date realised) {
        this.realised = realised;
    }
}
