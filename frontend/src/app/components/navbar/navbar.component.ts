import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { AuthService } from '../../services/auth.service';
import { ThemeService } from '../../services/theme.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule
  ],
  template: `
    <mat-toolbar class="navbar">
      <span class="navbar-title">
        <a routerLink="/streaks" class="title-link" routerLinkActive="active-link" [routerLinkActiveOptions]="{exact: true}">Streaks</a>
      </span>
      
      <span class="spacer"></span>
      
      <!-- Desktop buttons -->
      <div class="desktop-actions">
        <button mat-button routerLink="/streaks" class="nav-button" routerLinkActive="active-link">
          <mat-icon>local_fire_department</mat-icon>
          Streaks
        </button>
        <button mat-button routerLink="/settings" class="nav-button" routerLinkActive="active-link">
          <mat-icon>settings</mat-icon>
          Settings
        </button>
        <button mat-button (click)="logout()" class="nav-button logout-button">
          <mat-icon>logout</mat-icon>
          Logout
        </button>
      </div>
      
      <!-- Mobile hamburger menu -->
      <button mat-icon-button [matMenuTriggerFor]="menu" class="mobile-menu-button">
        <mat-icon>menu</mat-icon>
      </button>
      
      <mat-menu #menu="matMenu">
        <button mat-menu-item routerLink="/streaks">
          <mat-icon>local_fire_department</mat-icon>
          <span>Streaks</span>
        </button>
        <button mat-menu-item routerLink="/settings">
          <mat-icon>settings</mat-icon>
          <span>Settings</span>
        </button>
        <button mat-menu-item (click)="logout()">
          <mat-icon>logout</mat-icon>
          <span>Logout</span>
        </button>
      </mat-menu>
    </mat-toolbar>
  `,
  styles: [`
    .navbar {
      background-color: var(--color-surface);
      color: var(--color-text);
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      z-index: 1000;
      display: flex;
      align-items: center;
      padding: 0 1rem;
      height: 64px;
    }
    
    .navbar-title {
      font-size: 1.25rem;
      font-weight: 500;
    }
    
    .title-link {
      color: var(--color-text);
      text-decoration: none;
      padding: 0.5rem;
      border-radius: 4px;
      transition: all 0.2s ease;
    }
    
    .title-link:hover {
      color: var(--color-primary);
    }
    
    .spacer {
      flex: 1;
    }
    
    .desktop-actions {
      display: flex;
      align-items: center;
      gap: 0.25rem;
    }
    
    .nav-button {
      color: var(--color-text);
      display: flex;
      align-items: center;
      gap: 0.25rem;
      transition: all 0.2s ease;
    }
    
    .nav-button mat-icon {
      font-size: 1.25rem;
      width: 1.25rem;
      height: 1.25rem;
    }
    
    .nav-button.active-link {
      color: var(--color-primary) !important;
      background-color: var(--color-surface-hover);
      border-radius: 4px;
    }
    
    .logout-button:hover {
      color: var(--color-error);
    }
    
    .mobile-menu-button {
      display: none;
      color: var(--color-text);
    }
    
    @media (max-width: 768px) {
      .desktop-actions {
        display: none;
      }
      
      .mobile-menu-button {
        display: flex;
      }
    }
  `]
})
export class NavbarComponent {
  authService = inject(AuthService);
  themeService = inject(ThemeService);
  
  logout(): void {
    this.authService.logout();
  }
}
