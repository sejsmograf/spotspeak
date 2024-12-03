package com.example.spotspeak.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.example.spotspeak.dto.ErrorResponse;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.ForbiddenException;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    ErrorResponse handleConstraintViolationException(ConstraintViolationException e) {
        logger.error("Constraint violation exception", e);
        ErrorResponse response = ErrorResponse.createInstance();
        e.getConstraintViolations()
                .forEach(constraintViolation -> response.addMessage(
                        constraintViolation.getPropertyPath().toString() + " : "
                                + constraintViolation.getMessage()));

        return response;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    ErrorResponse handleInvalidArgumentException(IllegalArgumentException e) {
        ErrorResponse response = ErrorResponse.createInstance();
        response.addMessage("Illegal argument" + e.getMessage());
        return response;
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    ErrorResponse handleForbiddenException(ForbiddenException e) {
        logger.error("Forbidden exception", e);
        ErrorResponse response = ErrorResponse.createInstance();
        response.addMessage("Access Denied: " + e.getMessage());
        return response;
    }
}
