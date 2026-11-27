package com.qprogramming.gifts.account.group;

import com.qprogramming.gifts.account.RegisterForm;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

/**
 * Form used by kid administrator to invite kid to become regular account
 */
@Getter
@Setter
public class KidConvertForm {

    @NotBlank(message = RegisterForm.NOT_BLANK_MESSAGE)
    private String kidId;

    @NotBlank(message = RegisterForm.NOT_BLANK_MESSAGE)
    @Email(message = "Not email")
    private String email;
}
