import {Injectable, signal} from '@angular/core';

export interface User {
  username: string;
}

@Injectable({providedIn: 'root'})
export class AuthService {

  currentUser = signal<User | null>(null);

  login(username: string) {
    this.currentUser.set({username});
  }

  logout() {
    this.currentUser.set(null);
  }

  register(username: string) {
    this.currentUser.set({username});
  }
}
