package com.example.spotspeak.exception;

import jakarta.ws.rs.ForbiddenException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.spotspeak.dto.ErrorResponse;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    ErrorResponse handleConstraintViolationException(ConstraintViolationException e) {
        ErrorResponse response = ErrorResponse.createInstance();
        e.getConstraintViolations()
                .forEach(constraintViolation -> response.addMessage(
                        constraintViolation.getPropertyPath().toString() + " : "
                                + constraintViolation.getMessage()));

        return response;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    ErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        ErrorResponse response = ErrorResponse.createInstance();

        e.getBindingResult().getFieldErrors()
                .forEach(fieldError -> response
                        .addMessage(fieldError.getField() + " : "
                                + fieldError.getDefaultMessage()));

        return response;
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    ErrorResponse handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        ErrorResponse response = ErrorResponse.createInstance();
        response.addMessage(e.getParameterName() + " : " + e.getMessage());

        return response;
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    ErrorResponse handleBindException(BindException e) {
        ErrorResponse response = ErrorResponse.createInstance();

        e.getBindingResult().getFieldErrors()
                .forEach(fieldError -> response
                        .addMessage(fieldError.getField() + " : "
                                + fieldError.getDefaultMessage()));

        return response;
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    ErrorResponse handleForbiddenException(ForbiddenException e) {
        ErrorResponse response = ErrorResponse.createInstance();
        response.addMessage("Access Denied: " + e.getMessage());
        return response;
    }

}
