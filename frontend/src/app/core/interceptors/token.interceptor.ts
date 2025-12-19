import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';

import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from '../../services/auth.service';

let isRefreshing = false;

export const tokenInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);

  if (
    !req.url.includes('/login') &&
    !req.url.includes('/register') &&
    !req.url.includes('/refresh')
  ) {
    const token = authService.getToken();
    if (token) {
      req = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`,
        },
      });
    }
  }

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      // Only handle 401
      if (error.status !== 401) {
        return throwError(() => error);
      }

      // If refresh itself fails → logout
      if (req.url.includes('/refresh')) {
        authService.logout();
        return throwError(() => error);
      }

      // Prevent multiple refresh calls
      if (isRefreshing) {
        authService.logout();
        return throwError(() => error);
      }

      isRefreshing = true;

      // 🔁 Attempt refresh via AuthService
      return authService.refresh().pipe(
        switchMap((res) => {
          isRefreshing = false;

          // Save new access token
          authService.saveToken(res.token);

          // Retry original request with new token
          const retryReq = req.clone({
            setHeaders: {
              Authorization: `Bearer ${res.token}`,
            },
          });

          return next(retryReq);
        }),
        catchError((refreshErr) => {
          isRefreshing = false;
          authService.logout();
          return throwError(() => refreshErr);
        }),
      );
    }),
  );
};
