package com.qprogramming.gifts.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;

/**
 * Created by Khobar on 28.02.2017.
 */
@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "account_seq_gen")
    @SequenceGenerator(name = "account_seq_gen", sequenceName = "account_id_seq", allocationSize = 1)
    private Long id;
    @Column(unique = true)
    @NotEmpty
    private String username;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Account account = (Account) o;

        if (!id.equals(account.id)) {
            return false;
        }
        if (!username.equals(account.username)) {
            return false;
        }
        if (email != null ? !email.equals(account.email) : account.email != null) {
            return false;
        }
        if (name != null ? !name.equals(account.name) : account.name != null) {
            return false;
        }
        return surname != null ? surname.equals(account.surname) : account.surname == null;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + username.hashCode();
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (surname != null ? surname.hashCode() : 0);
        return result;
    }
}
