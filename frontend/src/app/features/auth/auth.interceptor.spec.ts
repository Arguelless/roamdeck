import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { vi } from 'vitest';

import { authInterceptor } from './auth.interceptor';
import { SessionService } from './data-access/session.service';

describe('authInterceptor', () => {
  let http: HttpClient;
  let httpController: HttpTestingController;
  let session: SessionService;
  let router: Router;

  beforeEach(() => {
    localStorage.clear();

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        provideRouter([])
      ]
    });

    http = TestBed.inject(HttpClient);
    httpController = TestBed.inject(HttpTestingController);
    session = TestBed.inject(SessionService);
    router = TestBed.inject(Router);

    vi.spyOn(router, 'navigate').mockResolvedValue(true);
  });

  afterEach(() => {
    httpController.verify();
  });

  it('sends the token of an open session', () => {
    session.start(aToken());
    http.get('/api/itineraries').subscribe();

    const request = httpController.expectOne('/api/itineraries');

    expect(request.request.headers.get('Authorization')).toBe(`Bearer ${aToken()}`);
  });

  it('sends no authorization header when there is no session', () => {
    http.get('/api/health').subscribe();

    const request = httpController.expectOne('/api/health');

    expect(request.request.headers.has('Authorization')).toBe(false);
  });

  it('ends the session and asks for a new login when an authenticated request is rejected', () => {
    session.start(aToken());
    http.get('/api/itineraries').subscribe({ error: () => undefined });

    httpController
      .expectOne('/api/itineraries')
      .flush('', { status: 401, statusText: 'Unauthorized' });

    expect(session.isLoggedIn()).toBe(false);
    expect(router.navigate).toHaveBeenCalledWith(['/login'], {
      queryParams: { returnUrl: '/', reason: 'expired' }
    });
  });

  it('leaves a rejected login alone instead of treating it as an expired session', () => {
    http.post('/api/auth/login', {}).subscribe({ error: () => undefined });

    httpController
      .expectOne('/api/auth/login')
      .flush('', { status: 401, statusText: 'Unauthorized' });

    expect(router.navigate).not.toHaveBeenCalled();
  });
});

function aToken(): string {
  const expiration = Math.floor(Date.now() / 1000) + 3600;
  const payload = btoa(JSON.stringify({ sub: 'a-user-id', exp: expiration }))
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=/g, '');

  return `a-header.${payload}.a-signature`;
}
