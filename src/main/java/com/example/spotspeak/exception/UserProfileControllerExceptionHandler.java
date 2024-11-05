package com.example.spotspeak.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.spotspeak.controller.user.UserProfileController;
import com.example.spotspeak.dto.ErrorResponse;

@RestControllerAdvice(basePackageClasses = { UserProfileController.class })
public class UserProfileControllerExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserNotFoundException(UserNotFoundException ex) {
        ErrorResponse response = ErrorResponse.createInstance();
        response.addMessage(ex.getMessage());
        return response;
    }

    @ExceptionHandler(KeycloakClientException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleKeycloakException(KeycloakClientException ex) {
        ErrorResponse response = ErrorResponse.createInstance();
        response.addMessage(ex.getMessage());
        response.addMessage(ex.getDetails());
        return response;
    }

    @ExceptionHandler(PasswordChallengeFailedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handlePasswordChallengeFailedException(PasswordChallengeFailedException ex) {
        ErrorResponse response = ErrorResponse.createInstance();
        response.addMessage(ex.getMessage());
        return response;
    }

    @ExceptionHandler(AttributeAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleAttributeAlreadyExistsException(AttributeAlreadyExistsException ex) {
        ErrorResponse response = ErrorResponse.createInstance();
        response.addMessage(ex.getMessage());
        return response;
    }
}
