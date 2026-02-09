import {Component} from '@angular/core';
import {MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatListModule} from '@angular/material/list';
import {MatDividerModule} from '@angular/material/divider';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatSnackBar, MatSnackBarModule} from '@angular/material/snack-bar';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {FormsModule} from '@angular/forms';
import {NgForOf, NgIf} from '@angular/common';
import {SearchBarComponent} from '../search-bar/search-bar.component';
import {GameService} from '../../services/game.service';
import {GameListService} from '../../services/game-list.service';
import {RawgResultsDto} from '../../models/game.model';

@Component({
  selector: 'app-create-list-dialog',
  standalone: true,
  imports: [
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatListModule,
    MatDividerModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatFormFieldModule,
    MatInputModule,
    FormsModule,
    NgForOf,
    NgIf,
    SearchBarComponent
  ],
  templateUrl: './create-list-dialog.component.html',
  styleUrl: './create-list-dialog.component.scss'
})
export class CreateListDialogComponent {
  listName: string = '';
  games: RawgResultsDto[] = [];
  addedGames: RawgResultsDto[] = [];
  isLoading = false;
  isCreating = false;
  hasSearched = false;

  constructor(
    public dialogRef: MatDialogRef<CreateListDialogComponent>,
    private gameService: GameService,
    private gameListService: GameListService,
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
    if (!this.addedGames.find(g => g.id === game.id)) {
      this.addedGames.push(game);
      this.snackBar.open(`"${game.name}" ajouté à la liste`, 'OK', {
        duration: 3000
      });
    }
  }

  removeGame(game: RawgResultsDto) {
    this.addedGames = this.addedGames.filter(g => g.id !== game.id);
  }

  isGameAdded(game: RawgResultsDto): boolean {
    return this.addedGames.some(g => g.id === game.id);
  }

  createList() {
    if (!this.listName.trim() || this.addedGames.length === 0) {
      return;
    }

    this.isCreating = true;

    this.gameListService.createList(this.listName.trim()).subscribe({
      next: (createdList) => {
        let completed = 0;
        const total = this.addedGames.length;

        this.addedGames.forEach(game => {
          this.gameListService.addGameToList(createdList.id, game).subscribe({
            next: () => {
              completed++;
              if (completed === total) {
                this.isCreating = false;
                this.snackBar.open(`Liste "${this.listName}" créée avec ${total} jeu(x)!`, 'OK', {
                  duration: 3000
                });
                this.dialogRef.close(true);
              }
            },
            error: (err) => {
              console.error('Erreur lors de l\'ajout du jeu:', err);
              completed++;
              if (completed === total) {
                this.isCreating = false;
                this.dialogRef.close(true);
              }
            }
          });
        });
      },
      error: (err) => {
        console.error('Erreur lors de la création de la liste:', err);
        this.isCreating = false;
        this.snackBar.open('Erreur lors de la création de la liste', 'OK', {
          duration: 3000
        });
      }
    });
  }

  close() {
    this.dialogRef.close();
  }
}
