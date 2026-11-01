package com.jewelry.workshop.presentation.exception;

public class PasswordMismatchException extends RuntimeException {
    public PasswordMismatchException() {
        super("Пароли не совпадают");
    }
}