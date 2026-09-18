import {Injectable, PLATFORM_ID, inject, signal} from '@angular/core';
import {isPlatformBrowser} from '@angular/common';
import {HttpClient} from '@angular/common/http';
import {tap} from 'rxjs';
import {AuthResponse, LoginRequest, RegisterRequest, User} from '../models/user.model';

const TOKEN_KEY = 'bingbang_token';
const USER_KEY = 'bingbang_user';

@Injectable({providedIn: 'root'})
export class AuthService {

  private base = '/api/auth';
  private isBrowser = isPlatformBrowser(inject(PLATFORM_ID));

  currentUser = signal<User | null>(this.readStoredUser());

  constructor(private http: HttpClient) {
  }

  login(request: LoginRequest) {
    return this.http.post<AuthResponse>(`${this.base}/login`, request)
      .pipe(tap(response => this.storeSession(response)));
  }

  register(request: RegisterRequest) {
    return this.http.post<AuthResponse>(`${this.base}/register`, request)
      .pipe(tap(response => this.storeSession(response)));
  }

  logout() {
    this.currentUser.set(null);
    if (this.isBrowser) {
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
    }
  }

  getToken(): string | null {
    return this.isBrowser ? localStorage.getItem(TOKEN_KEY) : null;
  }

  private storeSession(response: AuthResponse) {
    this.currentUser.set(response.user);
    if (this.isBrowser) {
      localStorage.setItem(TOKEN_KEY, response.token);
      localStorage.setItem(USER_KEY, JSON.stringify(response.user));
    }
  }

  private readStoredUser(): User | null {
    if (!this.isBrowser) {
      return null;
    }
    const stored = localStorage.getItem(USER_KEY);
    return stored ? JSON.parse(stored) : null;
  }
}
