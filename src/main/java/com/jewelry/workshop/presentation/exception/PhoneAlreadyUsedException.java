package com.jewelry.workshop.presentation.exception;

public class PhoneAlreadyUsedException extends RuntimeException {
    public PhoneAlreadyUsedException(String phone) {
        super("Телефон уже используется другим клиентом");
    }
}
