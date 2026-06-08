import {Component, computed, inject, OnInit, signal} from '@angular/core';
import {CurrencyPipe, DatePipe} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {NotificationService} from '../../services/notification-service';
import {Transaction} from '../../models/transactions.models';
import {AccountsService} from '../../services/accounts-service';
import {TransactionsService} from '../../services/transactions-service';
import {AccountDetailsData} from '../../models/accounts.models';

@Component({
  selector: 'app-account-details',
  imports: [
    CurrencyPipe,
    DatePipe
  ],
  templateUrl: './account-details.html',
  styles: ``,
})
export class AccountDetails implements OnInit {
  private readonly accountService: AccountsService = inject(AccountsService);
  private readonly transactionService: TransactionsService = inject(TransactionsService);
  private readonly notificationService: NotificationService = inject(NotificationService);
  private readonly route: ActivatedRoute = inject(ActivatedRoute);
  private readonly router: Router = inject(Router);

  account = signal<AccountDetailsData | null>(null);
  transactions = signal<Transaction[]>([]);
  loadingTx = signal(true);

  private readonly id = this.route.snapshot.paramMap.get('id')!;

  totalIncome = computed(() =>
    this.transactions()
      .filter(t => t.type === 'INCOME')
      .reduce((s, t) => s + t.amount, 0)
  );
  totalExpense = computed(() =>
    this.transactions()
      .filter(t => t.type === 'EXPENSE')
      .reduce((s, t) => s + t.amount, 0)
  );

  ngOnInit(): void {
    this.accountService.getAccount(this.id).subscribe({
      next: acc => {
        this.account.set(acc);
      },
      error: e => {
        this.notificationService.show(e.message, 'error');
        this.router.navigate(['/dashboard']);
      },
    });

    this.accountService.getAccountTransactions(this.id).subscribe({
      next: txs => {
        this.transactions.set(txs.filter(t => t.accountId === this.id));
        this.loadingTx.set(false);
      },
      error: e => {
        this.notificationService.show(e.message, 'error');
        this.loadingTx.set(false);
      },
    });
  }

  exportCsv(): void {
    const acc = this.account();
    if (!acc) return;
    this.accountService.exportAccountCsv(acc.id, acc.name);
  }

  deleteTransaction(id: string): void {
    this.transactionService.deleteTransaction(id).subscribe({
      next: () => {
        this.transactions.update(t => t.filter(x => x.id !== id));
        this.notificationService.show('Transaction deleted');
        this.accountService.getAccount(this.id).subscribe({
          next: acc => this.account.set(acc),
        });
      },
      error: e => this.notificationService.show(e.message, 'error'),
    });
  }

  goBack(): void {
    this.router.navigate(['/dashboard']);
  }
}
