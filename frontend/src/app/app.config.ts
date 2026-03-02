import { ApplicationConfig, APP_INITIALIZER } from '@angular/core';
import { provideRouter, Routes } from '@angular/router';
import { provideHttpClient, withInterceptors, HTTP_INTERCEPTORS } from '@angular/common/http';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { authInterceptor, errorInterceptor } from './services/error.interceptor';
import { AuthService } from './services/auth.service';
import { Observable } from 'rxjs';

import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideAnimationsAsync(),
    provideHttpClient(
      withInterceptors([authInterceptor, errorInterceptor])
    ),
    {
      provide: APP_INITIALIZER,
      useFactory: (authService: AuthService): (() => Observable<boolean>) => {
        return () => authService.checkAuth();
      },
      deps: [AuthService],
      multi: true
    }
  ]
};
