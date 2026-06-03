package com.personalbudgetmanager.exception;

import java.time.LocalDateTime;

record ErrorResponse(LocalDateTime timestamp, int status, String error, String message) {
}
