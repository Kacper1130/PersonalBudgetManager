package com.personalbudgetmanager.transaction.dto;

import com.personalbudgetmanager.transaction.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreateTransactionRequest(
        @Positive(message = "Amount must be positive")
        BigDecimal amount,
        @NotNull(message = "Transaction type is required")
        TransactionType type,
        @NotBlank(message = "Category is required")
        String category,
        String description,
        LocalDateTime dateTime,
        @NotNull(message = "Account ID is required")
        UUID accountId
) {
}
