package com.personalbudgetmanager.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    @Query("SELECT t FROM Transaction t WHERE " +
            "(CAST(:from AS timestamp) IS NULL OR t.dateTime >= :from) AND " +
            "(CAST(:to AS timestamp) IS NULL OR t.dateTime <= :to) AND " +
            "(CAST(:category AS string) IS NULL OR t.category = :category) ORDER BY t.dateTime DESC")
    List<Transaction> findWithFilters(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, @Param("category") String category);

    List<Transaction> findByAccountIdOrderByDateTimeDesc(UUID accountId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.type = 'EXPENSE' " +
            "AND t.category = :category " +
            "AND t.dateTime >= :startOfMonth")
    BigDecimal sumExpensesByCategoryInCurrentMonth(String category, LocalDateTime startOfMonth);
}
