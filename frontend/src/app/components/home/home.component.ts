import {Component, OnInit} from '@angular/core';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatDialog} from '@angular/material/dialog';
import {NgForOf, NgIf} from '@angular/common';
import {Router} from '@angular/router';
import {GameSearchDialogComponent} from '../game-search-dialog/game-search-dialog.component';
import {CreateListDialogComponent} from '../create-list-dialog/create-list-dialog.component';
import {GameListService} from '../../services/game-list.service';
import {AuthService} from '../../services/auth.service';
import {GameList} from '../../models/game.model';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    MatButtonModule,
    MatIconModule,
    NgForOf,
    NgIf
  ],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent implements OnInit {
  gameLists: GameList[] = [];

  constructor(
    private dialog: MatDialog,
    private gameListService: GameListService,
    private authService: AuthService,
    private router: Router
  ) {}

  get isLoggedIn(): boolean {
    return this.authService.isLoggedIn();
  }

  goToLogin() {
    this.router.navigate(['/login']);
  }

  logout() {
    this.authService.logout();
    this.loadLists();
  }

  ngOnInit() {
    this.loadLists();
  }

  loadLists() {
    this.gameListService.getAllLists().subscribe({
      next: (lists) => this.gameLists = lists,
      error: (err) => console.error('Erreur lors du chargement des listes:', err)
    });
  }

  openSearchDialog() {
    this.dialog.open(GameSearchDialogComponent, {
      width: '600px',
      maxHeight: '80vh'
    });
  }

  openCreateListDialog() {
    const dialogRef = this.dialog.open(CreateListDialogComponent, {
      width: '600px',
      maxHeight: '80vh'
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadLists();
      }
    });
  }
}
