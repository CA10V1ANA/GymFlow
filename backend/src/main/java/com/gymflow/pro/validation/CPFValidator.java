package com.gymflow.pro.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validates Brazilian CPF numbers using the official check-digit algorithm.
 * Accepts CPF with or without punctuation (e.g. 123.456.789-09 or 12345678909).
 */
public class CPFValidator implements ConstraintValidator<CPF, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // use @NotBlank separately for required checks
        }
        String cpf = value.replaceAll("[^0-9]", "");

        if (cpf.length() != 11 || cpf.chars().distinct().count() == 1) {
            return false;
        }

        try {
            int[] digits = cpf.chars().map(c -> c - '0').toArray();

            int firstCheck = calculateCheckDigit(digits, 9);
            int secondCheck = calculateCheckDigit(digits, 10);

            return digits[9] == firstCheck && digits[10] == secondCheck;
        } catch (Exception e) {
            return false;
        }
    }

    private int calculateCheckDigit(int[] digits, int length) {
        int sum = 0;
        int multiplier = length + 1;
        for (int i = 0; i < length; i++) {
            sum += digits[i] * multiplier--;
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }
}
