package com.qprogramming.gifts.account;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;

/**
 * Created by Khobar on 28.02.2017.
 */
@Entity
public class Account {

    @Id
    private String id;

    @Column(unique = true)
    private String email;
    @JsonIgnore
    private String password;
    @Column
    private String language = "en";
    @Column
    private String name;
    @Column
    private String surname;

    private String role = "ROLE_USER";

    private Instant created;

    public Account() {
        this.created = Instant.now();
    }

    public Account(String email, String password, String role_user) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.created = Instant.now();

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Instant getCreated() {
        return created;
    }

    public void setCreated(Instant created) {
        this.created = created;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Account account = (Account) o;

        if (!id.equals(account.id)) return false;
        if (!email.equals(account.email)) return false;
        if (language != null ? !language.equals(account.language) : account.language != null) return false;
        if (name != null ? !name.equals(account.name) : account.name != null) return false;
        if (surname != null ? !surname.equals(account.surname) : account.surname != null) return false;
        return created != null ? created.equals(account.created) : account.created == null;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + email.hashCode();
        result = 31 * result + (language != null ? language.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (surname != null ? surname.hashCode() : 0);
        result = 31 * result + (created != null ? created.hashCode() : 0);
        return result;
    }
}
