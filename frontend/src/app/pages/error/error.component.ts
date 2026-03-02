import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ErrorService } from '../../services/error.service';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-error',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="min-h-screen flex items-center justify-center bg-gray-50">
      <div class="max-w-md w-full text-center p-8">
        <div class="text-6xl mb-4">⚠️</div>
        <h1 class="text-4xl font-bold text-gray-900 mb-4">
          @if (error()?.code) {
            Error {{ error()?.code }}
          } @else {
            Something Went Wrong
          }
        </h1>
        <p class="text-gray-600 mb-4">
          {{ error()?.message || 'An unexpected error occurred. Please try again.' }}
        </p>
        
        @if (environment.showDetailedErrors && error()?.details) {
          <div class="mt-4 p-4 bg-gray-100 rounded text-left text-sm text-gray-700 overflow-auto">
            <pre>{{ error()?.details | json }}</pre>
          </div>
        }

        <div class="mt-8 space-x-4">
          <button 
            (click)="goBack()"
            class="inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50">
            Go Back
          </button>
          <a 
            routerLink="/streaks"
            class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700">
            Go to Dashboard
          </a>
        </div>
      </div>
    </div>
  `
})
export class ErrorComponent {
  private errorService = inject(ErrorService);
  
  error = this.errorService.currentError;
  environment = environment;

  goBack(): void {
    window.history.back();
  }
}
