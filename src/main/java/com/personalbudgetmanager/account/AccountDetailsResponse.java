package com.personalbudgetmanager.account;

import java.math.BigDecimal;
import java.util.UUID;

record AccountDetailsResponse(UUID id, String name, BigDecimal balance) {
}
