package com.personalbudgetmanager.transaction;

import com.personalbudgetmanager.account.Account;
import com.personalbudgetmanager.account.AccountService;
import com.personalbudgetmanager.exception.CsvGenerationException;
import com.personalbudgetmanager.transaction.dto.CreateTransactionRequest;
import com.personalbudgetmanager.transaction.dto.TransactionResponse;
import com.personalbudgetmanager.exception.TransactionNotFoundException;
import lombok.AllArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

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
    TransactionResponse createTransaction(CreateTransactionRequest createTransactionRequest) {
        Account account = accountService.getAccount(createTransactionRequest.accountId());

        Transaction transaction = new Transaction();
        transaction.setAmount(createTransactionRequest.amount());
        transaction.setType(createTransactionRequest.type());
        transaction.setCategory(createTransactionRequest.category());
        transaction.setDescription(createTransactionRequest.description());
        LocalDateTime dateTime = createTransactionRequest.dateTime() != null ? createTransactionRequest.dateTime() : LocalDateTime.now();
        transaction.setDateTime(dateTime);
        transaction.setAccount(account);

        if (createTransactionRequest.type() == TransactionType.INCOME) {
            account.setBalance(account.getBalance().add(createTransactionRequest.amount()));
        } else if (createTransactionRequest.type() == TransactionType.EXPENSE) {
            account.setBalance(account.getBalance().subtract(createTransactionRequest.amount()));
        }

        Transaction savedTransaction = transactionRepository.save(transaction);

        return new TransactionResponse(
                savedTransaction.getId(),
                savedTransaction.getAmount(),
                savedTransaction.getType().name(),
                savedTransaction.getCategory(),
                savedTransaction.getDescription(),
                savedTransaction.getDateTime(),
                savedTransaction.getAccount().getId()
        );
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

    public String exportAccountTransactions(UUID accountId) {
        accountService.verifyAccountExists(accountId);
        List<Transaction> transactions = transactionRepository.findByAccountId(accountId);

        StringWriter sw = new StringWriter();
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader("ID", "Amount", "Type", "Category", "Description", "Date")
                .build();

        try (final CSVPrinter printer = new CSVPrinter(sw, csvFormat)) {
            for (var t : transactions) {
                printer.printRecord(
                        t.getId(),
                        t.getAmount(),
                        t.getType(),
                        t.getCategory(),
                        t.getDescription() != null ? t.getDescription() : "",
                        t.getDateTime()
                );
            }
        } catch (IOException e) {
            throw new CsvGenerationException();
        }

        return sw.toString();
    }
}
