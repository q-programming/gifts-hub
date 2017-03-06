package com.qprogramming.gifts.account.avatar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by Khobar on 06.03.2017.
 */
@Entity
public class Avatar {

    @Id
    private String id;
    @Column
    private byte[] image;

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
