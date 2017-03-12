package com.qprogramming.gifts.account.avatar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import java.util.Date;

/**
 * Created by Khobar on 06.03.2017.
 */
@Entity
public class Avatar {

    @Id
    private String id;
    @Lob
    private byte[] image;
    @Column
    private Date created;
    @Column
    private String type;

    public Avatar() {
        created = new Date();
    }

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

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
