package com.lostfound.util;

public class Validation {

    public static boolean isEmpty(String text) {

        return text == null
                || text.trim().isEmpty();
    }

    public static boolean isPhoneValid(
            String phone) {

        return phone != null
                && phone.matches("\\d{10}");
    }

    public static boolean isValidQuantity(
            int quantity) {

        return quantity >= 1;
    }
}