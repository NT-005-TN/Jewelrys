package com.jewelry.workshop.presentation.exception;

public class ClientProfileNotFoundException extends RuntimeException {
    public ClientProfileNotFoundException() {
        super("Профиль клиента не найден");
    }
}