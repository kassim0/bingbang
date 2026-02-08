import {Component} from '@angular/core';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatDialog} from '@angular/material/dialog';
import {GameSearchDialogComponent} from '../game-search-dialog/game-search-dialog.component';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    MatButtonModule,
    MatIconModule
  ],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent {

  constructor(private dialog: MatDialog) {}

  openSearchDialog() {
    this.dialog.open(GameSearchDialogComponent, {
      width: '600px',
      maxHeight: '80vh'
    });
  }
}
