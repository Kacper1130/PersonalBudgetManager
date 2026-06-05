package com.personalbudgetmanager.budgetLimit;

import com.personalbudgetmanager.budgetLimit.dto.BudgetLimitDto;
import com.personalbudgetmanager.exception.BudgetLimitNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetLimitServiceTest {

    @Mock
    private BudgetLimitRepository budgetLimitRepository;

    @InjectMocks
    private BudgetLimitService budgetLimitService;

    // --- setLimitForCategory ---

    @Test
    void setLimitForCategory_shouldCreateNewLimit_whenCategoryDoesNotExist() {
        BudgetLimitDto request = new BudgetLimitDto("Food", new BigDecimal("500.00"));
        BudgetLimit saved = budgetLimitWithCategoryAndAmount("food", new BigDecimal("500.00"));

        when(budgetLimitRepository.findByCategory("food")).thenReturn(Optional.empty());
        when(budgetLimitRepository.save(any())).thenReturn(saved);

        BudgetLimitDto result = budgetLimitService.setLimitForCategory(request);

        assertThat(result.category()).isEqualTo("food");
        assertThat(result.limitAmount()).isEqualByComparingTo("500.00");
    }

    @Test
    void setLimitForCategory_shouldUpdateExistingLimit_whenCategoryExists() {
        BudgetLimitDto request = new BudgetLimitDto("food", new BigDecimal("800.00"));
        BudgetLimit existing = budgetLimitWithCategoryAndAmount("food", new BigDecimal("300.00"));
        BudgetLimit updated = budgetLimitWithCategoryAndAmount("food", new BigDecimal("800.00"));

        when(budgetLimitRepository.findByCategory("food")).thenReturn(Optional.of(existing));
        when(budgetLimitRepository.save(any())).thenReturn(updated);

        BudgetLimitDto result = budgetLimitService.setLimitForCategory(request);

        assertThat(result.limitAmount()).isEqualByComparingTo("800.00");
        verify(budgetLimitRepository).save(argThat(b -> b.getLimitAmount().compareTo(new BigDecimal("800.00")) == 0));
    }

    @Test
    void setLimitForCategory_shouldNormalizeCategory_trimAndLowercase() {
        BudgetLimitDto request = new BudgetLimitDto("  FOOD  ", new BigDecimal("200.00"));
        BudgetLimit saved = budgetLimitWithCategoryAndAmount("food", new BigDecimal("200.00"));

        when(budgetLimitRepository.findByCategory("food")).thenReturn(Optional.empty());
        when(budgetLimitRepository.save(any())).thenReturn(saved);

        budgetLimitService.setLimitForCategory(request);

        verify(budgetLimitRepository).findByCategory("food");
        ArgumentCaptor<BudgetLimit> captor = ArgumentCaptor.forClass(BudgetLimit.class);
        verify(budgetLimitRepository).save(captor.capture());
        assertThat(captor.getValue().getCategory()).isEqualTo("food");
    }

    // --- getAllLimits ---

    @Test
    void getAllLimits_shouldReturnMappedList() {
        BudgetLimit b1 = budgetLimitWithCategoryAndAmount("food", new BigDecimal("300.00"));
        BudgetLimit b2 = budgetLimitWithCategoryAndAmount("transport", new BigDecimal("150.00"));
        when(budgetLimitRepository.findAll()).thenReturn(List.of(b1, b2));

        List<BudgetLimitDto> result = budgetLimitService.getAllLimits();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(BudgetLimitDto::category).containsExactlyInAnyOrder("food", "transport");
    }

    @Test
    void getAllLimits_shouldReturnEmptyList_whenNoLimitsSet() {
        when(budgetLimitRepository.findAll()).thenReturn(List.of());

        List<BudgetLimitDto> result = budgetLimitService.getAllLimits();

        assertThat(result).isEmpty();
    }

    // --- getBudgetLimit ---

    @Test
    void getBudgetLimit_shouldReturnAmount_whenLimitExists() {
        BudgetLimit limit = budgetLimitWithCategoryAndAmount("food", new BigDecimal("400.00"));
        when(budgetLimitRepository.findByCategory("food")).thenReturn(Optional.of(limit));

        Optional<BigDecimal> result = budgetLimitService.getBudgetLimit("food");

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualByComparingTo("400.00");
    }

    @Test
    void getBudgetLimit_shouldReturnEmpty_whenLimitDoesNotExist() {
        when(budgetLimitRepository.findByCategory("unknown")).thenReturn(Optional.empty());

        Optional<BigDecimal> result = budgetLimitService.getBudgetLimit("unknown");

        assertThat(result).isEmpty();
    }

    @Test
    void getBudgetLimit_shouldNormalizeCategory_trimAndLowercase() {
        when(budgetLimitRepository.findByCategory("food")).thenReturn(Optional.empty());

        budgetLimitService.getBudgetLimit("  FOOD  ");

        verify(budgetLimitRepository).findByCategory("food");
    }

    // --- deleteBudgetLimit ---

    @Test
    void deleteBudgetLimit_shouldDelete_whenCategoryExists() {
        when(budgetLimitRepository.existsByCategory("food")).thenReturn(true);

        budgetLimitService.deleteBudgetLimit("food");

        verify(budgetLimitRepository).deleteByCategory("food");
    }

    @Test
    void deleteBudgetLimit_shouldThrowBudgetLimitNotFoundException_whenCategoryDoesNotExist() {
        when(budgetLimitRepository.existsByCategory("unknown")).thenReturn(false);

        assertThatThrownBy(() -> budgetLimitService.deleteBudgetLimit("unknown"))
                .isInstanceOf(BudgetLimitNotFoundException.class);

        verify(budgetLimitRepository, never()).deleteByCategory(any());
    }

    @Test
    void deleteBudgetLimit_shouldNormalizeCategory_trimAndLowercase() {
        when(budgetLimitRepository.existsByCategory("food")).thenReturn(true);

        budgetLimitService.deleteBudgetLimit("  FOOD  ");

        verify(budgetLimitRepository).existsByCategory("food");
        verify(budgetLimitRepository).deleteByCategory("food");
    }

    // --- helpers ---

    private BudgetLimit budgetLimitWithCategoryAndAmount(String category, BigDecimal amount) {
        BudgetLimit bl = new BudgetLimit();
        bl.setCategory(category);
        bl.setLimitAmount(amount);
        return bl;
    }
}