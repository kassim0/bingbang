import {Component, Input} from '@angular/core';
import {MatButton, MatButtonModule} from "@angular/material/button";
import {
  MatDialogRef,
  MatDialogTitle,
  MatDialogContent,
  MatDialogActions, MatDialogClose
} from "@angular/material/dialog";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {FormsModule} from "@angular/forms";
import {SearchBarComponent} from "../share/search-bar/search-bar.component";
import {RawgResultsDto} from "../../models/rawg.models";
import {NgForOf} from "@angular/common";
import {MatList, MatListItem} from "@angular/material/list";
import {MatDivider} from "@angular/material/divider";
import {GameItemComponent} from "../share/game-item/game-item.component";
import {MatSnackBar} from "@angular/material/snack-bar";
import {GameApiService} from "../../services/game-api.service";

@Component({
  selector: 'app-list-games-popup',
  standalone: true,
  imports: [
    MatButton, MatFormFieldModule,
    MatInputModule,
    FormsModule,
    MatButtonModule,
    MatDialogTitle,
    MatDialogContent,
    MatDialogActions,
    MatDialogClose, SearchBarComponent, NgForOf, MatList, MatListItem, MatDivider, GameItemComponent,
  ],
  templateUrl: './list-games-popup.component.html',
  styleUrl: './list-games-popup.component.scss'
})
export class ListGamesPopupComponent{

  reponse : RawgResultsDto[] | undefined;

  @Input()
  gameNameSearch:string='';

  constructor(
    public dialogRef: MatDialogRef<ListGamesPopupComponent>,
    private gameApiService: GameApiService,
    private snackBar: MatSnackBar) {
  }

  close() {
    this.dialogRef.close();
  }

  receiveData(data: string) {
    this.gameNameSearch = data;
    this.gameApiService.searchGames(this.gameNameSearch).subscribe((reponse) => {
      this.reponse = reponse.results;
    });
  }

  onAddGame(game: RawgResultsDto) {
    this.gameApiService.saveGame(game).subscribe({
      next: () => {
        this.snackBar.open(`"${game.name}" ajouté avec succès!`, 'OK', {
          duration: 3000
        });
      },
      error: (err) => {
        this.snackBar.open(`Erreur lors de l'ajout du jeu`, 'OK', {
          duration: 3000
        });
        console.error('Erreur lors de l\'ajout du jeu:', err);
      }
    });
  }
}
