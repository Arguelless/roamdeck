import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { EMPTY, Observable, catchError, finalize, map, switchMap } from 'rxjs';

import { AuthApiService } from '../data-access/auth-api.service';
import { SessionService } from '../data-access/session.service';
import { RegisterRequest } from '../models/register-request.model';

const MINIMUM_PASSWORD_LENGTH = 8;

@Component({
  selector: 'app-register-page',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register-page.html',
  styleUrl: './register-page.scss'
})
export class RegisterPage {
  private readonly formBuilder = inject(FormBuilder);
  private readonly authApi = inject(AuthApiService);
  private readonly session = inject(SessionService);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly minimumPasswordLength = MINIMUM_PASSWORD_LENGTH;

  protected readonly form = this.formBuilder.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(MINIMUM_PASSWORD_LENGTH)]]
  });

  protected onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.error.set(null);

    const credentials = this.form.getRawValue();

    this.authApi
      .register(credentials)
      .pipe(
        switchMap(() => this.logInTheNewAccount(credentials)),
        finalize(() => this.loading.set(false))
      )
      .subscribe({
        next: (token) => {
          this.session.start(token);
          this.router.navigateByUrl('/itinerary');
        },
        error: (error: unknown) => this.error.set(messageFor(error))
      });
  }

  private logInTheNewAccount(credentials: RegisterRequest): Observable<string> {
    return this.authApi.login(credentials).pipe(
      map((response) => response.token),
      catchError(() => this.askForAManualLogin())
    );
  }

  private askForAManualLogin(): Observable<never> {
    this.router.navigate(['/login'], { queryParams: { reason: 'registered' } });

    return EMPTY;
  }
}

function messageFor(error: unknown): string {
  if (!(error instanceof HttpErrorResponse)) {
    return 'No se pudo crear la cuenta. Inténtalo de nuevo.';
  }

  if (error.status === 409) {
    return 'Ya existe una cuenta con ese email.';
  }

  if (error.status === 400) {
    return 'Revisa el email y la contraseña.';
  }

  return 'No se pudo crear la cuenta. Inténtalo de nuevo.';
}
