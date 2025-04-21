package com.example.demo.exception;

public class UserNotFoundExceptionByName extends RuntimeException {
    public UserNotFoundExceptionByName(String message) {
        super(message);
    }

}

