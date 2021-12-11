package com.qprogramming.gifts.account.event;

import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.group.Group;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@EqualsAndHashCode
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
}
