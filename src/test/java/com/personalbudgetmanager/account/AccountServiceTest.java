package com.personalbudgetmanager.account;

import com.personalbudgetmanager.account.dto.AccountResponse;
import com.personalbudgetmanager.account.dto.CreateAccountRequest;
import com.personalbudgetmanager.exception.AccountHasTransactionsException;
import com.personalbudgetmanager.exception.AccountNotFoundException;
import com.personalbudgetmanager.transaction.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    private UUID accountId;
    private Account account;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
        account = new Account("Test Account", new BigDecimal("100.00"));
        account.setId(accountId);
    }

    // --- getAllAccounts ---

    @Test
    void getAllAccounts_shouldReturnMappedResponses() {
        Account second = new Account("Second Account", BigDecimal.ZERO);
        second.setId(UUID.randomUUID());
        when(accountRepository.findAll()).thenReturn(List.of(account, second));

        List<AccountResponse> result = accountService.getAllAccounts();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(accountId);
        assertThat(result.get(0).name()).isEqualTo("Test Account");
        assertThat(result.get(1).name()).isEqualTo("Second Account");
    }

    @Test
    void getAllAccounts_shouldReturnEmptyList_whenNoAccountsExist() {
        when(accountRepository.findAll()).thenReturn(List.of());

        List<AccountResponse> result = accountService.getAllAccounts();

        assertThat(result).isEmpty();
    }

    // --- createAccount ---

    @Test
    void createAccount_shouldSaveAndReturnResponse_withProvidedBalance() {
        CreateAccountRequest request = new CreateAccountRequest("Savings", new BigDecimal("500.00"));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        AccountResponse result = accountService.createAccount(request);

        assertThat(result.id()).isEqualTo(accountId);
        assertThat(result.name()).isEqualTo("Test Account");
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void createAccount_shouldDefaultBalanceToZero_whenBalanceIsNull() {
        CreateAccountRequest request = new CreateAccountRequest("No-Balance Account", null);
        Account savedAccount = new Account("No-Balance Account", BigDecimal.ZERO);
        savedAccount.setId(UUID.randomUUID());
        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);

        AccountResponse result = accountService.createAccount(request);

        assertThat(result.name()).isEqualTo("No-Balance Account");
        verify(accountRepository).save(argThat(a -> a.getBalance().compareTo(BigDecimal.ZERO) == 0));
    }

    // --- getAccountDetails ---

    @Test
    void getAccountDetails_shouldReturnDetails_whenAccountExists() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        AccountDetailsResponse result = accountService.getAccountDetails(accountId);

        assertThat(result.id()).isEqualTo(accountId);
        assertThat(result.name()).isEqualTo("Test Account");
        assertThat(result.balance()).isEqualByComparingTo("100.00");
    }

    @Test
    void getAccountDetails_shouldThrowAccountNotFoundException_whenAccountDoesNotExist() {
        UUID missingId = UUID.randomUUID();
        when(accountRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccountDetails(missingId))
                .isInstanceOf(AccountNotFoundException.class);
    }

    // --- deleteAccount ---

    @Test
    void deleteAccount_shouldDeleteAccount_whenAccountHasNoTransactions() {
        account.setTransactions(List.of());
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        accountService.deleteAccount(accountId);

        verify(accountRepository).delete(account);
    }

    @Test
    void deleteAccount_shouldThrowAccountHasTransactionsException_whenAccountHasTransactions() {
        account.setTransactions(List.of(new Transaction()));
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.deleteAccount(accountId))
                .isInstanceOf(AccountHasTransactionsException.class);

        verify(accountRepository, never()).delete(any());
    }

    @Test
    void deleteAccount_shouldThrowAccountNotFoundException_whenAccountDoesNotExist() {
        UUID missingId = UUID.randomUUID();
        when(accountRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.deleteAccount(missingId))
                .isInstanceOf(AccountNotFoundException.class);

        verify(accountRepository, never()).delete(any());
    }

    // --- getAccount ---

    @Test
    void getAccount_shouldReturnAccount_whenExists() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        Account result = accountService.getAccount(accountId);

        assertThat(result).isEqualTo(account);
    }

    @Test
    void getAccount_shouldThrowAccountNotFoundException_whenNotFound() {
        UUID missingId = UUID.randomUUID();
        when(accountRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccount(missingId))
                .isInstanceOf(AccountNotFoundException.class);
    }

    // --- verifyAccountExists ---

    @Test
    void verifyAccountExists_shouldNotThrow_whenAccountExists() {
        when(accountRepository.existsById(accountId)).thenReturn(true);

        // Should not throw
        accountService.verifyAccountExists(accountId);
    }

    @Test
    void verifyAccountExists_shouldThrowAccountNotFoundException_whenAccountDoesNotExist() {
        UUID missingId = UUID.randomUUID();
        when(accountRepository.existsById(missingId)).thenReturn(false);

        assertThatThrownBy(() -> accountService.verifyAccountExists(missingId))
                .isInstanceOf(AccountNotFoundException.class);
    }
}