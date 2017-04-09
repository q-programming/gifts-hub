package com.qprogramming.gifts.account.family;

import com.qprogramming.gifts.account.Account;
import org.springframework.util.CollectionUtils;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Family {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "family_seq_gen")
    @SequenceGenerator(name = "family_seq_gen", sequenceName = "family_id_seq", allocationSize = 1)
    private Long id;

    @Column
    private String name;

    @OneToMany
    @JoinTable(name = "family_members")
    private Set<Account> members;

    @OneToMany
    @JoinTable(name = "family_admins")
    private Set<Account> admins;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<Account> getMembers() {
        if (CollectionUtils.isEmpty(members)) {
            members = new HashSet<>();
        }
        return members;
    }

    public void setMembers(Set<Account> members) {
        this.members = members;
    }

    public Set<Account> getAdmins() {
        if (CollectionUtils.isEmpty(admins)) {
            admins = new HashSet<>();
        }
        return admins;
    }

    public void setAdmins(Set<Account> admins) {
        this.admins = admins;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Family family = (Family) o;

        if (id != null ? !id.equals(family.id) : family.id != null) return false;
        if (members != null ? !members.equals(family.members) : family.members != null) return false;
        return admins != null ? admins.equals(family.admins) : family.admins == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (members != null ? members.hashCode() : 0);
        result = 31 * result + (admins != null ? admins.hashCode() : 0);
        return result;
    }
}
