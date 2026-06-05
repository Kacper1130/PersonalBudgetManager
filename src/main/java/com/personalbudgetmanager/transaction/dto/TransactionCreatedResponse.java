package com.personalbudgetmanager.transaction.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TransactionCreatedResponse(TransactionResponse transactionResponse, String warning) {
}
