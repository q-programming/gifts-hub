package com.qprogramming.gifts.account.family;

import com.qprogramming.gifts.account.Account;

import javax.persistence.*;

/**
 * Created by XE050991499 on 2017-06-01.
 */
@Entity
public class FamilyEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "family_event_seq_gen")
    @SequenceGenerator(name = "family_event_seq_gen", sequenceName = "family_event_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne
    private Account account;

    @Enumerated(EnumType.STRING)
    private FamilyEventType type;

    @ManyToOne
    private Family family;

    @Column
    private String uuid;

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

    public FamilyEventType getType() {
        return type;
    }

    public void setType(FamilyEventType type) {
        this.type = type;
    }

    public Family getFamily() {
        return family;
    }

    public void setFamily(Family family) {
        this.family = family;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FamilyEvent that = (FamilyEvent) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (account != null ? !account.equals(that.account) : that.account != null) return false;
        if (type != that.type) return false;
        if (family != null ? !family.equals(that.family) : that.family != null) return false;
        return uuid != null ? uuid.equals(that.uuid) : that.uuid == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (account != null ? account.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (family != null ? family.hashCode() : 0);
        result = 31 * result + (uuid != null ? uuid.hashCode() : 0);
        return result;
    }
}
