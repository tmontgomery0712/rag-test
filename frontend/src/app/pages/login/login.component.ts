import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ThemeToggleComponent } from '../../components/theme-toggle/theme-toggle.component';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, ThemeToggleComponent],
  template: `
    <div class="min-h-screen flex items-center justify-center">
      <div class="theme-toggle-container">
        <app-theme-toggle></app-theme-toggle>
      </div>
      
      <div class="max-w-md w-full space-y-8 p-8 card">
        <div>
          <h2 class="text-center text-3xl font-bold">Sign in</h2>
        </div>

        @if (isLocked()) {
          <div class="warning-banner">
            <p class="font-medium">Account Locked</p>
            <p class="text-sm">Your account has been locked due to too many failed attempts.
               Please try again in {{ remainingMinutes() }} minutes.</p>
          </div>
        }

        @if (errorMessage()) {
          <div class="error-banner">
            <p>{{ errorMessage() }}</p>
            @if (attemptsRemaining() !== null && attemptsRemaining()! <= 2) {
              <p class="text-sm mt-1 font-medium">
                Warning: {{ attemptsRemaining() }} attempt(s) remaining before account lock.
              </p>
            }
          </div>
        }

        <form [formGroup]="loginForm" (ngSubmit)="onSubmit()" class="mt-8 space-y-6">
          <div class="space-y-4">
            <div>
              <label for="username" class="block text-sm font-medium">Username</label>
              <input
                id="username"
                type="text"
                formControlName="username"
                class="input-field"
                [class.input-error]="loginForm.get('username')?.invalid && loginForm.get('username')?.touched">
            </div>

            <div>
              <label for="password" class="block text-sm font-medium">Password</label>
              <input
                id="password"
                type="password"
                formControlName="password"
                class="input-field"
                [class.input-error]="loginForm.get('password')?.invalid && loginForm.get('password')?.touched">
            </div>
          </div>

          <button
            type="submit"
            [disabled]="loginForm.invalid || isLoading()"
            class="btn-primary w-full">
            @if (isLoading()) {
              Signing in...
            } @else {
              Sign in
            }
          </button>
        </form>

        <div class="text-center">
          <span class="text-sm">Don't have an account? </span>
          <a routerLink="/register" class="text-sm underline cursor-pointer">Register</a>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .theme-toggle-container {
      position: absolute;
      top: 1rem;
      right: 1rem;
    }
    
    .input-field {
      display: block;
      width: 100%;
      padding: 0.5rem 0.75rem;
      border: 1px solid var(--color-border);
      border-radius: 0.25rem;
      background-color: var(--color-surface);
      color: var(--color-text);
      transition: border-color 0.2s ease;
    }
    
    .input-field:focus {
      outline: none;
      border-color: var(--color-primary);
    }
    
    .input-error {
      border-color: var(--color-error);
    }
    
    .btn-primary {
      display: flex;
      justify-content: center;
      padding: 0.75rem 1.5rem;
      border: none;
      border-radius: 0.25rem;
      background-color: var(--color-primary);
      color: white;
      font-weight: 500;
      cursor: pointer;
      transition: background-color 0.2s ease;
    }
    
    .btn-primary:hover:not(:disabled) {
      background-color: var(--color-primary-hover);
    }
    
    .btn-primary:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
    
    .warning-banner {
      padding: 1rem;
      border-radius: 0.25rem;
      background-color: rgba(255, 167, 38, 0.1);
      border: 1px solid var(--color-warning);
      color: var(--color-warning);
    }
    
    .error-banner {
      padding: 1rem;
      border-radius: 0.25rem;
      background-color: rgba(239, 83, 80, 0.1);
      border: 1px solid var(--color-error);
      color: var(--color-error);
    }
  `]
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private authService = inject(AuthService);

  loginForm = this.fb.group({
    username: ['', [Validators.required]],
    password: ['', [Validators.required]]
  });

  isLoading = signal(false);
  errorMessage = signal<string | null>(null);
  attemptsRemaining = signal<number | null>(null);
  isLocked = signal(false);
  lockedUntil = signal<Date | null>(null);

  constructor() {
    this.route.queryParams.subscribe(params => {
      if (params['locked'] === 'true') {
        this.isLocked.set(true);
        if (params['until']) {
          this.lockedUntil.set(new Date(params['until']));
        }
      }
    });
  }

  remainingMinutes(): number {
    const until = this.lockedUntil();
    if (!until) return 30;
    const diff = until.getTime() - Date.now();
    return Math.max(0, Math.ceil(diff / 60000));
  }

  onSubmit(): void {
    if (this.loginForm.invalid) return;

    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.attemptsRemaining.set(null);

    const { username, password } = this.loginForm.value;

    this.authService.login(username!, password!).subscribe({
      next: () => {
        this.router.navigate(['/streaks']);
      },
      error: (error) => {
        this.isLoading.set(false);

        if (error.status === 403 && error.error?.error === 'ACCOUNT_LOCKED') {
          this.isLocked.set(true);
          if (error.error?.lockedUntil) {
            this.lockedUntil.set(new Date(error.error.lockedUntil));
          }
          this.errorMessage.set('Account locked due to too many failed attempts.');
        } else if (error.status === 401) {
          this.errorMessage.set(error.error?.message || 'Invalid username or password');
          if (error.error?.attemptsRemaining !== undefined) {
            this.attemptsRemaining.set(error.error.attemptsRemaining);
          }
        } else {
          this.errorMessage.set('An error occurred. Please try again.');
        }
      }
    });
  }
}
