package com.qprogramming.gifts.settings;

import javax.persistence.*;

/**
 * Created by XE050991499 on 2017-03-21.
 */
@Entity
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

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SearchEngine that = (SearchEngine) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (searchString != null ? !searchString.equals(that.searchString) : that.searchString != null) return false;
        return icon != null ? icon.equals(that.icon) : that.icon == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (searchString != null ? searchString.hashCode() : 0);
        result = 31 * result + (icon != null ? icon.hashCode() : 0);
        return result;
    }
}
