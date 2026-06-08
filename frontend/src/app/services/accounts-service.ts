import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {catchError, Observable} from 'rxjs';
import {AccountDetailsData, AccountSummary, CreateAccountRequest} from '../models/accounts.models';
import {handleHttpError} from '../utils/error-handler';
import {Transaction} from '../models/transactions.models';

@Injectable({
  providedIn: 'root',
})
export class AccountsService {
  private readonly http = inject(HttpClient);
  private readonly base = 'http://localhost:8080/api';

  getAccounts(): Observable<AccountSummary[]> {
    return this.http
      .get<AccountSummary[]>(`${this.base}/accounts`)
      .pipe(catchError(handleHttpError));
  }

  getAccount(id: string): Observable<AccountDetailsData> {
    return this.http
      .get<AccountDetailsData>(`${this.base}/accounts/${id}`)
      .pipe(catchError(handleHttpError));
  }

  createAccount(req: CreateAccountRequest): Observable<AccountDetailsData> {
    return this.http
      .post<AccountDetailsData>(`${this.base}/accounts`, req)
      .pipe(catchError(handleHttpError));
  }

  deleteAccount(id: string): Observable<void> {
    return this.http
      .delete<void>(`${this.base}/accounts/${id}`)
      .pipe(catchError(handleHttpError));
  }

  getAccountTransactions(id: string): Observable<Transaction[]> {
    return this.http
      .get<Transaction[]>(`${this.base}/accounts/${id}/transactions`)
      .pipe(catchError(handleHttpError));
  }

  exportAccountCsv(id: string, accountName: string): void {
    this.http
      .get(`${this.base}/accounts/${id}/transactions/export`, {
        responseType: 'blob',
        headers: new HttpHeaders({ Accept: 'text/csv' }),
      })
      .pipe(catchError(handleHttpError))
      .subscribe(blob => {
        const url  = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href     = url;
        link.download = `${accountName.replace(/\s+/g, '_')}_transactions.csv`;
        link.click();
        URL.revokeObjectURL(url);
      });
  }
}
