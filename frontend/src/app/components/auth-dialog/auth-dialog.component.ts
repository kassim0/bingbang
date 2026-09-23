import {Component} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {MatButtonModule} from '@angular/material/button';
import {MatDialogRef} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatIconModule} from '@angular/material/icon';
import {MatInputModule} from '@angular/material/input';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {NgIf} from '@angular/common';
import {AuthService} from '../../services/auth.service';

@Component({
  selector: 'app-auth-dialog',
  standalone: true,
  imports: [
    FormsModule, MatButtonModule, MatFormFieldModule, MatIconModule, MatInputModule, MatProgressSpinnerModule, NgIf,
  ],
  templateUrl: './auth-dialog.component.html',
  styleUrl: './auth-dialog.component.scss'
})
export class AuthDialogComponent {

  mode: 'login' | 'register' = 'login';
  username = '';
  email = '';
  password = '';
  isSubmitting = false;
  errorMessage = '';

  constructor(
    public dialogRef: MatDialogRef<AuthDialogComponent>,
    private authService: AuthService) {
  }

  toggleMode() {
    this.mode = this.mode === 'login' ? 'register' : 'login';
    this.errorMessage = '';
  }

  close() {
    this.dialogRef.close();
  }

  submit() {
    this.isSubmitting = true;
    this.errorMessage = '';

    const request = this.mode === 'login'
      ? this.authService.login({email: this.email, password: this.password})
      : this.authService.register({username: this.username, email: this.email, password: this.password});

    request.subscribe({
      next: () => {
        this.isSubmitting = false;
        this.dialogRef.close(true);
      },
      error: (err) => {
        this.isSubmitting = false;
        this.errorMessage = this.mode === 'login'
          ? 'Email ou mot de passe incorrect.'
          : (err?.error?.message ?? 'Impossible de créer le compte.');
      }
    });
  }
}
