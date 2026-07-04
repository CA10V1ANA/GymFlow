package com.gymflow.pro.util;

import java.security.SecureRandom;

/**
 * Generates short human-friendly codes (e.g. student registration codes).
 */
public final class CodeGenerator {

    private static final String ALPHABET = "0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private CodeGenerator() {
    }

    public static String numericCode(String prefix, int digits) {
        StringBuilder sb = new StringBuilder(prefix);
        for (int i = 0; i < digits; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
