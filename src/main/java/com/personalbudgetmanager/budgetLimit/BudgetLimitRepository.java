package com.personalbudgetmanager.budgetLimit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface BudgetLimitRepository extends JpaRepository<BudgetLimit, Long> {
    Optional<BudgetLimit> findByCategory(String category);

    boolean existsByCategory(String category);

    void deleteByCategory(String category);
}
