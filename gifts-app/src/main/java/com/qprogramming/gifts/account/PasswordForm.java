package com.qprogramming.gifts.account;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class PasswordForm {
    @NotBlank(message = RegisterForm.NOT_BLANK_MESSAGE)
    private String password;

    @NotBlank(message = RegisterForm.NOT_BLANK_MESSAGE)
    private String confirmpassword;

    @NotBlank(message = RegisterForm.NOT_BLANK_MESSAGE)
    private String token;
}
