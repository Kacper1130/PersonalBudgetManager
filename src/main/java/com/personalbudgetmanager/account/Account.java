package com.personalbudgetmanager.account;

import com.personalbudgetmanager.transaction.Transaction;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String name;
    private BigDecimal balance;
    @OneToMany(mappedBy = "account")
    List<Transaction> transactions;

    public Account(String name, BigDecimal balance) {
        this.name = name;
        this.balance = balance;
    }
}
