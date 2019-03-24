package com.qprogramming.gifts.schedule;

import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.gift.Gift;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity
public class AppEvent {

    public AppEvent() {
        this.time = new Date();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "app_event_seq_gen")
    @SequenceGenerator(name = "app_event_seq_gen", sequenceName = "app_event_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne
    private Account account;

    @ManyToOne
    private Account createdBy;

    @ManyToOne
    private Gift gift;

    @Column
    private Date time;

    @Enumerated(EnumType.STRING)
    private AppEventType type;

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

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public AppEventType getType() {
        return type;
    }

    public void setType(AppEventType type) {
        this.type = type;
    }

    public Gift getGift() {
        return gift;
    }

    public void setGift(Gift gift) {
        this.gift = gift;
    }

    public Account getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Account createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppEvent appEvent = (AppEvent) o;
        return Objects.equals(id, appEvent.id) &&
                Objects.equals(account, appEvent.account) &&
                Objects.equals(createdBy, appEvent.createdBy) &&
                Objects.equals(gift, appEvent.gift) &&
                Objects.equals(time, appEvent.time) &&
                type == appEvent.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, account, createdBy, gift, time, type);
    }
}
