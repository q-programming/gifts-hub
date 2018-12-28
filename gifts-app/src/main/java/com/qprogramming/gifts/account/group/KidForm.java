package com.qprogramming.gifts.account.group;

import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.RegisterForm;

import javax.validation.constraints.NotBlank;

/**
 * Created by Remote on 05.04.2017.
 */
public class KidForm {

    private Long groupId;
    private String id;
    @NotBlank(message = RegisterForm.NOT_BLANK_MESSAGE)
    private String name;
    @NotBlank(message = RegisterForm.NOT_BLANK_MESSAGE)
    private String surname;
    @NotBlank(message = RegisterForm.NOT_BLANK_MESSAGE)
    private String username;
    private String avatar;
    private Boolean publicList = false;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Boolean getPublicList() {
        return publicList;
    }

    public void setPublicList(Boolean publicList) {
        this.publicList = publicList;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Account createAccount() {
        Account kid = new Account();
        kid.setName(getName());
        kid.setSurname(getSurname());
        kid.setUsername(getUsername());
        kid.setPublicList(getPublicList());
        return kid;
    }
}
