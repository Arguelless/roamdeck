import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { vi } from 'vitest';

import { SessionService } from '../data-access/session.service';
import { RegisterPage } from './register-page';

describe('RegisterPage', () => {
  let fixture: ComponentFixture<RegisterPage>;
  let httpController: HttpTestingController;
  let session: SessionService;
  let router: Router;

  beforeEach(async () => {
    localStorage.clear();

    await TestBed.configureTestingModule({
      imports: [RegisterPage],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterPage);
    httpController = TestBed.inject(HttpTestingController);
    session = TestBed.inject(SessionService);
    router = TestBed.inject(Router);

    vi.spyOn(router, 'navigate').mockResolvedValue(true);
    vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);

    fixture.detectChanges();
  });

  afterEach(() => {
    httpController.verify();
  });

  it('opens a session for the new account and goes to the itinerary', () => {
    submitValidDetails();

    httpController.expectOne('/api/users/register').flush('User registered successfully');
    httpController.expectOne('/api/auth/login').flush({ token: aToken() });

    expect(session.isLoggedIn()).toBe(true);
    expect(router.navigateByUrl).toHaveBeenCalledWith('/itinerary');
  });

  it('reports an email that is already taken', () => {
    submitValidDetails();

    httpController
      .expectOne('/api/users/register')
      .flush('', { status: 409, statusText: 'Conflict' });

    fixture.detectChanges();
    expect(pageText()).toContain('Ya existe una cuenta con ese email.');
    expect(session.isLoggedIn()).toBe(false);
  });

  it('asks for a manual login when the account was created but signing in failed', () => {
    submitValidDetails();

    httpController.expectOne('/api/users/register').flush('User registered successfully');
    httpController
      .expectOne('/api/auth/login')
      .flush('', { status: 500, statusText: 'Server Error' });

    expect(router.navigate).toHaveBeenCalledWith(['/login'], {
      queryParams: { reason: 'registered' }
    });
    fixture.detectChanges();
    expect(pageText()).not.toContain('No se pudo crear la cuenta');
  });

  it('does not call the api when the password is too short', () => {
    fillIn('#email', 'nueva@roamdeck.com');
    fillIn('#password', 'short');
    submit();

    httpController.expectNone('/api/users/register');
  });

  function submitValidDetails(): void {
    fillIn('#email', 'nueva@roamdeck.com');
    fillIn('#password', 'secret123');
    submit();
  }

  function fillIn(selector: string, value: string): void {
    const input: HTMLInputElement = fixture.nativeElement.querySelector(selector);
    input.value = value;
    input.dispatchEvent(new Event('input'));
  }

  function submit(): void {
    fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit'));
    fixture.detectChanges();
  }

  function pageText(): string {
    return fixture.nativeElement.textContent;
  }
});

function aToken(): string {
  const expiration = Math.floor(Date.now() / 1000) + 3600;
  const payload = btoa(JSON.stringify({ sub: 'a-user-id', exp: expiration }))
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=/g, '');

  return `a-header.${payload}.a-signature`;
}
