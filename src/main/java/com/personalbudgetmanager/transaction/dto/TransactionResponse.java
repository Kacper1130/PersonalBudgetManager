package com.personalbudgetmanager.transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        BigDecimal amount,
        String type,
        String category,
        String description,
        LocalDateTime dateTime,
        UUID accountId
) {
}
