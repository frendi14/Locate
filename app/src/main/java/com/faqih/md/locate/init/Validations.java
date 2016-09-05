package com.faqih.md.locate.init;

/**
 * Created by Faqih on 8/18/2016.
 */
public class Validations {
    public static boolean isPasswordValid(String password) {
        return password.length() > 7;
    }

    public static  boolean isPasswordMatch(String password, String confirmPassword){
        return  password.equals(confirmPassword);
    }
}
