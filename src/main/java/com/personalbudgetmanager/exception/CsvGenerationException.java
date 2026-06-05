package com.personalbudgetmanager.exception;

public class CsvGenerationException extends RuntimeException {
    public CsvGenerationException() {
        super("Unexpected error occurred while building the CSV file.");
    }
}
