package com.qprogramming.gifts.schedule;

import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.gift.Gift;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@EqualsAndHashCode
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
}
