package com.personalbudgetmanager.transaction.dto;

import com.personalbudgetmanager.transaction.TransactionType;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreateTransactionRequest(
        @Positive
        BigDecimal amount,
        TransactionType type,
        String category,
        String description,
        LocalDateTime dateTime,
        UUID accountId
) {
}
