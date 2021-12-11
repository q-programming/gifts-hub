package com.qprogramming.gifts.account.group;

import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.RegisterForm;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * Created by Remote on 05.04.2017.
 */
@Getter
@Setter
public class KidForm {

    private Long groupId;
    private String id;
    @NotBlank(message = RegisterForm.NOT_BLANK_MESSAGE)
    private String name;
    @NotBlank(message = RegisterForm.NOT_BLANK_MESSAGE)
    private String surname;
    @NotBlank(message = RegisterForm.NOT_BLANK_MESSAGE)
    private String username;
    private String avatar;
    private Boolean publicList = false;

    public Account createAccount() {
        Account kid = new Account();
        kid.setName(getName());
        kid.setSurname(getSurname());
        kid.setUsername(getUsername());
        kid.setPublicList(getPublicList());
        return kid;
    }
}
