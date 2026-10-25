import {HttpClient} from "@angular/common/http";
import {LoginRequest,RegisterRequest,AuthResponse,User} from "../models/user.model";
import {Injectable, signal} from "@angular/core";

@Injectable({ providedIn: 'root' })
export class AuthService{

  constructor(private http: HttpClient) {}

  private base = '/api/auth';

  public connectedUser = signal(null)

  register(registerRequest:RegisterRequest){
    return this.http.post<AuthResponse>(`${this.base}/register`, registerRequest);
  }

  login(loginRequest:LoginRequest){
    return this.http.post<AuthResponse>(`${this.base}/login`, loginRequest);
  }

  connectUser(user:User){
    this.connectedUser.set(user);
  }

  logout(){}

  getToken(){}
}
