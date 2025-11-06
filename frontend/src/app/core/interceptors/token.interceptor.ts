import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

/**
 * Intercepts HTTP requests to add JWT token in the Authorization header,
 * except for login and register endpoints.
 * Handles 401 Unauthorized errors by clearing stored tokens and redirecting to login.
 */
export const tokenInterceptor: HttpInterceptorFn = (req, next) => {
  const router: Router = inject(Router);

  if (!req.url.includes('/login') && !req.url.includes('/register')) {
    const token = sessionStorage.getItem('token');
    if (token) {
      req = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`,
        },
      });
    }
  }

  return next(req).pipe(
    catchError((err) => {
      if (err instanceof HttpErrorResponse && err.status === 401) {
        sessionStorage.removeItem('token');
        sessionStorage.removeItem('username');
        router.navigate(['/login']);
      }
      return throwError(() => err);
    }),
  );
};
