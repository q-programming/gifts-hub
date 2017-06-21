package com.qprogramming.gifts.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

@Entity
public class Account implements Serializable, UserDetails {

    @Id
    private String id;
    @Column
    private String email;
    @JsonIgnore
    private String password;
    @Column
    private String language;
    @Column
    private String name;
    @Column
    private String surname;
    @Column(unique = true)
    private String username;
    @Transient
    private Collection<GrantedAuthority> authorities = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private Roles role;

    @Column
    private Date created;

    @Enumerated(EnumType.STRING)
    private AccountType type;

    @Column
    private Boolean publicList = false;

    @Column
    private Boolean tourComplete = false;

    private String fullname;

    private Boolean familyAdmin = false;

    private Integer giftsCount = 0;

    @Transient
    private String tokenValue;

    public Account() {
        this.created = new Date();
    }

    public Account(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        setAuthority(Roles.ROLE_USER);
        this.created = new Date();

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
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

    public Boolean getTourComplete() {
        return tourComplete;
    }

    public void setTourComplete(Boolean tourComplete) {
        this.tourComplete = tourComplete;
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
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public AccountType getType() {
        return type;
    }

    public void setType(AccountType type) {
        this.type = type;
    }

    public String getFullname() {
        return getName() + " " + getSurname();
    }

    public Boolean getPublicList() {
        return publicList;
    }

    public void setPublicList(Boolean publicList) {
        this.publicList = publicList;
    }

    public boolean isFamilyAdmin() {
        return familyAdmin != null ? familyAdmin : false;
    }

    public void setFamilyAdmin(boolean familyAdmin) {
        this.familyAdmin = familyAdmin;
    }

    public Integer getGiftsCount() {
        return giftsCount != null ? giftsCount : 0;
    }

    public void setGiftsCount(Integer giftsCount) {
        this.giftsCount = giftsCount;
    }


    @JsonIgnore
    public String getTokenValue() {
        return tokenValue;
    }

    @JsonIgnore
    public void setTokenValue(String tokenValue) {
        this.tokenValue = tokenValue;
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
        if (email != null ? !email.equals(account.email) : account.email != null) return false;
        if (!name.equals(account.name)) return false;
        if (!surname.equals(account.surname)) return false;
        if (!username.equals(account.username)) return false;
        return created != null ? created.equals(account.created) : account.created == null;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + name.hashCode();
        result = 31 * result + surname.hashCode();
        result = 31 * result + username.hashCode();
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
