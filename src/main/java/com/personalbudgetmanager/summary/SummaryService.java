package com.personalbudgetmanager.summary;

import com.personalbudgetmanager.summary.dto.SummaryResponse;
import com.personalbudgetmanager.transaction.Transaction;
import com.personalbudgetmanager.transaction.TransactionService;
import com.personalbudgetmanager.transaction.TransactionType;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
class SummaryService {

    private final TransactionService transactionService;

    SummaryResponse getSummary() {
        List<Transaction> allTransactions = transactionService.getAllTransactions();

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        Map<String, BigDecimal> expensesByCategory = new HashMap<>();

        for (var t : allTransactions) {
            if (t.getType() == TransactionType.INCOME) {
                totalIncome = totalIncome.add(t.getAmount());

            } else if (t.getType() == TransactionType.EXPENSE) {
                totalExpense = totalExpense.add(t.getAmount());

                expensesByCategory.merge(t.getCategory(), t.getAmount(), BigDecimal::add);
            }
        }

        return new SummaryResponse(totalIncome, totalExpense, expensesByCategory);
    }
}
