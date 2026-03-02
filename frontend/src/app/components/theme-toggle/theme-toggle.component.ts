import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ThemeService } from '../../services/theme.service';

@Component({
  selector: 'app-theme-toggle',
  standalone: true,
  imports: [CommonModule],
  template: `
    <button 
      (click)="themeService.toggleTheme()"
      class="theme-toggle"
      [attr.aria-label]="themeService.theme() === 'dark' ? 'Switch to light mode' : 'Switch to dark mode'"
      [attr.title]="themeService.theme() === 'dark' ? 'Switch to light mode' : 'Switch to dark mode'">
      @if (themeService.theme() === 'dark') {
        <span class="icon" aria-hidden="true">☀️</span>
      } @else {
        <span class="icon" aria-hidden="true">🌙</span>
      }
    </button>
  `,
  styles: [`
    .theme-toggle {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 44px;
      height: 44px;
      padding: 0;
      background: transparent;
      border: none;
      border-radius: 50%;
      cursor: pointer;
      transition: background-color 0.2s ease;
    }
    
    .theme-toggle:hover {
      background-color: var(--color-surface-hover);
    }
    
    .theme-toggle .icon {
      font-size: 1.5rem;
      line-height: 1;
    }
  `]
})
export class ThemeToggleComponent {
  themeService = inject(ThemeService);
}
