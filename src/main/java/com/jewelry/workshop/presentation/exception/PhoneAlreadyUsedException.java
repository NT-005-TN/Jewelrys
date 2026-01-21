package com.jewelry.workshop.presentation.exception;

public class PhoneAlreadyUsedException extends RuntimeException {
    public PhoneAlreadyUsedException(String phone) {
        super("Телефон " + phone + " уже используется другим клиентом");
    }
}