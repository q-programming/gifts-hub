package com.qprogramming.gifts.support;

import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Jakub Romaniszyn on 2018-07-23
 */
@Component
public class TimeProvider implements Serializable {
    public Date now() {
        return new Date();
    }

    public long getCurrentTimeMillis() {
        return new DateTime(now()).getMillis();
    }
}
