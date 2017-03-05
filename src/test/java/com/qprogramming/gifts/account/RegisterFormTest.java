package com.qprogramming.gifts.account;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;

/**
 * Created by Remote on 05.03.2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class RegisterFormTest {

    @Test
    public void createAccountTest(){
        RegisterForm form = new RegisterForm();
        form.setName("Name");
        form.setSurname("Surname");
        form.setEmail("email@mail.com");
        form.setPassword("password");
        Account account = form.createAccount();
        assertTrue(account != null);


    }


}