package com.personalbudgetmanager.exception;

public class AccountHasTransactionsException extends RuntimeException {
    public AccountHasTransactionsException() {
        super("Cannot delete account with transactions.");
    }
}
