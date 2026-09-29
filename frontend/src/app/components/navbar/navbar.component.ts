import {Component, computed} from '@angular/core';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatButtonModule} from '@angular/material/button';
import {MatMenuModule} from '@angular/material/menu';
import {MatIconModule} from '@angular/material/icon';
import {MatDialog} from '@angular/material/dialog';
import {AuthDialogComponent} from '../auth-dialog/auth-dialog.component';
import {AuthService} from '../../services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [MatToolbarModule, MatButtonModule, MatMenuModule, MatIconModule],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.scss'
})
export class NavbarComponent {

  currentUser = this.authService.currentUser;
  /** Un compte invité est déjà "connecté" (currentUser non nul) mais doit quand même pouvoir créer un vrai compte. */
  isRealAccount = computed(() => !!this.currentUser() && !this.currentUser()!.guest);

  constructor(private dialog: MatDialog, private authService: AuthService) {}

  openAuthDialog() {
    this.dialog.open(AuthDialogComponent, {
      width: '400px'
    });
  }

  logout() {
    this.authService.logout();
  }
}
