package com.personalbudgetmanager.summary.dto;

import java.math.BigDecimal;
import java.util.Map;

public record SummaryResponse(BigDecimal totalIncome, BigDecimal totalExpense, Map<String, BigDecimal> expensesByCategory) {
}
