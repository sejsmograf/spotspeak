package com.example.spotspeak.exception;

import com.example.spotspeak.controller.trace.TraceController;
import com.example.spotspeak.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = { TraceController.class })
public class TraceControllerExceptionHandler {

    @ExceptionHandler(TraceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleTraceNotFoundException(TraceNotFoundException ex) {
        ErrorResponse response = ErrorResponse.createInstance();

        response.addMessage(ex.getMessage());
        return response;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneralException(Exception ex) {
        ErrorResponse response = ErrorResponse.createInstance();
        response.addMessage(ex.getMessage());
        return response;
    }
}
