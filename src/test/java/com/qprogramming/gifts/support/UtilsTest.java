package com.qprogramming.gifts.support;

import org.junit.Assert;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by XE050991499 on 2017-05-07.
 */
public class UtilsTest {
    @Test
    public void invalidUrlLink() throws Exception {
        String link = "http://google.com/some/file!.html";
        assertFalse(Utils.validUrlLink(link));
    }

    @Test
    public void validUrlLink() throws Exception {
        String link = "http://google.com/some/file.html";
        assertFalse(Utils.validUrlLink(link));
    }

    @Test
    public void validateEmailWrong() throws Exception {
        String email = "notvalidmail";
        assertFalse(Utils.validateEmail(email));
    }

    @Test
    public void validateEmail() throws Exception {
        String email = "valid@email.com";
        assertTrue(Utils.validateEmail(email));
    }

    @Test
    public void convertDateToStringAndStringToDateTest() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2014);
        cal.set(Calendar.MONTH, 11);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        Date testDate = Utils.convertStringToDate("01-12-2014");
        Assert.assertEquals("01-12-2014", Utils.convertDateToString(testDate));
    }
}