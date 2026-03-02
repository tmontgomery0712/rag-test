import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { ThemeService } from '../../services/theme.service';
import { AuthService } from '../../services/auth.service';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSlideToggleModule
  ],
  template: `
    <div class="page-container">
      <h1 class="page-title">Settings</h1>
      
      <!-- Username Section -->
      <mat-card class="settings-card">
        <mat-card-header>
          <mat-card-title>Username</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <p class="current-value">Current: <strong>{{ currentUsername() }}</strong></p>
          <mat-form-field class="w-full">
            <mat-label>New Username</mat-label>
            <input matInput [(ngModel)]="newUsername" placeholder="Enter new username">
          </mat-form-field>
          <button mat-raised-button color="primary" (click)="updateUsername()" [disabled]="!newUsername || isLoading()">
            Update Username
          </button>
        </mat-card-content>
      </mat-card>
      
      <!-- Password Section -->
      <mat-card class="settings-card">
        <mat-card-header>
          <mat-card-title>Change Password</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <mat-form-field class="w-full">
            <mat-label>Current Password</mat-label>
            <input matInput type="password" [(ngModel)]="currentPassword" placeholder="Enter current password">
          </mat-form-field>
          <mat-form-field class="w-full">
            <mat-label>New Password</mat-label>
            <input matInput type="password" [(ngModel)]="newPassword" placeholder="Enter new password">
          </mat-form-field>
          <mat-form-field class="w-full">
            <mat-label>Confirm New Password</mat-label>
            <input matInput type="password" [(ngModel)]="confirmPassword" placeholder="Confirm new password">
          </mat-form-field>
          <button mat-raised-button color="primary" (click)="changePassword()" [disabled]="!canChangePassword() || isLoading()">
            Change Password
          </button>
        </mat-card-content>
      </mat-card>
      
      <!-- Theme Section -->
      <mat-card class="settings-card">
        <mat-card-header>
          <mat-card-title>Appearance</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <div class="theme-toggle-row">
            <span>Dark Mode</span>
            <mat-slide-toggle 
              [checked]="themeService.theme() === 'dark'"
              (change)="themeService.toggleTheme()">
            </mat-slide-toggle>
          </div>
        </mat-card-content>
      </mat-card>
      
      @if (message()) {
        <div class="message" [class.error]="isError()">{{ message() }}</div>
      }
    </div>
  `,
  styles: [`
    .page-container {
      width: 100%;
      max-width: 90%;
      margin: 0 auto;
      padding: 1rem;
    }
    
    @media (min-width: 768px) {
      .page-container {
        max-width: 60%;
        padding: 1.5rem;
      }
    }
    
    .page-title {
      margin-bottom: 1.5rem;
      color: var(--color-text);
    }
    
    .settings-card {
      margin-bottom: 1.5rem;
      background-color: var(--color-surface) !important;
    }
    
    .current-value {
      color: var(--color-text-secondary);
      margin-bottom: 1rem;
    }
    
    .w-full {
      width: 100%;
      margin-bottom: 0.5rem;
    }
    
    .theme-toggle-row {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0.5rem 0;
    }
    
    .message {
      padding: 1rem;
      border-radius: 4px;
      margin-top: 1rem;
      background-color: var(--color-surface);
      color: var(--color-text);
    }
    
    .message.error {
      color: var(--color-error);
      border: 1px solid var(--color-error);
    }
  `]
})
export class SettingsComponent {
  private http = inject(HttpClient);
  authService = inject(AuthService);
  themeService = inject(ThemeService);
  
  currentUsername = signal('');
  newUsername = '';
  currentPassword = '';
  newPassword = '';
  confirmPassword = '';
  isLoading = signal(false);
  message = signal('');
  isError = signal(false);
  
  private readonly API_URL = environment.apiUrl + '/api';
  
  constructor() {
    // Get current username from auth service
    const user = this.authService.currentUser();
    if (user) {
      this.currentUsername.set(user.username);
    }
  }
  
  canChangePassword(): boolean {
    return !!(this.currentPassword && this.newPassword && this.confirmPassword);
  }
  
  updateUsername(): void {
    if (!this.newUsername.trim()) return;
    
    this.isLoading.set(true);
    this.message.set('');
    
    this.http.put<{ message: string }>(`${this.API_URL}/user/username`, { username: this.newUsername })
      .subscribe({
        next: (response) => {
          this.message.set(response.message);
          this.isError.set(false);
          this.currentUsername.set(this.newUsername);
          this.newUsername = '';
          this.isLoading.set(false);
        },
        error: (err) => {
          this.message.set(err.error?.error || 'Failed to update username');
          this.isError.set(true);
          this.isLoading.set(false);
        }
      });
  }
  
  changePassword(): void {
    if (!this.canChangePassword()) return;
    
    if (this.newPassword !== this.confirmPassword) {
      this.message.set('Passwords do not match');
      this.isError.set(true);
      return;
    }
    
    this.isLoading.set(true);
    this.message.set('');
    
    this.http.post<{ message: string }>(`${this.API_URL}/user/change-password`, {
      currentPassword: this.currentPassword,
      newPassword: this.newPassword
    }).subscribe({
      next: (response) => {
        this.message.set(response.message);
        this.isError.set(false);
        this.currentPassword = '';
        this.newPassword = '';
        this.confirmPassword = '';
        this.isLoading.set(false);
      },
      error: (err) => {
        this.message.set(err.error?.error || 'Failed to change password');
        this.isError.set(true);
        this.isLoading.set(false);
      }
    });
  }
}
