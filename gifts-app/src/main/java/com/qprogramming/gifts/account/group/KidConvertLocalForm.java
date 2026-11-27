package com.qprogramming.gifts.account.group;

import com.qprogramming.gifts.account.RegisterForm;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * Form used by invited kid to finish conversion using username and password
 */
@Getter
@Setter
public class KidConvertLocalForm {

    @NotBlank(message = RegisterForm.NOT_BLANK_MESSAGE)
    private String username;

    @NotBlank(message = RegisterForm.NOT_BLANK_MESSAGE)
    private String password;

    @NotBlank(message = RegisterForm.NOT_BLANK_MESSAGE)
    private String confirmpassword;
}
