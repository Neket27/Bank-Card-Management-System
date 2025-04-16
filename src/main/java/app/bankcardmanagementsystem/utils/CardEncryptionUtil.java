package app.bankcardmanagementsystem.utils;

import java.util.Base64;

public class CardEncryptionUtil {

    public static String encrypt(String cardNumber) {
        return Base64.getEncoder().encodeToString(cardNumber.getBytes());
    }

    public static String decrypt(String encrypted) {
        return new String(Base64.getDecoder().decode(encrypted));
    }

    public static String maskCard(String cardNumber) {
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}