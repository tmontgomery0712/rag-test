import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ThemeToggleComponent } from '../../components/theme-toggle/theme-toggle.component';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, ThemeToggleComponent],
  template: `
    <div class="min-h-screen flex items-center justify-center">
      <div class="theme-toggle-container">
        <app-theme-toggle></app-theme-toggle>
      </div>
      
      <div class="max-w-md w-full space-y-8 p-8 card">
        <div>
          <h2 class="text-center text-3xl font-bold">Create Account</h2>
        </div>

        @if (errorMessage()) {
          <div class="error-banner">
            <p>{{ errorMessage() }}</p>
          </div>
        }

        <form [formGroup]="registerForm" (ngSubmit)="onSubmit()" class="mt-8 space-y-6">
          <div class="space-y-4">
            <div>
              <label for="username" class="block text-sm font-medium">Username</label>
              <input 
                id="username" 
                type="text" 
                formControlName="username"
                class="input-field"
                [class.input-error]="registerForm.get('username')?.invalid && registerForm.get('username')?.touched">
              @if (registerForm.get('username')?.hasError('required') && registerForm.get('username')?.touched) {
                <p class="mt-1 text-sm error-text">Username is required</p>
              }
              @if (registerForm.get('username')?.hasError('minlength') && registerForm.get('username')?.touched) {
                <p class="mt-1 text-sm error-text">Username must be at least 3 characters</p>
              }
            </div>

            <div>
              <label for="password" class="block text-sm font-medium">Password</label>
              <input 
                id="password" 
                type="password" 
                formControlName="password"
                class="input-field"
                [class.input-error]="registerForm.get('password')?.invalid && registerForm.get('password')?.touched">
              @if (registerForm.get('password')?.hasError('required') && registerForm.get('password')?.touched) {
                <p class="mt-1 text-sm error-text">Password is required</p>
              }
              @if (registerForm.get('password')?.hasError('minlength') && registerForm.get('password')?.touched) {
                <p class="mt-1 text-sm error-text">Password must be at least 8 characters</p>
              }
            </div>

            <div>
              <label for="confirmPassword" class="block text-sm font-medium">Confirm Password</label>
              <input 
                id="confirmPassword" 
                type="password" 
                formControlName="confirmPassword"
                class="input-field"
                [class.input-error]="registerForm.hasError('passwordMismatch') && registerForm.get('confirmPassword')?.touched">
              @if (registerForm.hasError('passwordMismatch') && registerForm.get('confirmPassword')?.touched) {
                <p class="mt-1 text-sm error-text">Passwords do not match</p>
              }
            </div>
          </div>

          <button 
            type="submit" 
            [disabled]="registerForm.invalid || isLoading()"
            class="btn-primary w-full">
            @if (isLoading()) {
              Creating account...
            } @else {
              Register
            }
          </button>
        </form>

        <div class="text-center">
          <span class="text-sm">Already have an account? </span>
          <a routerLink="/login" class="text-sm underline cursor-pointer">Sign in</a>
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
    
    .error-text {
      color: var(--color-error);
    }
    
    .btn-primary {
      display: flex;
      justify-content: center;
      width: 100%;
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
    
    .error-banner {
      padding: 1rem;
      border-radius: 0.25rem;
      background-color: rgba(239, 83, 80, 0.1);
      border: 1px solid var(--color-error);
      color: var(--color-error);
    }
  `]
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private authService = inject(AuthService);

  registerForm = this.fb.group({
    username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
    password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(100)]],
    confirmPassword: ['', [Validators.required]]
  }, { 
    validators: this.passwordMatchValidator 
  });

  isLoading = signal(false);
  errorMessage = signal<string | null>(null);

  passwordMatchValidator(form: import('@angular/forms').AbstractControl) {
    const password = form.get('password');
    const confirmPassword = form.get('confirmPassword');
    if (password && confirmPassword && password.value !== confirmPassword.value) {
      return { passwordMismatch: true };
    }
    return null;
  }

  onSubmit(): void {
    if (this.registerForm.invalid) return;

    this.isLoading.set(true);
    this.errorMessage.set(null);

    const { username, password } = this.registerForm.value;

    this.authService.register(username!, password!).subscribe({
      next: () => {
        this.router.navigate(['/streaks']);
      },
      error: (error) => {
        this.isLoading.set(false);
        
        if (error.status === 409) {
          this.errorMessage.set('Username already taken');
        } else if (error.status === 0) {
          this.errorMessage.set('Unable to connect to server');
        } else {
          this.errorMessage.set('Registration failed. Please try again.');
        }
      }
    });
  }
}
