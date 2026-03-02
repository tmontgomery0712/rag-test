import { Injectable, signal } from '@angular/core';

export interface AppError {
  code: number;
  error?: string;
  message: string;
  details?: unknown;
  lockedUntil?: string;
  attemptsRemaining?: number;
}

@Injectable({
  providedIn: 'root'
})
export class ErrorService {
  private currentErrorSignal = signal<AppError | null>(null);

  readonly currentError = this.currentErrorSignal.asReadonly();

  setError(error: AppError): void {
    this.currentErrorSignal.set(error);
  }

  clearError(): void {
    this.currentErrorSignal.set(null);
  }
}
