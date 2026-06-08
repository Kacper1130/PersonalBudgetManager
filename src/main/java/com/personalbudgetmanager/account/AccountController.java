package com.personalbudgetmanager.account;

import com.personalbudgetmanager.account.dto.AccountResponse;
import com.personalbudgetmanager.account.dto.CreateAccountRequest;
import com.personalbudgetmanager.transaction.TransactionService;
import com.personalbudgetmanager.transaction.dto.TransactionResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
@AllArgsConstructor
class AccountController {

    private final AccountService accountService;
    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest createAccountRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.createAccount(createAccountRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountDetailsResponse> getAccountDetails(@PathVariable UUID id) {
        return ResponseEntity.ok(accountService.getAccountDetails(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable UUID id) {
        accountService.deleteAccount(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<TransactionResponse>> getAccountTransactions(@PathVariable("id") UUID accountId) {
        return ResponseEntity.ok(transactionService.getAccountTransactions(accountId));
    }

    @GetMapping(value = "/{id}/transactions/export", produces = "text/csv")
    public ResponseEntity<String> exportAccountTransactions(@PathVariable("id") UUID accountId) {
        String csv = transactionService.exportAccountTransactions(accountId);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"transactions_account_" + accountId + ".csv\"");
        headers.set(HttpHeaders.CONTENT_TYPE, "text/csv");

        return ResponseEntity.ok().headers(headers).body(csv);
    }
}
