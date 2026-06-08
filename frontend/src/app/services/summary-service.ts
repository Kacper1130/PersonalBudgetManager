import {inject, Injectable} from '@angular/core';
import {catchError, Observable} from 'rxjs';
import {SummaryResponse} from '../models/summary.models';
import {HttpClient} from '@angular/common/http';
import {handleHttpError} from '../utils/error-handler';

@Injectable({
  providedIn: 'root',
})
export class SummaryService {
  private readonly http = inject(HttpClient);
  private readonly base = 'http://localhost:8080/api';

  getSummary(): Observable<SummaryResponse> {
    return this.http
      .get<SummaryResponse>(`${this.base}/summary`)
      .pipe(catchError(handleHttpError));
  }
}
