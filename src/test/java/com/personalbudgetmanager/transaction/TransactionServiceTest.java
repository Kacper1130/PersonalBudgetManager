package com.personalbudgetmanager.transaction;

import com.personalbudgetmanager.account.Account;
import com.personalbudgetmanager.account.AccountService;
import com.personalbudgetmanager.budgetLimit.BudgetLimitService;
import com.personalbudgetmanager.transaction.dto.CreateTransactionRequest;
import com.personalbudgetmanager.transaction.dto.TransactionCreatedResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountService accountService;

    @Mock
    private BudgetLimitService budgetLimitService;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void createTransaction_Income_ShouldIncreaseAccountBalanceAndNoWarning() {
        // Arrange
        Account account = new Account("Main", BigDecimal.valueOf(1000));
        when(accountService.getAccount(any())).thenReturn(account);
        when(transactionRepository.save(any())).thenAnswer(i -> {
            Transaction t = i.getArgument(0);
            t.setId(UUID.randomUUID());
            return t;
        });

        CreateTransactionRequest request = new CreateTransactionRequest(
                BigDecimal.valueOf(500), TransactionType.INCOME, "Wypłata", "Pensja", LocalDateTime.now(), UUID.randomUUID()
        );

        // Act
        TransactionCreatedResponse response = transactionService.createTransaction(request);

        // Assert
        assertNull(response.warning());
        assertEquals(BigDecimal.valueOf(1500), account.getBalance());
        verify(budgetLimitService, never()).getBudgetLimit(anyString()); // Nie sprawdzamy limitów dla przychodów
    }

    @Test
    void createTransaction_ExpenseExceedingLimit_ShouldDecreaseBalanceAndReturnWarning() {
        // Arrange
        Account account = new Account("Main", BigDecimal.valueOf(1000));
        when(accountService.getAccount(any())).thenReturn(account);

        // Zwracamy limit = 200 zł dla kategorii jedzenie
        when(budgetLimitService.getBudgetLimit("jedzenie")).thenReturn(Optional.of(BigDecimal.valueOf(200)));

        // Symulujemy, że wydaliśmy już w tym miesiącu 250 zł
        when(transactionRepository.sumExpensesByCategoryInCurrentMonth(eq("jedzenie"), any())).thenReturn(BigDecimal.valueOf(250));

        when(transactionRepository.save(any())).thenAnswer(i -> {
            Transaction t = i.getArgument(0);
            t.setId(UUID.randomUUID());
            return t;
        });

        CreateTransactionRequest request = new CreateTransactionRequest(
                BigDecimal.valueOf(50), TransactionType.EXPENSE, "jedzenie", "Pizza", LocalDateTime.now(), UUID.randomUUID()
        );

        // Act
        TransactionCreatedResponse response = transactionService.createTransaction(request);

        // Assert
        assertEquals(BigDecimal.valueOf(950), account.getBalance()); // 1000 - 50
        assertNotNull(response.warning());
    }

    @Test
    void createTransaction_ExpenseUnderLimit_ShouldDecreaseBalanceAndNoWarning() {
        // Arrange
        Account account = new Account("Main", BigDecimal.valueOf(1000));
        when(accountService.getAccount(any())).thenReturn(account);

        // Limit 500, wydane do tej pory 100
        when(budgetLimitService.getBudgetLimit("jedzenie")).thenReturn(Optional.of(BigDecimal.valueOf(500)));
        when(transactionRepository.sumExpensesByCategoryInCurrentMonth(eq("jedzenie"), any())).thenReturn(BigDecimal.valueOf(100));

        when(transactionRepository.save(any())).thenAnswer(i -> {
            Transaction t = i.getArgument(0);
            t.setId(UUID.randomUUID());
            return t;
        });

        CreateTransactionRequest request = new CreateTransactionRequest(
                BigDecimal.valueOf(50), TransactionType.EXPENSE, "jedzenie", "Pizza", LocalDateTime.now(), UUID.randomUUID()
        );

        // Act
        TransactionCreatedResponse response = transactionService.createTransaction(request);

        // Assert
        assertEquals(BigDecimal.valueOf(950), account.getBalance()); // 1000 - 50
        assertNull(response.warning()); // Limit nieprzekroczony, brak ostrzeżenia
    }

    @Test
    void deleteTransaction_Income_ShouldDecreaseAccountBalance() {
        // Arrange
        UUID id = UUID.randomUUID();
        Account account = new Account("Main", BigDecimal.valueOf(1500));
        Transaction transaction = new Transaction();
        transaction.setId(id);
        transaction.setAmount(BigDecimal.valueOf(500));
        transaction.setType(TransactionType.INCOME);
        transaction.setAccount(account);

        when(transactionRepository.findById(id)).thenReturn(Optional.of(transaction));

        // Act
        transactionService.deleteTransaction(id);

        // Assert
        assertEquals(BigDecimal.valueOf(1000), account.getBalance()); // Wycofanie przychodu
        verify(transactionRepository).delete(transaction);
    }

    @Test
    void deleteTransaction_Expense_ShouldIncreaseAccountBalance() {
        // Arrange
        UUID id = UUID.randomUUID();
        Account account = new Account("Main", BigDecimal.valueOf(1000));
        Transaction transaction = new Transaction();
        transaction.setId(id);
        transaction.setAmount(BigDecimal.valueOf(200));
        transaction.setType(TransactionType.EXPENSE); // Usuwamy wydatek
        transaction.setAccount(account);

        when(transactionRepository.findById(id)).thenReturn(Optional.of(transaction));

        // Act
        transactionService.deleteTransaction(id);

        // Assert
        assertEquals(BigDecimal.valueOf(1200), account.getBalance()); // Pieniądze wracają na konto
        verify(transactionRepository).delete(transaction);
    }

    @Test
    void exportAccountTransactions_ShouldGenerateCsvString() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        Transaction t1 = new Transaction();
        t1.setId(UUID.randomUUID());
        t1.setAmount(BigDecimal.valueOf(100));
        t1.setType(TransactionType.EXPENSE);
        t1.setCategory("Paliwo");
        t1.setDateTime(LocalDateTime.now());

        when(transactionRepository.findByAccountId(accountId)).thenReturn(List.of(t1));

        // Act
        String csvResult = transactionService.exportAccountTransactions(accountId);

        // Assert
        assertNotNull(csvResult);
        assertTrue(csvResult.contains("Amount")); // Sprawdzamy czy są nagłówki
        assertTrue(csvResult.contains("100")); // Sprawdzamy czy są dane z transakcji
        assertTrue(csvResult.contains("Paliwo"));
        verify(accountService).verifyAccountExists(accountId);
    }
}