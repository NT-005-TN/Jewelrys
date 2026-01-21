package com.jewelry.workshop.presentation.exception;

public class EmailAlreadyUsedException extends RuntimeException {
    public EmailAlreadyUsedException(String email) {
        super("Email " + email + " уже зарегистрирован");
    }
}