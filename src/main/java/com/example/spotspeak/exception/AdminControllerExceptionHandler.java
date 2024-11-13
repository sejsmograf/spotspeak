package com.example.spotspeak.exception;

import com.example.spotspeak.controller.admin.AdminController;
import com.example.spotspeak.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = {AdminController.class})
public class AdminControllerExceptionHandler {

    @ExceptionHandler(AchievementExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleAchievementExistsException(AchievementExistsException ex) {
        ErrorResponse response = ErrorResponse.createInstance();
        response.addMessage(ex.getMessage());
        return response;
    }

    @ExceptionHandler(AchievementNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleFriendshipRequestNotFoundException(AchievementNotFoundException ex) {
        ErrorResponse response = ErrorResponse.createInstance();
        response.addMessage(ex.getMessage());
        return response;
    }
}
