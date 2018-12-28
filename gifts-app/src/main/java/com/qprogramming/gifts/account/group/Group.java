package com.qprogramming.gifts.account.group;

import com.fasterxml.jackson.annotation.JsonView;
import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.config.MappingConfiguration;
import org.springframework.util.CollectionUtils;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

//@JsonIdentityInfo(
//        generator = ObjectIdGenerators.PropertyGenerator.class,
//        property = "id")
@Entity
@Table(name = "group_table")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "group_seq_gen")
    @SequenceGenerator(name = "group_seq_gen", sequenceName = "group_id_seq", allocationSize = 1)
    private Long id;

    @Column
    private String name;

    @ManyToMany(mappedBy = "groups")
    @JsonView(MappingConfiguration.Members.class)
    private Set<Account> members;

    @OneToMany
    @JoinTable(name = "group_admins")
    @JsonView(MappingConfiguration.Members.class)
    private Set<Account> admins;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Set<Account> getMembers() {
        if (CollectionUtils.isEmpty(members)) {
            members = new HashSet<>();
        }
        return members;
    }

    public void setMembers(Set<Account> members) {
        this.members = members;
    }

    public Group addMember(Account account) {
        this.getMembers().add(account);
        account.getGroups().add(this);
        return this;
    }

    public Group removeMember(Account account) {
        this.getMembers().remove(account);
        account.getGroups().remove(this);
        return this;
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
        Group group = (Group) o;
        return id.equals(group.id) &&
                Objects.equals(name, group.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
