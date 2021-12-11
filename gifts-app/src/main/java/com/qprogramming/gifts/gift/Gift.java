package com.qprogramming.gifts.gift;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.gift.category.Category;
import com.qprogramming.gifts.gift.image.GiftImage;
import com.qprogramming.gifts.settings.SearchEngine;
import io.jsonwebtoken.lang.Collections;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static com.qprogramming.gifts.support.Utils.GIFT_COMPARATOR;

@Entity
@Getter
@Setter
@NoArgsConstructor
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
    private Date created = new Date();

    @Column
    private Date realised;

    @Column
    private GiftStatus status;

    @Column
    private Boolean hidden = false;

    @Transient
    private String imageData;

    @OneToOne(fetch = FetchType.LAZY)
    private GiftImage image;

    @Column
    private Boolean hasImage = false;

    public Set<SearchEngine> getEngines() {
        if (Collections.isEmpty(engines)) {
            this.engines = new HashSet<>();
        }
        return engines;
    }

    public Category getCategory() {
        if (category == null) {
            Category category = new Category(StringUtils.EMPTY);
            category.setPriority(Integer.MIN_VALUE + 1);
            return category;
        }
        return category;
    }

    public Set<String> getLinks() {
        if (Collections.isEmpty(links)) {
            links = new HashSet<>();
        }
        return links;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Gift gift = (Gift) o;

        if (!id.equals(gift.id)) return false;
        if (!name.equals(gift.name)) return false;
        if (!userId.equals(gift.userId)) return false;
        return Objects.equals(created, gift.created);
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

    @JsonProperty
    public String getImageData() {
        return imageData;
    }

    @JsonProperty
    public void setImageData(String imageData) {
        this.imageData = imageData;
    }

    public GiftImage getImage() {
        return image;
    }

    public void setImage(GiftImage image) {
        this.image = image;
    }

    public Boolean isHasImage() {
        if (hasImage == null) {
            hasImage = false;
        }
        return hasImage;
    }

    public void setHasImage(Boolean hasImage) {
        this.hasImage = hasImage;
    }
}
