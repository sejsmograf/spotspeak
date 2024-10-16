package com.example.spotspeak.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.example.spotspeak.controller.UserProfileController;
import com.example.spotspeak.dto.ErrorResponse;

@ControllerAdvice(basePackageClasses = { UserProfileController.class })
public class UserProfileControllerExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex) {
        ErrorResponse response = new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(KeycloakClientException.class)
    public ResponseEntity<ErrorResponse> handleKeycloakClientException(KeycloakClientException ex) {
        ErrorResponse response = new ErrorResponse(ex.statusCode, ex.getMessage());
        return ResponseEntity.status(HttpStatus.valueOf(ex.statusCode)).body(response);
    }

    @ExceptionHandler(KeycloakServerException.class)
    public ResponseEntity<ErrorResponse> handleKeycloakClientException(KeycloakServerException ex) {
        ErrorResponse response = new ErrorResponse(ex.statusCode, ex.getMessage());
        return ResponseEntity.status(HttpStatus.valueOf(ex.statusCode)).body(response);
    }

    @ExceptionHandler(KeycloakException.class)
    public ResponseEntity<ErrorResponse> handleKeycloakException(KeycloakException ex) {
        int statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        ErrorResponse response = new ErrorResponse(statusCode, ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

}
