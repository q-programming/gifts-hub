package com.qprogramming.gifts.api.user;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AccountSettings {
    private boolean newsletter;
    private boolean birthdayReminders;
    private boolean publicList;
    private String language;
    private String birthday;
}
