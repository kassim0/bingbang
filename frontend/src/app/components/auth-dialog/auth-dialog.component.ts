import { Component } from '@angular/core';
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {MatButtonModule} from "@angular/material/button";
import {FormsModule} from "@angular/forms";
import {MatIconModule} from "@angular/material/icon";
import {HttpErrorResponse} from "@angular/common/http";
import {AuthService} from "../../services/auth.service";
import {LoginRequest, RegisterRequest} from "../../models/user.model";
import {MatDialogRef} from "@angular/material/dialog";

@Component({
  selector: 'app-auth-dialog',
  standalone: true,
  imports: [MatFormFieldModule,
  MatInputModule,
  MatButtonModule,
  FormsModule,
  MatIconModule],
  templateUrl: './auth-dialog.component.html',
  styleUrl: './auth-dialog.component.scss'
})
export class AuthDialogComponent {

  usernameInput = '';
  emailInput = '';
  passwordInput = '';

  page: 'login' | 'register' = 'register';
  errorMessage = '';
  loading = false;

  constructor(private authService: AuthService,
              public dialogRef: MatDialogRef<AuthDialogComponent>) {
  }

  switchPage(){
    this.page = this.page === 'register' ? 'login' : 'register';
    this.passwordInput = '';
    this.errorMessage = '';
  }

  registerFormComplete(){
    return this.usernameInput !== '' && this.emailInput !== '' && this.passwordInput !== '';
  }

  loginFormComplete(){
    return this.emailInput !== '' && this.passwordInput !== '';
  }

  register(){
    if(!this.registerFormComplete()){
      this.errorMessage = 'Please fill in all fields.';
      return;
    }
    const registerRequest : RegisterRequest = {
      username: this.usernameInput,
      email: this.emailInput,
      password: this.passwordInput
    };
    this.startRequest();
    this.authService.register(registerRequest).subscribe({
      next: () => this.dialogRef.close(),
      error: err => this.handleError(err)
    });
  }

  login(){
    if(!this.loginFormComplete()){
      this.errorMessage = 'Please fill in all fields.';
      return;
    }
    const loginRequest : LoginRequest = {
      email: this.emailInput,
      password: this.passwordInput
    };

    this.startRequest();
    this.authService.login(loginRequest).subscribe({
      next: () => this.dialogRef.close(),
      error: err => this.handleError(err)
    });
  }

  private startRequest(){
    this.errorMessage = '';
    this.loading = true;
  }

  private handleError(err: HttpErrorResponse){
    this.loading = false;
    switch(err.status){
      case 400:
        this.errorMessage = 'Invalid email, or password not between 8 and 72 characters.';
        break;
      case 401:
        this.errorMessage = 'Wrong email or password.';
        break;
      case 409:
        this.errorMessage = 'This email is already in use.';
        break;
      default:
        this.errorMessage = 'Something went wrong, please try again.';
    }
  }

}
