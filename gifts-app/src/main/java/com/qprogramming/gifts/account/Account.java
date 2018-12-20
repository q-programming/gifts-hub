package com.qprogramming.gifts.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.qprogramming.gifts.account.authority.Authority;
import com.qprogramming.gifts.account.authority.Role;
import io.jsonwebtoken.lang.Collections;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

import static com.qprogramming.gifts.support.Utils.ACCOUNT_COMPARATOR;

@Entity
public class Account implements Serializable, UserDetails, Comparable<Account> {

    @Id
    private String id;
    @Column(unique = true)
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

    @Column
    private Date created;

    @Enumerated(EnumType.STRING)
    private AccountType type;

    @Column(columnDefinition = "boolean default false")
    private Boolean publicList = false;

    @Column(columnDefinition = "boolean default false")
    private Boolean tourComplete = false;

    @Column(columnDefinition = "boolean default false")
    private Boolean seenChangelog = false;

    @Column(columnDefinition = "boolean default false")
    private Boolean notifications = false;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "user_authority",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "authority_id", referencedColumnName = "id"))
    private List<Authority> authorities = new ArrayList<>();

    @Column
    private String uuid;

    @Column(columnDefinition = "boolean default false")
    private boolean enabled = false;

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
        this.created = new Date();
    }

    public Account(String email) {
        this.email = email;
        this.type = AccountType.TEMP;
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

    public Boolean getTourComplete() {
        return tourComplete;
    }

    public void setTourComplete(Boolean tourComplete) {
        this.tourComplete = tourComplete;
    }

    public void addAuthority(Authority authority) {
        List<Authority> auths = new ArrayList<>(getAuthorities());
        auths.add(authority);
        this.setAuthorities(auths);
    }

    @Override
    public Collection<Authority> getAuthorities() {
        if (Collections.isEmpty(this.authorities)) {
            this.authorities = new ArrayList<>();
        }
        return this.authorities;
    }

    public void setAuthorities(List<Authority> authorities) {
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
        return StringUtils.isNotBlank(name) && StringUtils.isNotBlank(surname) ? name + " " + surname : null;
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

    public Boolean getSeenChangelog() {
        return seenChangelog;
    }

    public void setSeenChangelog(Boolean seenChangelog) {
        this.seenChangelog = seenChangelog;
    }

    public Boolean getNotifications() {
        return notifications;
    }

    public void setNotifications(Boolean notifications) {
        this.notifications = notifications;
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
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @JsonIgnore
    public boolean getIsUser() {
        return this.authorities.stream().map(Authority::getName).anyMatch(role -> Arrays.asList(Role.ROLE_ADMIN, Role.ROLE_USER).contains(role));
    }


    @JsonIgnore
    public boolean getIsAdmin() {
        return this.authorities.stream().map(Authority::getName).anyMatch(Role.ROLE_ADMIN::equals);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(id, account.id) &&
                email.equals(account.email) &&
                Objects.equals(language, account.language) &&
                Objects.equals(name, account.name) &&
                Objects.equals(surname, account.surname) &&
                username.equals(account.username) &&
                Objects.equals(created, account.created) &&
                type == account.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email, language, name, surname, username, created, type);
    }

    @Override
    public String toString() {
        return "Account{" +
                "email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                '}';
    }

    @Override
    public int compareTo(Account o) {
        return ACCOUNT_COMPARATOR.compare(this, o);
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
