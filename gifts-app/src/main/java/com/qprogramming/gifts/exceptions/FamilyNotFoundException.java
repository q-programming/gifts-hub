package com.qprogramming.gifts.exceptions;

/**
 * Created by Jakub Romaniszyn on 2018-08-08
 * <p>
 * Exception to be thrown when family was not found
 */
public class FamilyNotFoundException extends Exception {
    public static final String FAMILY_NOT_FOUND = "Family for user {} was not found";
}
