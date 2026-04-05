import {Component} from '@angular/core';
import {MatDialogRef, MatDialogTitle, MatDialogContent} from '@angular/material/dialog';
import {MatTabsModule} from '@angular/material/tabs';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';
import {FormsModule} from '@angular/forms';
import {AuthService} from '../../services/auth.service';

@Component({
  selector: 'app-auth-dialog',
  standalone: true,
  imports: [
    MatDialogTitle, MatDialogContent,
    MatTabsModule, MatFormFieldModule, MatInputModule, MatButtonModule, FormsModule
  ],
  templateUrl: './auth-dialog.component.html',
  styleUrl: './auth-dialog.component.scss'
})
export class AuthDialogComponent {

  loginUsername = '';
  loginPassword = '';

  registerUsername = '';
  registerPassword = '';
  registerConfirm = '';

  constructor(
    private dialogRef: MatDialogRef<AuthDialogComponent>,
    private authService: AuthService
  ) {}

  onLogin() {
    if (!this.loginUsername || !this.loginPassword) return;
    this.authService.login(this.loginUsername);
    this.dialogRef.close();
  }

  onRegister() {
    if (!this.registerUsername || !this.registerPassword) return;
    if (this.registerPassword !== this.registerConfirm) return;
    this.authService.register(this.registerUsername);
    this.dialogRef.close();
  }
}
