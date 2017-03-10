package com.qprogramming.gifts.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Khobar on 28.02.2017.
 */
@Entity
public class Account implements Serializable, UserDetails {

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
    @Transient
    private Collection<GrantedAuthority> authorities = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private Roles role;

    private Instant created;

    public Account() {
        this.created = Instant.now();
    }

    public Account(String email, String password) {
        this.email = email;
        this.password = password;
        setAuthority(Roles.ROLE_USER);
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

    public Roles getRole() {
        return role;
    }

    public void setRole(Roles role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public void setAuthority(Roles role) {
        this.authorities.add(createAuthority(role));
    }

    private GrantedAuthority createAuthority(Roles role) {
        return new SimpleGrantedAuthority(role.toString());
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Collection<GrantedAuthority> authorities) {
        this.authorities = authorities;
    }

    @JsonIgnore
    public String getPassword() {
        return password;
    }

    @JsonIgnore
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }

    @JsonIgnore
    public boolean getIsUser() {
        return getIsPowerUser() || role.equals(Roles.ROLE_USER);
    }

    /**
     * Checks if currently logged user have ROLE_USER authority
     *
     * @return
     */
    @JsonIgnore
    public boolean getIsPowerUser() {
        return getIsAdmin() || role.equals(Roles.ROLE_POWERUSER);
    }

    /**
     * Checks if currently logged user have ROLE_ADMIN authority
     *
     * @return
     */
    @JsonIgnore
    public boolean getIsAdmin() {
        return role.equals(Roles.ROLE_ADMIN);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Account account = (Account) o;

        if (!id.equals(account.id)) return false;
        if (!email.equals(account.email)) return false;
        if (name != null ? !name.equals(account.name) : account.name != null) return false;
        if (surname != null ? !surname.equals(account.surname) : account.surname != null) return false;
        return created != null ? created.equals(account.created) : account.created == null;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + email.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (surname != null ? surname.hashCode() : 0);
        result = 31 * result + (created != null ? created.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Account{" +
                "email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                '}';
    }
}
