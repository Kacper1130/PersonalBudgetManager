import {Component, computed, inject, OnInit, signal} from '@angular/core';
import {Router} from '@angular/router';
import {NotificationService} from '../../services/notification-service';
import {SummaryService} from '../../services/summary-service';
import {SummaryResponse} from '../../models/summary.models';
import {AccountsService} from '../../services/accounts-service';
import {AccountSummary} from '../../models/accounts.models';
import {CurrencyPipe} from '@angular/common';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';

@Component({
  selector: 'app-dashboard',
  imports: [
    CurrencyPipe,
    ReactiveFormsModule,
    FormsModule
  ],
  templateUrl: './dashboard.html',
  styles: ``,
})
export class Dashboard implements OnInit {
  private readonly summaryService: SummaryService = inject(SummaryService);
  private readonly accountsService: AccountsService = inject(AccountsService);
  private readonly notificationService = inject(NotificationService);
  private readonly router = inject(Router);

  accounts = signal<AccountSummary[]>([]);
  summary = signal<SummaryResponse | null>(null);
  loading = signal(true);

  showAddModal = signal(false);
  newAccountName = signal('');
  newInitialBal = signal<number | null>(null);

  catEntries = computed(() =>
    Object.entries(this.summary()?.expensesByCategory ?? {})
      .sort((a, b) => b[1] - a[1])
  );
  sumCat = computed(() =>
    this.catEntries().reduce((acc, entry) => acc + entry[1], 0)
  );
  net = computed(() =>
    (this.summary()?.totalIncome ?? 0) - (this.summary()?.totalExpense ?? 0)
  );

  ngOnInit(): void {
    this.summaryService.getSummary().subscribe({
      next: s => this.summary.set(s),
      error: e => this.notificationService.show(e.message, 'error'),
    });

    this.accountsService.getAccounts().subscribe({
      next: accounts => {
        this.accounts.set(accounts);
        this.loading.set(false);
      },
      error: e => {
        this.notificationService.show(e.message, 'error');
        this.loading.set(false);
      },
    });
  }

  openAccount(id: string): void {
    this.router.navigate(['/accounts', id]);
  }

  createAccount(): void {
    const name = this.newAccountName().trim();
    if (!name) return;

    this.accountsService.createAccount({
      name,
      ...(this.newInitialBal() != null ? {balance: this.newInitialBal()!} : {}),
    }).subscribe({
      next: acc => {
        // acc is AccountDetails but AccountSummary is a subset — safe to cast
        this.accounts.update(a => [...a, {id: acc.id, name: acc.name}]);
        this.notificationService.show(`Account "${acc.name}" created`);
        this.showAddModal.set(false);
        this.newAccountName.set('');
        this.newInitialBal.set(null);
      },
      error: e => this.notificationService.show(e.message, 'error'),
    });
  }

  deleteAccount(id: string): void {
    this.accountsService.deleteAccount(id).subscribe({
      next: () => {
        this.accounts.update(a => a.filter(x => x.id !== id));
        this.notificationService.show('Account deleted');
      },
      error: e => this.notificationService.show(e.message, 'error'),
    });
  }

  barWidth(val: number): string {
    return `${((val / this.sumCat()) * 100).toFixed(1)}%`;
  }
}
