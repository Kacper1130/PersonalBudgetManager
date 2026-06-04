package com.personalbudgetmanager.account.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record CreateAccountRequest(@NotBlank(message = "Account name is required") String name, BigDecimal balance) {
}
