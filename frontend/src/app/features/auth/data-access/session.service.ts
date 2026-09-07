import { Injectable, computed, signal } from '@angular/core';

const TOKEN_STORAGE_KEY = 'roamdeck.token';

@Injectable({ providedIn: 'root' })
export class SessionService {
  private readonly storedToken = signal<string | null>(readValidTokenFromStorage());

  readonly token = this.storedToken.asReadonly();
  readonly isLoggedIn = computed(() => this.storedToken() !== null);

  start(token: string): void {
    writeToStorage(token);
    this.storedToken.set(token);
  }

  end(): void {
    removeFromStorage();
    this.storedToken.set(null);
  }

  hasActiveSession(): boolean {
    const token = this.storedToken();

    if (token === null) {
      return false;
    }

    if (hasExpired(token)) {
      this.end();
      return false;
    }

    return true;
  }
}

function readValidTokenFromStorage(): string | null {
  const token = readFromStorage();

  if (token === null) {
    return null;
  }

  if (hasExpired(token)) {
    removeFromStorage();
    return null;
  }

  return token;
}

function hasExpired(token: string): boolean {
  const expiration = expirationOf(token);

  return expiration === null || expiration <= Date.now();
}

function expirationOf(token: string): number | null {
  const payload = token.split('.')[1];

  if (payload === undefined) {
    return null;
  }

  try {
    const claims = JSON.parse(decodeBase64Url(payload));

    return typeof claims.exp === 'number' ? claims.exp * 1000 : null;
  } catch {
    return null;
  }
}

function decodeBase64Url(value: string): string {
  const base64 = value.replace(/-/g, '+').replace(/_/g, '/');
  const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=');
  const bytes = Uint8Array.from(atob(padded), (character) => character.charCodeAt(0));

  return new TextDecoder().decode(bytes);
}

function readFromStorage(): string | null {
  try {
    return localStorage.getItem(TOKEN_STORAGE_KEY);
  } catch {
    return null;
  }
}

function writeToStorage(token: string): void {
  try {
    localStorage.setItem(TOKEN_STORAGE_KEY, token);
  } catch {
    // A session that only lives in memory is better than a crash on submit.
  }
}

function removeFromStorage(): void {
  try {
    localStorage.removeItem(TOKEN_STORAGE_KEY);
  } catch {
    // Nothing to do: the token is dropped from memory either way.
  }
}
