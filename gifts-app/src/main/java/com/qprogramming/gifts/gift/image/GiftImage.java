package com.qprogramming.gifts.gift.image;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class GiftImage {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cat_seq_giftimg")
    @SequenceGenerator(name = "giftimg_seq_gen", sequenceName = "giftimg_id_seq", allocationSize = 1)
    private Long id;

    @Lob
    private byte[] image;

    @Column
    private String type;
}
