import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-forbidden',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="min-h-screen flex items-center justify-center bg-gray-50">
      <div class="max-w-md w-full text-center p-8">
        <div class="text-6xl mb-4">🚫</div>
        <h1 class="text-4xl font-bold text-gray-900 mb-4">Access Denied</h1>
        <p class="text-gray-600 mb-8">
          You don't have permission to access this resource.
        </p>
        <a 
          routerLink="/streaks"
          class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700">
          Go to Dashboard
        </a>
      </div>
    </div>
  `
})
export class ForbiddenComponent {}
