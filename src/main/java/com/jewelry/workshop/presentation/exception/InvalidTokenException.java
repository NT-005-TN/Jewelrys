package com.jewelry.workshop.presentation.exception;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String reason) {
        super(reason);
    }
}