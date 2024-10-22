package com.example.spotspeak.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FileValidator.class)
public @interface ValidFile {
    String message() default "Invalid file";

    long maxSize() default 1048576; // Default max size: 1 MB

    String[] allowedTypes() default { "image/jpeg", "image/png" }; // Default allowed types

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
