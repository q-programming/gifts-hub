package com.qprogramming.gifts.account.event;

import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.group.Group;

import javax.persistence.*;
import java.util.Objects;

@Entity
public class AccountEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "account_event_seq_gen")
    @SequenceGenerator(name = "account_event_seq_gen", sequenceName = "account_event_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne
    private Account account;

    @Enumerated(EnumType.STRING)
    private AccountEventType type;

    @ManyToOne
    private Group group;

    @Column(unique = true)
    private String token;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public AccountEventType getType() {
        return type;
    }

    public void setType(AccountEventType type) {
        this.type = type;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AccountEvent that = (AccountEvent) o;

        if (!Objects.equals(id, that.id)) return false;
        if (!Objects.equals(account, that.account)) return false;
        if (type != that.type) return false;
        if (!Objects.equals(group, that.group)) return false;
        return Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (account != null ? account.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (group != null ? group.hashCode() : 0);
        result = 31 * result + (token != null ? token.hashCode() : 0);
        return result;
    }
}
