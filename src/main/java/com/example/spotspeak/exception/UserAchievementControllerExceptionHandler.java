package com.example.spotspeak.exception;

import com.example.spotspeak.controller.achievement.UserAchievementController;
import com.example.spotspeak.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = {UserAchievementController.class})
public class UserAchievementControllerExceptionHandler {

    @ExceptionHandler(UserAchievementNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserAchievementNotFoundException(UserAchievementNotFoundException ex) {
        ErrorResponse response = ErrorResponse.createInstance();
        response.addMessage(ex.getMessage());
        return response;
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserNotFoundException(UserNotFoundException ex) {
        ErrorResponse response = ErrorResponse.createInstance();
        response.addMessage(ex.getMessage());
        return response;
    }
}
