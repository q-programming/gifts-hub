package com.qprogramming.gifts.exceptions;

/**
 * Created by Jakub Romaniszyn on 2018-08-08
 * <p>
 * Exception to be thrown when user was not found
 */
public class AccountNotFoundException extends Exception {
    public static final String ACCOUNT_WITH_ID_WAS_NOT_FOUND = "Account with id {} was not found";
}
