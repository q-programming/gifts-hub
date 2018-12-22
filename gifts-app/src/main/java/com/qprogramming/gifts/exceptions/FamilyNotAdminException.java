package com.qprogramming.gifts.exceptions;

/**
 * Created by Jakub Romaniszyn on 2018-08-08
 * <p>
 * Exception to be thrown when current user is not family admin
 */
public class FamilyNotAdminException extends Exception {
    public static final String FAMILY_NOT_FOUND = "User with id {} is not family {} admin ";
}
