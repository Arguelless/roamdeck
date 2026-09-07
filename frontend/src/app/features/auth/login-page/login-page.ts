import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { AuthApiService } from '../data-access/auth-api.service';
import { SessionService } from '../data-access/session.service';

const DEFAULT_REDIRECT = '/itinerary';

@Component({
  selector: 'app-login-page',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login-page.html',
  styleUrl: './login-page.scss'
})
export class LoginPage {
  private readonly formBuilder = inject(FormBuilder);
  private readonly authApi = inject(AuthApiService);
  private readonly session = inject(SessionService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  private readonly reason = this.route.snapshot.queryParamMap.get('reason');

  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly sessionExpired = this.reason === 'expired';
  protected readonly accountCreated = this.reason === 'registered';

  protected readonly form = this.formBuilder.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required]
  });

  protected onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.error.set(null);

    this.authApi.login(this.form.getRawValue()).subscribe({
      next: (response) => {
        this.session.start(response.token);
        this.loading.set(false);
        this.router.navigateByUrl(this.redirectTarget());
      },
      error: (error: unknown) => {
        this.error.set(messageFor(error));
        this.loading.set(false);
      }
    });
  }

  private redirectTarget(): string {
    const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');

    return isInternalUrl(returnUrl) ? returnUrl : DEFAULT_REDIRECT;
  }
}

function isInternalUrl(url: string | null): url is string {
  return url !== null && url.startsWith('/') && !url.startsWith('//');
}

function messageFor(error: unknown): string {
  if (error instanceof HttpErrorResponse && error.status === 401) {
    return 'Email o contraseña incorrectos.';
  }

  return 'No se pudo iniciar sesión. Inténtalo de nuevo.';
}
