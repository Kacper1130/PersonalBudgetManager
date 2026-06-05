package com.personalbudgetmanager.budgetLimit;

import com.personalbudgetmanager.budgetLimit.dto.BudgetLimitDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/budget-limits")
@RequiredArgsConstructor
class BudgetLimitController {

    private final BudgetLimitService budgetLimitService;

    @GetMapping
    public ResponseEntity<List<BudgetLimitDto>> getLimits() {
        return ResponseEntity.ok(budgetLimitService.getAllLimits());
    }

    @PostMapping
    public ResponseEntity<BudgetLimitDto> setLimit(@Valid @RequestBody BudgetLimitDto request) {
        return ResponseEntity.ok(budgetLimitService.setLimitForCategory(request));
    }

    @DeleteMapping("/{category}")
    public ResponseEntity<Void> deleteLimit(@PathVariable String category) {
        budgetLimitService.deleteBudgetLimit(category);
        return ResponseEntity.ok().build();
    }
}
