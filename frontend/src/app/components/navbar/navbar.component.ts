import {Component} from '@angular/core';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatButtonModule} from '@angular/material/button';
import {MatMenuModule} from '@angular/material/menu';
import {MatIconModule} from '@angular/material/icon';
import {AuthService} from "../../services/auth.service";
import {AuthDialogComponent} from "../auth-dialog/auth-dialog.component";
import {MatDialog} from "@angular/material/dialog";


@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [MatToolbarModule, MatButtonModule, MatMenuModule, MatIconModule],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.scss'
})
export class NavbarComponent {

  connectedUser = this.authService.connectedUser;

  constructor( private authService: AuthService,public dialog:MatDialog) {}

  logout(){
    this.authService.logout();
  }

  openAuthDialog(){
    this.dialog.open(AuthDialogComponent,{
      width: '40%',
      height:'50%',
    });
  }
}
