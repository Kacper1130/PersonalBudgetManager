package com.personalbudgetmanager.transaction;

import com.personalbudgetmanager.account.Account;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    private TransactionType type;
    private String category;
    private String description;
    private LocalDateTime dateTime;
    @ManyToOne
    private Account account;
}
