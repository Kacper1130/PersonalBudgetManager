package com.personalbudgetmanager.account;

import com.personalbudgetmanager.account.dto.AccountResponse;
import com.personalbudgetmanager.account.dto.CreateAccountRequest;
import com.personalbudgetmanager.exception.AccountHasTransactionsException;
import com.personalbudgetmanager.exception.AccountNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    List<AccountResponse> getAllAccounts() {
        return accountRepository.findAll().stream().map(account -> new AccountResponse(account.getId(), account.getName())).toList();
    }

    AccountResponse createAccount(CreateAccountRequest createAccountRequest) {
        BigDecimal balance = createAccountRequest.balance() != null ? createAccountRequest.balance() : BigDecimal.ZERO;
        Account account = accountRepository.save(new Account(createAccountRequest.name(), balance));
        return new AccountResponse(account.getId(), account.getName());
    }

    AccountDetailsResponse getAccountDetails(UUID id) {
        return accountRepository.findById(id)
                .map(account -> new AccountDetailsResponse(account.getId(), account.getName(), account.getBalance()))
                .orElseThrow(() -> new AccountNotFoundException(id));
    }

    void deleteAccount(UUID id) {
        Account account = accountRepository.findById(id).orElseThrow(() -> new AccountNotFoundException(id));

        if (!account.getTransactions().isEmpty()) throw new AccountHasTransactionsException();

        accountRepository.delete(account);
    }

    public Account getAccount(UUID id) {
        return accountRepository.findById(id).orElseThrow(() -> new AccountNotFoundException(id));
    }

    public void verifyAccountExists(UUID accountId) {
        if (!accountRepository.existsById(accountId)) {
            throw new AccountNotFoundException(accountId);
        }
    }
}
