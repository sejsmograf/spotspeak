package com.example.spotspeak.validation;

import java.util.Arrays;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class FileValidator implements ConstraintValidator<ValidFile, MultipartFile> {

    private long maxSize;
    private String[] allowedTypes;

    @Override
    public void initialize(ValidFile constraintAnnotation) {
        this.maxSize = constraintAnnotation.maxSize();
        this.allowedTypes = constraintAnnotation.allowedTypes();
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        if (file.getSize() > maxSize) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "File size exceeds the maximum allowed size of " + maxSize / 1024 + " KB.")
                    .addConstraintViolation();
            return false;
        }

        if (!Arrays.asList(allowedTypes).contains(file.getContentType())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Invalid file type: [" + file.getContentType() + "]. Allowed types are "
                            + String.join(", ", allowedTypes) + ".")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
