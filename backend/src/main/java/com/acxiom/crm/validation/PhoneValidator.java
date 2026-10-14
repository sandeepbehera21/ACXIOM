package com.acxiom.crm.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * Ten-digit Indian mobile number, first digit 6-9.
 * <p>
 * Project rule chosen under p.7 §5.1, which explicitly defers the phone format
 * to the project ("10-digit Indian mobile format, if that is the project rule").
 * Blank values are treated as valid so that the annotation can be combined with
 * {@code @NotBlank} on required fields and used alone on optional ones.
 */
public class PhoneValidator implements ConstraintValidator<ValidPhone, String> {

    private static final Pattern INDIAN_MOBILE = Pattern.compile("^[6-9]\\d{9}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return INDIAN_MOBILE.matcher(value.trim()).matches();
    }
}
