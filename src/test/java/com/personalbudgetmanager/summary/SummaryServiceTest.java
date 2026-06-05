package com.personalbudgetmanager.summary;

import com.personalbudgetmanager.account.Account;
import com.personalbudgetmanager.summary.dto.SummaryResponse;
import com.personalbudgetmanager.transaction.Transaction;
import com.personalbudgetmanager.transaction.TransactionService;
import com.personalbudgetmanager.transaction.TransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SummaryServiceTest {

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private SummaryService summaryService;

    // --- getSummary ---

    @Test
    void getSummary_shouldReturnZeros_whenNoTransactionsExist() {
        when(transactionService.getAllTransactions()).thenReturn(List.of());

        SummaryResponse result = summaryService.getSummary();

        assertThat(result.totalIncome()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.totalExpense()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.expensesByCategory()).isEmpty();
    }

    @Test
    void getSummary_shouldSumIncomes_correctly() {
        when(transactionService.getAllTransactions()).thenReturn(List.of(
                buildTransaction(new BigDecimal("1000.00"), TransactionType.INCOME, "salary"),
                buildTransaction(new BigDecimal("500.00"), TransactionType.INCOME, "freelance")
        ));

        SummaryResponse result = summaryService.getSummary();

        assertThat(result.totalIncome()).isEqualByComparingTo("1500.00");
        assertThat(result.totalExpense()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getSummary_shouldSumExpenses_correctly() {
        when(transactionService.getAllTransactions()).thenReturn(List.of(
                buildTransaction(new BigDecimal("200.00"), TransactionType.EXPENSE, "food"),
                buildTransaction(new BigDecimal("150.00"), TransactionType.EXPENSE, "transport")
        ));

        SummaryResponse result = summaryService.getSummary();

        assertThat(result.totalExpense()).isEqualByComparingTo("350.00");
        assertThat(result.totalIncome()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getSummary_shouldGroupExpensesByCategory() {
        when(transactionService.getAllTransactions()).thenReturn(List.of(
                buildTransaction(new BigDecimal("100.00"), TransactionType.EXPENSE, "food"),
                buildTransaction(new BigDecimal("50.00"), TransactionType.EXPENSE, "food"),
                buildTransaction(new BigDecimal("200.00"), TransactionType.EXPENSE, "rent")
        ));

        SummaryResponse result = summaryService.getSummary();

        assertThat(result.expensesByCategory()).containsEntry("food", new BigDecimal("150.00"));
        assertThat(result.expensesByCategory()).containsEntry("rent", new BigDecimal("200.00"));
    }

    @Test
    void getSummary_shouldNotIncludeIncomes_inExpensesByCategory() {
        when(transactionService.getAllTransactions()).thenReturn(List.of(
                buildTransaction(new BigDecimal("500.00"), TransactionType.INCOME, "salary"),
                buildTransaction(new BigDecimal("100.00"), TransactionType.EXPENSE, "food")
        ));

        SummaryResponse result = summaryService.getSummary();

        assertThat(result.expensesByCategory()).doesNotContainKey("salary");
        assertThat(result.expensesByCategory()).containsKey("food");
    }

    @Test
    void getSummary_shouldCalculateBoth_whenMixedTransactionsExist() {
        when(transactionService.getAllTransactions()).thenReturn(List.of(
                buildTransaction(new BigDecimal("2000.00"), TransactionType.INCOME, "salary"),
                buildTransaction(new BigDecimal("400.00"), TransactionType.EXPENSE, "food"),
                buildTransaction(new BigDecimal("800.00"), TransactionType.EXPENSE, "rent"),
                buildTransaction(new BigDecimal("300.00"), TransactionType.INCOME, "bonus")
        ));

        SummaryResponse result = summaryService.getSummary();

        assertThat(result.totalIncome()).isEqualByComparingTo("2300.00");
        assertThat(result.totalExpense()).isEqualByComparingTo("1200.00");
        assertThat(result.expensesByCategory()).hasSize(2);
    }

    // --- helpers ---

    private Transaction buildTransaction(BigDecimal amount, TransactionType type, String category) {
        Transaction t = new Transaction();
        t.setId(UUID.randomUUID());
        t.setAmount(amount);
        t.setType(type);
        t.setCategory(category);
        t.setDateTime(LocalDateTime.now());
        Account account = new Account("Test", BigDecimal.ZERO);
        account.setId(UUID.randomUUID());
        t.setAccount(account);
        return t;
    }
}