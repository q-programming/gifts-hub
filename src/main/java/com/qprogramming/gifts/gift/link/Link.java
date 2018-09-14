package com.qprogramming.gifts.gift.link;

import javax.persistence.*;
import java.util.Objects;

@Entity
public class Link {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "link_seq_gen")
    @SequenceGenerator(name = "link_seq_gen", sequenceName = "link_id_seq", allocationSize = 1)
    private Long id;

    @Column
    private String url;

    public Link() {
    }

    public Link(String url) {
        this.url = url;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Link link = (Link) o;
        return Objects.equals(id, link.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
