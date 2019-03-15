package com.qprogramming.gifts.exceptions;

/**
 * Created by Jakub Romaniszyn on 2018-08-08
 * <p>
 * Exception to be thrown when group was not found
 */
public class GroupNotFoundException extends Exception {

    public static final String GROUP_NOT_FOUND = "Group was not found for {} with id {}";
}
