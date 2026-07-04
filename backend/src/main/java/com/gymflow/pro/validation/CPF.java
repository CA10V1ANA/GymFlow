package com.gymflow.pro.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CPFValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface CPF {

    String message() default "Invalid CPF number";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
