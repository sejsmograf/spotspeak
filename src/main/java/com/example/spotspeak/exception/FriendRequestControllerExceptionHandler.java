package com.example.spotspeak.exception;

import com.example.spotspeak.controller.friendship.FriendRequestController;
import com.example.spotspeak.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = {FriendRequestController.class})
public class FriendRequestControllerExceptionHandler {

    @ExceptionHandler(FriendRequestExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleFriendRequestExistsException(FriendRequestExistsException ex) {
        ErrorResponse response = ErrorResponse.createInstance();
        response.addMessage(ex.getMessage());
        return response;
    }

    @ExceptionHandler(FriendRequestNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleFriendshipRequestNotFoundException(FriendRequestNotFoundException ex) {
        ErrorResponse response = ErrorResponse.createInstance();
        response.addMessage(ex.getMessage());
        return response;
    }

    @ExceptionHandler(FriendshipExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleFriendshipExistsException(FriendshipExistsException ex) {
        ErrorResponse response = ErrorResponse.createInstance();
        response.addMessage(ex.getMessage());
        return response;
    }

    @ExceptionHandler(InvalidFriendRequestStatusException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleInvalidFriendRequestStatusException(InvalidFriendRequestStatusException ex) {
        ErrorResponse response = ErrorResponse.createInstance();
        response.addMessage(ex.getMessage());
        return response;
    }
}
