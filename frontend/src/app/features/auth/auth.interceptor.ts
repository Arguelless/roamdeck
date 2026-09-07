import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

import { SessionService } from './data-access/session.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const session = inject(SessionService);
  const router = inject(Router);

  const token = session.token();

  const outgoingRequest = token
    ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : request;

  return next(outgoingRequest).pipe(
    catchError((error: unknown) => {
      if (isSessionRejection(error) && token) {
        session.end();
        redirectToLogin(router);
      }

      return throwError(() => error);
    })
  );
};

function isSessionRejection(error: unknown): boolean {
  return error instanceof HttpErrorResponse && error.status === 401;
}

function redirectToLogin(router: Router): void {
  const currentUrl = router.url;

  if (currentUrl.startsWith('/login')) {
    return;
  }

  router.navigate(['/login'], {
    queryParams: { returnUrl: currentUrl, reason: 'expired' }
  });
}
