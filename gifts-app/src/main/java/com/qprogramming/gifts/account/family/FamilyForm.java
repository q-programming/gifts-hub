package com.qprogramming.gifts.account.family;

import java.util.Set;

/**
 * Created by Khobar on 04.04.2017.
 */
public class FamilyForm {
    private Long id;
    private String name;
    private Set<String> members;
    private Set<String> admins;

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

    public Set<String> getMembers() {
        return members;
    }

    public void setMembers(Set<String> members) {
        this.members = members;
    }

    public Set<String> getAdmins() {
        return admins;
    }

    public void setAdmins(Set<String> admins) {
        this.admins = admins;
    }
}
