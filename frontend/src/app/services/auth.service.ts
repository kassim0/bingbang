import {HttpClient} from "@angular/common/http";
import {AuthResponse, LoginRequest, RegisterRequest, User} from "../models/user.model";
import {Inject, Injectable, PLATFORM_ID, signal} from "@angular/core";
import {finalize, tap} from "rxjs";
import {isPlatformBrowser} from "@angular/common";

@Injectable({ providedIn: 'root' })
export class AuthService{

  constructor(private http: HttpClient
              ,@Inject(PLATFORM_ID) private platformId: Object) {
    if(isPlatformBrowser(this.platformId)) {
      if(localStorage.getItem('token')) {
        this.getMe().subscribe({error : () => this.clearSession()})
      }
      else {
        this.guest().subscribe({error : () => this.clearSession()})
      }
    }
  }

  private base = '/api/auth';

  public connectedUser = signal<User|null>(null);

  register(registerRequest:RegisterRequest){
    return this.http.post<AuthResponse>(`${this.base}/register`, registerRequest).pipe(
      tap(response=>this.connectUser(response.user,response.token))
    );
  }

  login(loginRequest:LoginRequest){
    return this.http.post<AuthResponse>(`${this.base}/login`, loginRequest).pipe(
      tap(response=>this.connectUser(response.user,response.token))
    );
  }

  private getMe(){
    return this.http.get<User>(`${this.base}/me`).pipe(
      tap(response=>{this.connectUser(response)})
    );
  }

  public guest(){
    return this.http.post<AuthResponse>(`${this.base}/guest`,'').pipe(
      tap(response=>{
        if (this.connectedUser()===null){
          this.connectUser(response.user,response.token)
        }
      })
    );
  }

  connectUser(user:User,token?:string){
    this.connectedUser.set(user);
    if(token && isPlatformBrowser(this.platformId)){
      localStorage.setItem('token', token);
    }
  }

  clearSession(){
    this.connectedUser.set(null);
    if(isPlatformBrowser(this.platformId)){
      localStorage.removeItem('token');
    }
  }

  logout(){
    return this.http.post<void>(`${this.base}/logout`,'').pipe(
      finalize(()=>this.clearSession())
    )
  }

  getToken(){
    if(isPlatformBrowser(this.platformId)){
      return localStorage.getItem('token');
    }
    return null;
  }
}
