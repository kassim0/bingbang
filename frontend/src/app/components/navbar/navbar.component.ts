import {Component, signal} from '@angular/core';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatButtonModule} from '@angular/material/button';
import {MatMenuModule} from '@angular/material/menu';
import {MatIconModule} from '@angular/material/icon';
import {AuthService} from "../../services/auth.service";
import {User} from "../../models/user.model";
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

  connectedUser  =  signal<User|null>(null);

  constructor( private authService: AuthService,public dialog:MatDialog) {
    this.connectedUser=authService.connectedUser;
  }

  logout(){
    this.authService.logout();
  }

  openAuthDialog(){
    const dialogRef = this.dialog.open(AuthDialogComponent,{
      width: '40%',
      height:'90%',
      data: {}
    })
  }
}
