import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';

export type Theme = 'dark' | 'light';

export interface UserPreferences {
  theme: Theme;
}

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  private http = inject(HttpClient);
  private authService = inject(AuthService);
  private readonly API_URL = environment.apiUrl + '/api/user';

  private readonly themeSignal = signal<Theme>('dark');
  readonly theme = this.themeSignal.asReadonly();

  constructor() {
    this.themeSignal.set('dark');
    this.applyTheme('dark');

    this.loadFromBackend();
  }

  private loadFromBackend(): void {
    if (!this.authService.isAuthenticated()) {
      return;
    }

    this.http.get<UserPreferences>(`${this.API_URL}/preferences`).subscribe({
      next: (prefs) => {
        if (prefs?.theme) {
          this.themeSignal.set(prefs.theme);
          this.applyTheme(prefs.theme);
        }
      },
      error: () => {
        // Fall back to dark theme
      }
    });
  }

  private applyTheme(theme: Theme): void {
    document.documentElement.setAttribute('data-theme', theme);
    if (theme === 'dark') {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }

  toggleTheme(): void {
    const newTheme = this.themeSignal() === 'dark' ? 'light' : 'dark';
    this.setTheme(newTheme);
  }

  setTheme(theme: Theme): void {
    this.themeSignal.set(theme);
    this.applyTheme(theme);

    if (!this.authService.isAuthenticated()) {
      return;
    }

    this.http.post<UserPreferences>(`${this.API_URL}/preferences`, { theme }).subscribe({
      error: (error: Error) => {
        // Silently ignore - preference already applied locally
      }
    });
  }

  getTheme(): Theme {
    return this.themeSignal();
  }
}
