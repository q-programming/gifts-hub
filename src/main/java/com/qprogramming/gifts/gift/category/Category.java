package com.qprogramming.gifts.gift.category;

import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;

@Entity
public class Category implements Comparable<Category> {

    public static final String REALISED = "REALISED";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cat_seq_gen")
    @SequenceGenerator(name = "cat_seq_gen", sequenceName = "cat_id_seq", allocationSize = 1)
    private Long id;

    @Column
    private String name;

    @Column
    private Integer priority;

    public Category(String name) {
        this.name = name;
        this.priority = 0;
    }

    public Category() {
        this.id = (long) Integer.MIN_VALUE;
        this.priority = 0;
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

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return id.equals(category.id);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(Category o) {
        if (this.priority < o.priority) {
            return 1;
        } else if (this.priority > o.priority) {
            return -1;
        }
        return this.name.compareTo(o.getName());
    }

    @Override
    public String toString() {
        if (StringUtils.isEmpty(name)) {
            return StringUtils.EMPTY;
        } else {
            return name;
        }
    }
}
