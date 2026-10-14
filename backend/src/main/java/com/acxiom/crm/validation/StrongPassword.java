package com.acxiom.crm.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Password policy, required by p.8 §6.2:
 * "Minimum length: 8 characters", "Require a combination of character classes
 * where appropriate".
 * <p>
 * Applied server-side so the policy cannot be bypassed by posting directly to
 * the API (p.7 §5.2: "Repeat all security- and business-critical validations on
 * the server").
 */
@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface StrongPassword {

    String message() default "Password must be at least 8 characters and include an uppercase letter, "
            + "a lowercase letter, a digit and a special character.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
