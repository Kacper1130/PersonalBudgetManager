import {Component, inject, OnInit, signal} from '@angular/core';
import {CurrencyPipe, DatePipe} from '@angular/common';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {NotificationService} from '../../services/notification-service';
import {TransactionsService} from '../../services/transactions-service';
import {AccountSummary} from '../../models/accounts.models';
import {CreateTransactionRequest, Transaction, TransactionFilters} from '../../models/transactions.models';
import {AccountsService} from '../../services/accounts-service';

@Component({
  selector: 'app-transactions',
  imports: [
    CurrencyPipe,
    DatePipe,
    ReactiveFormsModule,
    FormsModule
  ],
  templateUrl: './transactions.html',
  styles: ``,
})
export class Transactions implements OnInit {
  private readonly transactionService: TransactionsService = inject(TransactionsService);
  private readonly accountsService: AccountsService = inject(AccountsService);
  private readonly notificationService: NotificationService = inject(NotificationService);

  accounts = signal<AccountSummary[]>([]);
  txList = signal<Transaction[]>([]);
  loading = signal(true);

  filters: TransactionFilters = {from: '', to: '', category: ''};

  form: CreateTransactionRequest & { dateTime?: string } = {
    accountId: '',
    amount: 0,
    type: 'EXPENSE',
    category: '',
    description: '',
    dateTime: '',
  };

  ngOnInit(): void {
    this.accountsService.getAccounts().subscribe({
      next: accounts => this.accounts.set(accounts),
      error: e => this.notificationService.show(e.message, 'error'),
    });
    this.loadTransactions();
  }

  loadTransactions(): void {
    this.loading.set(true);
    const f: TransactionFilters = {};
    if (this.filters.from) f.from = this.filters.from;
    if (this.filters.to) f.to = this.filters.to;
    if (this.filters.category?.trim()) f.category = this.filters.category.trim();

    this.transactionService.getTransactions(f).subscribe({
      next: txs => {
        this.txList.set(txs);
        this.loading.set(false);
      },
      error: e => {
        this.notificationService.show(e.message, 'error');
        this.loading.set(false);
      },
    });
  }

  addTransaction(): void {
    if (!this.form.accountId || !this.form.amount || !this.form.category.trim()) {
      this.notificationService.show('Please fill in all required fields', 'warning');
      return;
    }

    const req: CreateTransactionRequest = {
      accountId: this.form.accountId,
      amount: +this.form.amount,
      type: this.form.type,
      category: this.form.category.trim(),
      description: this.form.description.trim(),
      ...(this.form.dateTime
        ? { dateTime: this.form.dateTime.length === 16 ? this.form.dateTime + ':00' : this.form.dateTime }
        : {}),
    };

    this.transactionService.createTransaction(req).subscribe({
      next: response => {
        this.loadTransactions();

        if (response.warning) {
          this.notificationService.show(response.warning, 'warning', 8000);
        } else {
          this.notificationService.show('Transaction added');
        }

        this.form = {
          ...this.form,
          amount: 0,
          category: '',
          description: '',
          dateTime: '',
        };
      },
      error: e => this.notificationService.show(e.message, 'error'),
    });
  }

  deleteTransaction(id: string): void {
    this.transactionService.deleteTransaction(id).subscribe({
      next: () => {
        this.txList.update(t => t.filter(x => x.id !== id));
        this.notificationService.show('Transaction deleted');
      },
      error: e => this.notificationService.show(e.message, 'error'),
    });
  }

  clearFilters(): void {
    this.filters = {from: '', to: '', category: ''};
    this.loadTransactions();
  }
}
