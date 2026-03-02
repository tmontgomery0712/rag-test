import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, catchError, of, throwError, switchMap, map } from 'rxjs';
import { Router } from '@angular/router';
import { environment } from '../../environments/environment';

export interface User {
  id: number;
  username: string;
}

export interface AuthResponse {
  accessToken: string;
  user: User;
}

export interface RefreshResponse {
  accessToken: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  private readonly API_URL = environment.apiUrl + '/api/auth';

  private currentUserSignal = signal<User | null>(null);
  private accessTokenSignal = signal<string | null>(null);

  readonly currentUser = this.currentUserSignal.asReadonly();
  readonly accessToken = this.accessTokenSignal.asReadonly();
  readonly isAuthenticated = signal<boolean>(false);

  checkAuth(): Observable<boolean> {
    return this.refresh().pipe(
      tap(() => console.log('Auto-refresh successful on app load')),
      map(() => true),
      catchError(() => {
        console.log('No valid session, user needs to login');
        this.isAuthenticated.set(false);
        return of(false);
      })
    );
  }

  login(username: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/login`, { username, password }).pipe(
      tap(response => this.handleAuthSuccess(response)),
      catchError(error => {
        return throwError(() => error);
      })
    );
  }

  register(username: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/register`, { username, password }).pipe(
      tap(response => this.handleAuthSuccess(response)),
      catchError(error => {
        return throwError(() => error);
      })
    );
  }

  refresh(): Observable<RefreshResponse> {
    return this.http.post<RefreshResponse>(`${this.API_URL}/refresh`, {}).pipe(
      tap(response => {
        this.accessTokenSignal.set(response.accessToken);
        this.isAuthenticated.set(true);
      }),
      catchError(error => {
        console.error('AuthService: Refresh failed', error);
        this.isAuthenticated.set(false);
        return throwError(() => error);
      })
    );
  }

  logout(): void {
    this.http.post(`${this.API_URL}/logout`, {}).subscribe({
      complete: () => this.clearAuth(),
      error: () => this.clearAuth()
    });
  }

  clearAuth(): void {
    this.accessTokenSignal.set(null);
    this.currentUserSignal.set(null);
    this.isAuthenticated.set(false);
    this.router.navigate(['/login']);
  }

  private handleAuthSuccess(response: AuthResponse): void {
    this.accessTokenSignal.set(response.accessToken);
    this.currentUserSignal.set(response.user);
    this.isAuthenticated.set(true);
  }

  getAccessToken(): string | null {
    return this.accessTokenSignal();
  }

  private fetchCurrentUser(): Observable<User | null> {
    return this.http.get<User>(`${environment.apiUrl}/api/auth/me`).pipe(
      tap(user => {
        this.currentUserSignal.set(user);
        this.isAuthenticated.set(true);
      }),
      catchError(() => {
        this.currentUserSignal.set(null);
        return of(null);
      })
    );
  }
}
