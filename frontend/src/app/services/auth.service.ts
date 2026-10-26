import {HttpClient} from "@angular/common/http";
import {AuthResponse, LoginRequest, RegisterRequest, User} from "../models/user.model";
import {Inject, Injectable, PLATFORM_ID, signal} from "@angular/core";
import {tap} from "rxjs";
import {isPlatformBrowser} from "@angular/common";

@Injectable({ providedIn: 'root' })
export class AuthService{

  constructor(private http: HttpClient
              ,@Inject(PLATFORM_ID) private platformId: Object) {
    if(isPlatformBrowser(this.platformId) && localStorage.getItem('token')) {
      this.getMe().subscribe()
    }
  }

  private base = '/api/auth';

  public connectedUser = signal<AuthResponse|null>(null);

  register(registerRequest:RegisterRequest){
    return this.http.post<AuthResponse>(`${this.base}/register`, registerRequest).pipe(
      tap(response=>this.connectUser(response))
    );
  }

  login(loginRequest:LoginRequest){
    return this.http.post<AuthResponse>(`${this.base}/login`, loginRequest).pipe(
      tap(response=>this.connectUser(response))
    );
  }

  private getMe(){
    return this.http.get<User>(`${this.base}/me`).pipe(
      tap(response=>{
        const auth : AuthResponse = {
          token: localStorage.getItem('token') ?? '',
          user: response
        };
        this.connectUser(auth)
      })
    );
  }

  connectUser(authResponse:AuthResponse){
    this.connectedUser.set(authResponse);
    if(isPlatformBrowser(this.platformId)){
      localStorage.setItem('token', authResponse.token);
    }
  }

  logout(){
    this.connectedUser.set(null);
    if(isPlatformBrowser(this.platformId)){
      localStorage.removeItem('token');
    }
  }

  getToken(){
    return this.connectedUser()?.token;
  }
}
