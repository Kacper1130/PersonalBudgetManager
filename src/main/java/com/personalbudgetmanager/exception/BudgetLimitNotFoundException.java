package com.personalbudgetmanager.exception;

public class BudgetLimitNotFoundException extends RuntimeException {
    public BudgetLimitNotFoundException(String category) {
        super(String.format("Budget limit for category '%s' not found", category));
    }
}
