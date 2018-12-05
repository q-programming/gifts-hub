package com.qprogramming.gifts.account;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

/**
 * Created by Khobar on 03.03.2017.
 */
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmpassword() {
        return confirmpassword;
    }

    public void setConfirmpassword(String confirmpassword) {
        this.confirmpassword = confirmpassword;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

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
