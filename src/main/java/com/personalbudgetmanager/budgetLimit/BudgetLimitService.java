package com.personalbudgetmanager.budgetLimit;

import com.personalbudgetmanager.budgetLimit.dto.BudgetLimitDto;
import com.personalbudgetmanager.exception.BudgetLimitNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BudgetLimitService {

    private final BudgetLimitRepository budgetLimitRepository;

    BudgetLimitDto setLimitForCategory(BudgetLimitDto request) {
        BudgetLimit budgetLimit = budgetLimitRepository.findByCategory(request.category().trim().toLowerCase())
                .orElse(new BudgetLimit());

        budgetLimit.setCategory(request.category().trim().toLowerCase());
        budgetLimit.setLimitAmount(request.limitAmount());

        BudgetLimit savedBudgetLimit = budgetLimitRepository.save(budgetLimit);
        return new BudgetLimitDto(savedBudgetLimit.getCategory(), savedBudgetLimit.getLimitAmount());
    }

    List<BudgetLimitDto> getAllLimits() {
        return budgetLimitRepository.findAll().stream()
                .map(b -> new BudgetLimitDto(b.getCategory(), b.getLimitAmount()))
                .toList();
    }

    public Optional<BigDecimal> getBudgetLimit(String category) {
        return budgetLimitRepository.findByCategory(category.trim().toLowerCase()).map(BudgetLimit::getLimitAmount);
    }

    @Transactional
    void deleteBudgetLimit(String category) {
        String normalizedCategory = category.trim().toLowerCase();
        if (!budgetLimitRepository.existsByCategory(normalizedCategory)) {
            throw new BudgetLimitNotFoundException(normalizedCategory);
        }
        budgetLimitRepository.deleteByCategory(normalizedCategory);
    }
}
