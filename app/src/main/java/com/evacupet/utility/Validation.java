package com.evacupet.utility;

import android.text.TextUtils;
import android.util.Patterns;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Validation {

    /**
     * Method to validate email addresses
     * This method will return true if entered string is valid email.
     *
     * @param email String parameter to validate as valid email.
     * @return boolean
     */
    public static boolean isValidEmail(String email) {
        return (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }

    /**
     * Method to validate mobile number.
     * This method will return true if entered string is valid mobile number.     *
     *
     * @param mobile String parameter to validate as valid mobile.
     * @return boolean
     */
    public static boolean isValidMobile(String mobile) {
        return (!TextUtils.isEmpty(mobile) && mobile.length() == 10);
    }

    /**
     * Method to validate password
     * This method will return true if entered string is valid password.
     *
     * @param password String parameter to validate as valid email.
     * @return boolean
     */
    public static boolean isValidPassword(String password) {
        return (!TextUtils.isEmpty(password) && password.length() >= 8);
    }

    /**
     * Parse verification code
     *
     * @param message sms message
     * @return only four numbers from massage string
     */
    public static String parseCode(String message) {
        Pattern p = Pattern.compile("\\b\\d{4}\\b");
        Matcher m = p.matcher(message);
        String code = "";
        while (m.find()) {
            code = m.group(0);
        }
        return code;
    }


}
