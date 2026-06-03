package com.personalbudgetmanager.exception;

import java.util.UUID;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(UUID id) {
        super(String.format("Account with id: %s not found", id));
    }
}
