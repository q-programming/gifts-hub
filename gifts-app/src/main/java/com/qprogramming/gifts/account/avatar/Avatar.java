package com.qprogramming.gifts.account.avatar;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import java.util.Date;

/**
 * Created by Khobar on 06.03.2017.
 */
@Entity
@Getter
@Setter
public class Avatar {

    @Id
    private String id;
    @Lob
    private byte[] image;
    @Column
    private Date created = new Date();
    @Column
    private String type;
}
