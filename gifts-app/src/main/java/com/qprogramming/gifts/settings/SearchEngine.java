package com.qprogramming.gifts.settings;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;

/**
 * Created by XE050991499 on 2017-03-21.
 */
@Entity
@Getter
@Setter
public class SearchEngine {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "search_seq_gen")
    @SequenceGenerator(name = "search_seq_gen", sequenceName = "search_id_seq", allocationSize = 1)
    private Long id;
    @Column
    private String name;
    @Column
    private String searchString;
    @Column(columnDefinition = "text")
    private String icon;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchEngine that = (SearchEngine) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "SearchEngine{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
