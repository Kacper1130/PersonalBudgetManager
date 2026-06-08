package com.personalbudgetmanager.transaction;

import com.personalbudgetmanager.account.Account;
import com.personalbudgetmanager.account.AccountService;
import com.personalbudgetmanager.budgetLimit.BudgetLimitService;
import com.personalbudgetmanager.exception.CsvGenerationException;
import com.personalbudgetmanager.exception.TransactionNotFoundException;
import com.personalbudgetmanager.transaction.dto.CreateTransactionRequest;
import com.personalbudgetmanager.transaction.dto.TransactionCreatedResponse;
import com.personalbudgetmanager.transaction.dto.TransactionResponse;
import lombok.AllArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final BudgetLimitService budgetLimitService;

    List<TransactionResponse> getTransactions(LocalDateTime from, LocalDateTime to, String category) {
        List<Transaction> response = transactionRepository.findWithFilters(from, to, category);

        return response.stream()
                .map(t -> new TransactionResponse(
                        t.getId(),
                        t.getAmount(),
                        t.getType().name(),
                        t.getCategory(),
                        t.getDescription(),
                        t.getDateTime(),
                        t.getAccount().getId()
                ))
                .toList();
    }

    @Transactional
    TransactionCreatedResponse createTransaction(CreateTransactionRequest createTransactionRequest) {
        Account account = accountService.getAccount(createTransactionRequest.accountId());

        Transaction transaction = new Transaction();
        transaction.setAmount(createTransactionRequest.amount());
        transaction.setType(createTransactionRequest.type());
        transaction.setCategory(createTransactionRequest.category());
        transaction.setDescription(createTransactionRequest.description());
        LocalDateTime dateTime = createTransactionRequest.dateTime() != null ? createTransactionRequest.dateTime() : LocalDateTime.now();
        transaction.setDateTime(dateTime);
        transaction.setAccount(account);

        Transaction savedTransaction = transactionRepository.save(transaction);

        Optional<String> warningMessage = Optional.empty();

        if (createTransactionRequest.type() == TransactionType.INCOME) {
            account.setBalance(account.getBalance().add(createTransactionRequest.amount()));
        } else if (createTransactionRequest.type() == TransactionType.EXPENSE) {
            account.setBalance(account.getBalance().subtract(createTransactionRequest.amount()));
            Optional<BigDecimal> budgetLimit = budgetLimitService.getBudgetLimit(savedTransaction.getCategory());
            if (budgetLimit.isPresent()) {
                warningMessage = checkLimitAndGetWarning(savedTransaction.getCategory(), budgetLimit.get());
            }
        }

        TransactionResponse transactionResponse = new TransactionResponse(
                savedTransaction.getId(),
                savedTransaction.getAmount(),
                savedTransaction.getType().name(),
                savedTransaction.getCategory(),
                savedTransaction.getDescription(),
                savedTransaction.getDateTime(),
                savedTransaction.getAccount().getId()
        );

        return new TransactionCreatedResponse(transactionResponse, warningMessage.orElse(null));
    }

    @Transactional
    void deleteTransaction(UUID id) {
        Transaction transaction = transactionRepository.findById(id).orElseThrow(() -> new TransactionNotFoundException(id));

        Account account = transaction.getAccount();

        if (transaction.getType() == TransactionType.INCOME) {
            account.setBalance(account.getBalance().subtract(transaction.getAmount()));
        } else if (transaction.getType() == TransactionType.EXPENSE) {
            account.setBalance(account.getBalance().add(transaction.getAmount()));
        }

        transactionRepository.delete(transaction);
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public List<TransactionResponse> getAccountTransactions(UUID accountId) {
        accountService.verifyAccountExists(accountId);
        List<Transaction> transactions = transactionRepository.findByAccountIdOrderByDateTimeDesc(accountId);

        return transactions.stream()
                .map(t -> new TransactionResponse(
                        t.getId(),
                        t.getAmount(),
                        t.getType().name(),
                        t.getCategory(),
                        t.getDescription(),
                        t.getDateTime(),
                        t.getAccount().getId()
                )).toList();
    }

    public String exportAccountTransactions(UUID accountId) {
        List<TransactionResponse> transactions = getAccountTransactions(accountId);

        StringWriter sw = new StringWriter();
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader("ID", "Amount", "Type", "Category", "Description", "Date")
                .build();

        try (final CSVPrinter printer = new CSVPrinter(sw, csvFormat)) {
            for (var t : transactions) {
                printer.printRecord(
                        t.id(),
                        t.amount(),
                        t.type(),
                        t.category(),
                        t.description() != null ? t.description() : "",
                        t.dateTime()
                );
            }
        } catch (IOException e) {
            throw new CsvGenerationException();
        }

        return sw.toString();
    }

    private Optional<String> checkLimitAndGetWarning(String category, BigDecimal limitAmount) {
        BigDecimal currentMonthSum = getSumOfExpensesForCurrentMonth(category);

        if (currentMonthSum.compareTo(limitAmount) > 0) {
            return Optional.of(
                    String.format(
                            "Warning: Budget limit for category '%s' has been exceeded! Limit: %s, Current Month Total: %s",
                            category, limitAmount, currentMonthSum
                    )
            );
        }

        return Optional.empty();
    }

    private BigDecimal getSumOfExpensesForCurrentMonth(String category) {
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        return transactionRepository.sumExpensesByCategoryInCurrentMonth(category, startOfMonth);
    }
}
