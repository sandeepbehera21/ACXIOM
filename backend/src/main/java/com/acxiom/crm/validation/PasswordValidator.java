package com.acxiom.crm.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * Enforces the password policy from p.8 §6.2 / p.8 §6.1:
 * minimum 8 characters with at least one uppercase letter, one lowercase
 * letter, one digit and one special character.
 * <p>
 * The minimum length is also mirrored in
 * {@code acxiom.security.password.min-length} in application.properties so the
 * configured value is visible in one place.
 */
public class PasswordValidator implements ConstraintValidator<StrongPassword, String> {

    private static final int MIN_LENGTH = 8;
    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("\\d");
    private static final Pattern SPECIAL = Pattern.compile("[^A-Za-z0-9]");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        return value.length() >= MIN_LENGTH
                && UPPERCASE.matcher(value).find()
                && LOWERCASE.matcher(value).find()
                && DIGIT.matcher(value).find()
                && SPECIAL.matcher(value).find();
    }
}
