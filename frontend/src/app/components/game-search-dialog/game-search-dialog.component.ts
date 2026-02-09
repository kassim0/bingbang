import {Component, OnInit} from '@angular/core';
import {MatDialogRef, MatDialogModule} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatListModule} from '@angular/material/list';
import {MatDividerModule} from '@angular/material/divider';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatSnackBar, MatSnackBarModule} from '@angular/material/snack-bar';
import {MatMenuModule} from '@angular/material/menu';
import {NgForOf, NgIf} from '@angular/common';
import {SearchBarComponent} from '../search-bar/search-bar.component';
import {GameService} from '../../services/game.service';
import {GameListService} from '../../services/game-list.service';
import {GameList, RawgResultsDto} from '../../models/game.model';

@Component({
  selector: 'app-game-search-dialog',
  standalone: true,
  imports: [
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatListModule,
    MatDividerModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatMenuModule,
    NgForOf,
    NgIf,
    SearchBarComponent
  ],
  templateUrl: './game-search-dialog.component.html',
  styleUrl: './game-search-dialog.component.scss'
})
export class GameSearchDialogComponent implements OnInit {

  games: RawgResultsDto[] = [];
  gameLists: GameList[] = [];
  isLoading = false;
  hasSearched = false;

  constructor(
    public dialogRef: MatDialogRef<GameSearchDialogComponent>,
    private gameService: GameService,
    private gameListService: GameListService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.gameListService.getAllLists().subscribe({
      next: (lists) => this.gameLists = lists,
      error: (err) => console.error('Erreur lors du chargement des listes:', err)
    });
  }

  onSearch(query: string) {
    this.isLoading = true;
    this.hasSearched = true;

    this.gameService.searchGamesByName(query).subscribe({
      next: (response) => {
        this.games = response.results || [];
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Erreur lors de la recherche:', err);
        this.games = [];
        this.isLoading = false;
      }
    });
  }

  addGameToList(game: RawgResultsDto, list: GameList) {
    this.gameListService.addGameToList(list.id, game).subscribe({
      next: () => {
        this.snackBar.open(`"${game.name}" ajouté à "${list.name}"!`, 'OK', {
          duration: 3000
        });
      },
      error: (err) => {
        this.snackBar.open(`Erreur lors de l'ajout du jeu`, 'OK', {
          duration: 3000
        });
        console.error('Erreur:', err);
      }
    });
  }

  close() {
    this.dialogRef.close();
  }
}
