import {Component, computed} from '@angular/core';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatButtonModule} from '@angular/material/button';
import {MatMenuModule} from '@angular/material/menu';
import {MatIconModule} from '@angular/material/icon';
import {MatDialog} from '@angular/material/dialog';
import {AuthService} from '../../services/auth.service';
import {AuthDialogComponent} from '../auth-dialog/auth-dialog.component';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [MatToolbarModule, MatButtonModule, MatMenuModule, MatIconModule],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.scss'
})
export class NavbarComponent {

  currentUser = computed(() => this.authService.currentUser());

  constructor(private authService: AuthService, private dialog: MatDialog) {}

  openAuthDialog() {
    this.dialog.open(AuthDialogComponent, {width: '400px'});
  }

  logout() {
    this.authService.logout();
  }
}
