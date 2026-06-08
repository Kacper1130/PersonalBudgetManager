import { HttpErrorResponse } from '@angular/common/http';
import { throwError, Observable } from 'rxjs';
import { ApiError } from '../models/api-error.models';

export function handleHttpError(err: HttpErrorResponse): Observable<never> {
  const apiError = err.error as ApiError;
  const message = apiError?.message ?? apiError?.error ?? `HTTP ${err.status}`;
  return throwError(() => new Error(message));
}
