import { Component } from '@angular/core';
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {MatButtonModule} from "@angular/material/button";
import {FormsModule} from "@angular/forms";
import {MatIconModule} from "@angular/material/icon";
import {NgIf} from "@angular/common";
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
  MatIconModule,
    NgIf],
  templateUrl: './auth-dialog.component.html',
  styleUrl: './auth-dialog.component.scss'
})
export class AuthDialogComponent {

  usernameRegisterInput = '';
  emailRegisterInput = '';
  passwordRegisterInput = '';
  emailLoginInput = '';
  passwordLoginInput = '';

  registerPage = 'register';
  loginPage = 'login';

  constructor(private authService: AuthService,
              public dialogRef: MatDialogRef<AuthDialogComponent>) {
  }

  page = this.registerPage;

  switchPage(){
    if(this.page == this.registerPage){
      this.page = this.loginPage;
    }
    else {this.page = this.registerPage;}
  }

  registerFormComplete(){
    return this.usernameRegisterInput != '' && this.emailRegisterInput != '' && this.passwordRegisterInput != '';
  }

  loginFormComplete(){
    return this.emailLoginInput != '' && this.passwordLoginInput != '';
  }

  register(){
    if(this.registerFormComplete()){
      const registerRequest : RegisterRequest ={
        'username' : this.usernameRegisterInput,
        'email' : this.emailRegisterInput,
        'password' : this.passwordRegisterInput
      }
      this.authService.register(registerRequest).subscribe({
        error : err => console.log(err),
        complete: () => this.dialogRef.close()
      })
    }
    else {
      console.log('none valid inputs !');
    }
  }

  login(){
    if(this.loginFormComplete()){
      const loginRequest : LoginRequest ={
        'email':this.emailLoginInput,
        'password' : this.passwordLoginInput
      }
      this.authService.login(loginRequest).subscribe({
        error : err => console.log(err),
        complete: () => this.dialogRef.close()
      })
    }
    else {
      console.log('none valid inputs !');
    }
  }

}
