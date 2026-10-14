package com.acxiom.crm.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Phone validation, required by p.7 §5.1 ("Phone: Validate allowed phone
 * pattern/length") and p.7 §5.3 ("Phone: Must follow the configured phone
 * format and length").
 * <p>
 * The specification states the format is a project choice
 * ("10-digit Indian mobile format, if that is the project rule"), so the
 * pattern lives in one place in {@link PhoneValidator} and is documented as a
 * project assumption.
 */
@Documented
@Constraint(validatedBy = PhoneValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhone {

    String message() default "Enter a valid 10-digit mobile number.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
