import { TestBed } from '@angular/core/testing';

import { SessionService } from './session.service';

const TOKEN_STORAGE_KEY = 'roamdeck.token';

describe('SessionService', () => {
  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({});
  });

  it('keeps the token of a session that has just started', () => {
    const session = TestBed.inject(SessionService);
    const token = tokenExpiringInMinutes(60);

    session.start(token);

    expect(session.token()).toBe(token);
    expect(session.isLoggedIn()).toBe(true);
    expect(localStorage.getItem(TOKEN_STORAGE_KEY)).toBe(token);
  });

  it('forgets the token when the session ends', () => {
    const session = TestBed.inject(SessionService);
    session.start(tokenExpiringInMinutes(60));

    session.end();

    expect(session.token()).toBeNull();
    expect(session.isLoggedIn()).toBe(false);
    expect(localStorage.getItem(TOKEN_STORAGE_KEY)).toBeNull();
  });

  it('ends an already ended session without complaining', () => {
    const session = TestBed.inject(SessionService);

    session.end();
    session.end();

    expect(session.isLoggedIn()).toBe(false);
  });

  it('discards a stored token that expired while the app was closed', () => {
    localStorage.setItem(TOKEN_STORAGE_KEY, tokenExpiringInMinutes(-1));

    const session = TestBed.inject(SessionService);

    expect(session.isLoggedIn()).toBe(false);
    expect(localStorage.getItem(TOKEN_STORAGE_KEY)).toBeNull();
  });

  it('discards a stored token that cannot be read', () => {
    localStorage.setItem(TOKEN_STORAGE_KEY, 'not-even-a-jwt');

    const session = TestBed.inject(SessionService);

    expect(session.isLoggedIn()).toBe(false);
  });

  it('reports no active session once the token expires', () => {
    const session = TestBed.inject(SessionService);
    session.start(tokenExpiringInMinutes(-1));

    expect(session.hasActiveSession()).toBe(false);
    expect(session.token()).toBeNull();
  });

  it('reports an active session while the token is still valid', () => {
    const session = TestBed.inject(SessionService);
    session.start(tokenExpiringInMinutes(60));

    expect(session.hasActiveSession()).toBe(true);
  });
});

function tokenExpiringInMinutes(minutes: number): string {
  const expiration = Math.floor(Date.now() / 1000) + minutes * 60;
  const payload = btoa(JSON.stringify({ sub: 'a-user-id', exp: expiration }))
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=/g, '');

  return `a-header.${payload}.a-signature`;
}
