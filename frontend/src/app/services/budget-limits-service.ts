import {inject, Injectable} from '@angular/core';
import {catchError, Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {handleHttpError} from '../utils/error-handler';
import {BudgetLimitDto} from '../models/budget-limits.models';

@Injectable({
  providedIn: 'root',
})
export class BudgetLimitsService {
  private readonly http = inject(HttpClient);
  private readonly base = 'http://localhost:8080';

  getBudgetLimits(): Observable<BudgetLimitDto[]> {
    return this.http
      .get<BudgetLimitDto[]>(`${this.base}/budget-limits`)
      .pipe(catchError(handleHttpError));
  }

  upsertBudgetLimit(dto: BudgetLimitDto): Observable<BudgetLimitDto> {
    return this.http
      .post<BudgetLimitDto>(`${this.base}/budget-limits`, dto)
      .pipe(catchError(handleHttpError));
  }

  deleteBudgetLimit(category: string): Observable<void> {
    return this.http
      .delete<void>(`${this.base}/budget-limits/${encodeURIComponent(category)}`)
      .pipe(catchError(handleHttpError));
  }
}
