package com.qprogramming.gifts.account.family;

import java.util.List;

/**
 * Created by Khobar on 04.04.2017.
 */
public class FamilyForm {
    private Long id;
    private List<String> members;
    private List<String> admins;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public List<String> getAdmins() {
        return admins;
    }

    public void setAdmins(List<String> admins) {
        this.admins = admins;
    }
}
