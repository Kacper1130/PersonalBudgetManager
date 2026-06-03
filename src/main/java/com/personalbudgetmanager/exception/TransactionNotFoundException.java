package com.personalbudgetmanager.exception;

import java.util.UUID;

public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException(UUID id) {
        super(String.format("Transaction with id: %s not found", id));
    }
}
