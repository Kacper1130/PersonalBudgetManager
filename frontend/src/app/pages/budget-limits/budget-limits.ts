import {Component, computed, inject, OnInit, signal} from '@angular/core';
import {BudgetLimitsService} from '../../services/budget-limits-service';
import {NotificationService} from '../../services/notification-service';
import {BudgetLimitDto} from '../../models/budget-limits.models';
import {SummaryResponse} from '../../models/summary.models';
import {SummaryService} from '../../services/summary-service';
import {CurrencyPipe, DecimalPipe} from '@angular/common';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';

interface LimitsWithProgres extends BudgetLimitDto {
  spent: number;
  pct: number;       // 0–100
  status: 'ok' | 'warn' | 'over';
}

@Component({
  selector: 'app-budget-limits',
  imports: [
    CurrencyPipe,
    DecimalPipe,
    ReactiveFormsModule,
    FormsModule
  ],
  templateUrl: './budget-limits.html',
  styles: ``,
})
export class BudgetLimits implements OnInit {
  private readonly budgetLimitService: BudgetLimitsService = inject(BudgetLimitsService);
  private readonly summaryService: SummaryService = inject(SummaryService);
  private readonly notificationService: NotificationService = inject(NotificationService);

  limits = signal<BudgetLimitDto[]>([]);
  summary = signal<SummaryResponse | null>(null);
  loading = signal(true);

  form = {category: '', limitAmount: 0};

  limitsWithProgress  = computed<LimitsWithProgres[]>(() => {
    const cats = this.summary()?.expensesByCategory ?? {};
    return this.limits().map(l => {
      const spent = cats[l.category.toLowerCase()] ?? 0;
      const pct = l.limitAmount > 0 ? Math.min((spent / l.limitAmount) * 100, 100) : 0;
      return {
        ...l,
        spent,
        pct,
        status: pct >= 100 ? 'over' : pct >= 75 ? 'warn' : 'ok',
      };
    });
  });

  ngOnInit(): void {
    this.summaryService.getSummary().subscribe({
      next: s => this.summary.set(s),
      error: e => this.notificationService.show(e.message, 'error'),
    });
    this.budgetLimitService.getBudgetLimits().subscribe({
      next: ls => {
        this.limits.set(ls);
        this.loading.set(false);
      },
      error: e => {
        this.notificationService.show(e.message, 'error');
        this.loading.set(false);
      },
    });
  }

  save(): void {
    const cat = this.form.category.trim().toLowerCase();
    if (!cat || !this.form.limitAmount) {
      this.notificationService.show('Category and amount are required', 'warning');
      return;
    }
    this.budgetLimitService.upsertBudgetLimit({category: cat, limitAmount: +this.form.limitAmount})
      .subscribe({
        next: saved => {
          this.limits.update(ls => {
            const idx = ls.findIndex(l => l.category === saved.category);
            return idx >= 0
              ? ls.map((l, i) => (i === idx ? saved : l))
              : [...ls, saved];
          });
          this.notificationService.show(`Limit set for "${saved.category}"`);
          this.form = {category: '', limitAmount: 0};
        },
        error: e => this.notificationService.show(e.message, 'error'),
      });
  }

  delete(category: string): void {
    this.budgetLimitService.deleteBudgetLimit(category).subscribe({
      next: () => {
        this.limits.update(ls => ls.filter(l => l.category !== category));
        this.notificationService.show(`Limit for "${category}" removed`);
      },
      error: e => this.notificationService.show(e.message, 'error'),
    });
  }

  editLimit(limit: BudgetLimitDto): void {
    this.form = {category: limit.category, limitAmount: limit.limitAmount};
  }
}
