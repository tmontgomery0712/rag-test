import { inject } from '@angular/core';
import { HttpInterceptorFn, HttpErrorResponse, HttpRequest, HttpHandlerFn, HttpEvent } from '@angular/common/http';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';
import { ErrorService } from './error.service';
import { environment } from '../../environments/environment';
import { catchError, throwError, Observable } from 'rxjs';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const errorService = inject(ErrorService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        // Let the component handle 401 - don't redirect here
        return throwError(() => error);
      }

      if (error.status === 403) {
        const errorBody = error.error;
        if (errorBody?.error === 'ACCOUNT_LOCKED') {
          router.navigate(['/login'], {
            queryParams: {
              locked: true,
              until: errorBody.lockedUntil
            }
          });
        } else {
          errorService.setError({
            code: 403,
            error: errorBody?.error,
            message: 'You do not have permission to access this resource',
            details: environment.showDetailedErrors ? errorBody : undefined
          });
          router.navigate(['/forbidden']);
        }
        return throwError(() => error);
      }

      if (error.status === 0) {
        errorService.setError({
          code: 0,
          message: 'Unable to connect to the server. Please check your connection.',
          details: environment.showDetailedErrors ? error.message : undefined
        });
        router.navigate(['/error']);
        return throwError(() => error);
      }

      if (error.status >= 400 && error.status < 600) {
        const errorBody = error.error;
        errorService.setError({
          code: error.status,
          error: errorBody?.error,
          message: environment.showDetailedErrors
            ? errorBody?.message || error.message
            : 'An unexpected error occurred. Please try again.',
          details: environment.showDetailedErrors ? errorBody : undefined
        });
        router.navigate(['/error']);
        return throwError(() => error);
      }

      return throwError(() => error);
    })
  );
};

export const authInterceptor: HttpInterceptorFn = (req: HttpRequest<unknown>, next: HttpHandlerFn): Observable<HttpEvent<unknown>> => {
  const authService = inject(AuthService);
  const token = authService.getAccessToken();

  const authReq = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(authReq);
};
