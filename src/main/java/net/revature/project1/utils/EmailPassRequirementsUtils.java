package net.revature.project1.utils;

import java.util.regex.Pattern;

public class EmailPassRequirementsUtils {
    private static final String PASSWORD_REGEX = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[~`!@#$%^&*()\\-_+={}\\[\\]|;:<>,./?]).{8,}$";
    private static final String EMAIL_REGEX = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]))\\.){3}(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";

    private static final Pattern passPattern = Pattern.compile(PASSWORD_REGEX);
    private static final Pattern emailPattern = Pattern.compile(EMAIL_REGEX);

    /**
     * This is based on the RFC 5322 requirements for email.
     * @param email Takes in a  {@code String} email to be processed.
     * @return {@code True} if the password meets requirements or {@code False} if it fails to
     * meet the requirements.
     */
    public static boolean isValidEmail(String email) {
        return emailPattern.matcher(email).matches();
    }

    /**
     * This is based on 1 uppercase, 1 lowercase, 1 special, 1 number, and 8 characters or greater.
     * @param password Take in {@code String} object password.
     * @return {@code True} if the password meets requirements or {@code False} if it fails to
     * meet the requirements.
     */
    public static boolean isValidPassword(String password) {
        return passPattern.matcher(password).matches();
    }
}
