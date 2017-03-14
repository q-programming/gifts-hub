package com.qprogramming.gifts.gift;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Khobar on 10.03.2017.
 */
@Entity
public class Gift {

    public static final String NAME = "name";
    public static final String LINK = "link";
    public static final String CATEGORY = "category";
    public static final String DESCRIPTION = "description";
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

    @Column
    private String userId;

    @Column
    private Date created;
    @Enumerated(EnumType.STRING)
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
}
