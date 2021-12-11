package com.qprogramming.gifts.account;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

/**
 * Created by Khobar on 03.03.2017.
 */
@Getter
@Setter
public class RegisterForm {


    public static final String NOT_BLANK_MESSAGE = "Cannot be blank";

    @NotBlank
    @Email(message = "Not email")
    private String email;

    @NotBlank(message = RegisterForm.NOT_BLANK_MESSAGE)
    private String name;

    @NotBlank(message = RegisterForm.NOT_BLANK_MESSAGE)
    private String surname;

    @NotBlank(message = RegisterForm.NOT_BLANK_MESSAGE)
    private String username;

    @NotBlank(message = RegisterForm.NOT_BLANK_MESSAGE)
    private String password;

    @NotBlank(message = RegisterForm.NOT_BLANK_MESSAGE)
    private String confirmpassword;

    public Account createAccount() {
        Account account = new Account();
        account.setName(getName());
        account.setSurname(getSurname());
        account.setEmail(getEmail());
        account.setUsername(getUsername());
        account.setPassword(getPassword());
        return account;
    }
}
