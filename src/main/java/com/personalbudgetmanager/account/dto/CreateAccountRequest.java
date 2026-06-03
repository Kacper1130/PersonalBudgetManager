package com.personalbudgetmanager.account.dto;

import java.math.BigDecimal;

public record CreateAccountRequest(String name, BigDecimal balance) {
}
