import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {
  CreateTransactionRequest,
  Transaction,
  TransactionCreatedResponse,
  TransactionFilters
} from '../models/transactions.models';
import {catchError, Observable} from 'rxjs';
import {handleHttpError} from '../utils/error-handler';

@Injectable({
  providedIn: 'root',
})
export class TransactionsService {
  private readonly http = inject(HttpClient);
  private readonly base = 'http://localhost:8080';

  getTransactions(filters?: TransactionFilters): Observable<Transaction[]> {
    let params = new HttpParams();
    if (filters?.from) params = params.set('from', filters.from);
    if (filters?.to) params = params.set('to', filters.to);
    if (filters?.category) params = params.set('category', filters.category);
    return this.http
      .get<Transaction[]>(`${this.base}/transactions`, {params})
      .pipe(catchError(handleHttpError));
  }

  createTransaction(req: CreateTransactionRequest): Observable<TransactionCreatedResponse> {
    return this.http
      .post<TransactionCreatedResponse>(`${this.base}/transactions`, req)
      .pipe(catchError(handleHttpError));
  }

  deleteTransaction(id: string): Observable<unknown> {
    return this.http
      .delete<unknown>(`${this.base}/transactions/${id}`)
      .pipe(catchError(handleHttpError));
  }
}
