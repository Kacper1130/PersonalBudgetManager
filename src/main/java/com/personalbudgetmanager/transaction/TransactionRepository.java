package com.personalbudgetmanager.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    @Query("SELECT t FROM Transaction t WHERE " +
            "(CAST(:from AS timestamp) IS NULL OR t.dateTime >= :from) AND " +
            "(CAST(:to AS timestamp) IS NULL OR t.dateTime <= :to) AND " +
            "(CAST(:category AS string) IS NULL OR t.category = :category)")
    List<Transaction> findWithFilters(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, @Param("category") String category);
}
