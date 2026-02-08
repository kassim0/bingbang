import {Component} from '@angular/core';
import {MatDialogRef, MatDialogModule} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatListModule} from '@angular/material/list';
import {MatDividerModule} from '@angular/material/divider';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatSnackBar, MatSnackBarModule} from '@angular/material/snack-bar';
import {NgForOf, NgIf} from '@angular/common';
import {SearchBarComponent} from '../search-bar/search-bar.component';
import {GameService} from '../../services/game.service';
import {RawgResultsDto} from '../../models/game.model';

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
    NgForOf,
    NgIf,
    SearchBarComponent
  ],
  templateUrl: './game-search-dialog.component.html',
  styleUrl: './game-search-dialog.component.scss'
})
export class GameSearchDialogComponent {

  games: RawgResultsDto[] = [];
  isLoading = false;
  hasSearched = false;

  constructor(
    public dialogRef: MatDialogRef<GameSearchDialogComponent>,
    private gameService: GameService,
    private snackBar: MatSnackBar
  ) {}

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

  onAddGame(game: RawgResultsDto) {
    this.gameService.saveGame(game).subscribe({
      next: () => {
        this.snackBar.open(`"${game.name}" ajouté avec succès!`, 'OK', {
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
