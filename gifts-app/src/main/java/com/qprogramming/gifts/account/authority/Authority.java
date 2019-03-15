package com.qprogramming.gifts.account.authority;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;

/**
 * Created by Jakub Romaniszyn on 20.07.2018.
 */
@Entity
public class Authority implements GrantedAuthority {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name")
    private Role name;

    @Override
    public String getAuthority() {
        return name.name();
    }

    @JsonIgnore
    public Role getName() {
        return name;
    }

    public void setName(Role name) {
        this.name = name;
    }

    @JsonIgnore
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
