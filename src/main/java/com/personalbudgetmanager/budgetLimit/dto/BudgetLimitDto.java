package com.personalbudgetmanager.budgetLimit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record BudgetLimitDto(
        @NotBlank(message = "Category is required") String category,
        @PositiveOrZero(message = "Limit amount cannot be negative") BigDecimal limitAmount
) {
}
