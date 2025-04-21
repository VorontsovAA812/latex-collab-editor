package com.example.demo.rest.controllers;

import com.example.demo.exception.ApiError;
import com.example.demo.exception.LatexCompilationException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.exception.UserNotFoundExceptionByName;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({UserNotFoundException.class, UserNotFoundExceptionByName.class})
    public ResponseEntity<ApiError> handleUserNotFound(Exception ex) {
        ApiError apiError = new ApiError(HttpStatus.NOT_FOUND, ex.getMessage(),        null );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }
    @ExceptionHandler(LatexCompilationException.class)
    public ResponseEntity<ApiError> handleLatexError(LatexCompilationException ex) {
        ApiError error = new ApiError();
        error.setStatus(HttpStatus.BAD_REQUEST);
        error.setMessage(ex.getMessage());
        error.setDetails(ex.getCompilerOutput()); // Добавляем вывод компилятора
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

}