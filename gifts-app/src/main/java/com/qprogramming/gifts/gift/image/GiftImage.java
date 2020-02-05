package com.qprogramming.gifts.gift.image;

import javax.persistence.*;

@Entity
public class GiftImage {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cat_seq_giftimg")
    @SequenceGenerator(name = "giftimg_seq_gen", sequenceName = "giftimg_id_seq", allocationSize = 1)
    private Long id;

    @Lob
    private byte[] image;

    @Column
    private String type;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
