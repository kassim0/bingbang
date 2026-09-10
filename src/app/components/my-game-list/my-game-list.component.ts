import {Component, Inject} from '@angular/core';
import {Game, GamesList, UpdateGamesList} from "../../models/games.model";
import {NgClass, NgForOf} from "@angular/common";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {MatIcon} from "@angular/material/icon";
import {MatButton, MatIconButton} from "@angular/material/button";
import {GameApiService} from "../../services/game-api.service";
import {FormsModule} from "@angular/forms";
import {MatFormField} from "@angular/material/form-field";
import {MatInput} from "@angular/material/input";
import {GamesSearchPopupComponent} from "../games-search-popup/games-search-popup.component";

@Component({
  selector: 'app-my-game-list',
  standalone: true,
  imports: [
    NgForOf,
    MatIcon,
    MatIconButton,
    NgClass,
    MatButton,
    FormsModule,
    MatFormField,
    MatInput
  ],
  templateUrl: './my-game-list.component.html',
  styleUrl: './my-game-list.component.scss'
})
export class MyGameListComponent {

  gamesList : GamesList;
  selectedGames : Game[] = [];
  updateGamesList : UpdateGamesList;
  editedName : string;

  constructor(@Inject(MAT_DIALOG_DATA) public data: { gamesList: GamesList },
              private gameApiService: GameApiService,
              private dialogRef: MatDialogRef<MyGameListComponent>) {
    this.gamesList = data.gamesList;
    this.editedName = data.gamesList.name;
    this.updateGamesList = {
      gamesListId: this.gamesList.id,
      newGameId: null,
      removeGameId: null,
      newName: null,
    };
  }

  onDeleteGame(game: Game) {
    if(this.isGameSelected(game)) {
      this.selectedGames = this.selectedGames.filter(g=>g.id!==game.id);
    }
    else {
      this.selectedGames.push(game);
    }
  }

  isGameSelected(game:Game): boolean {
    return this.selectedGames.some(g => g.id === game?.id);
  }

  saveModifGamesList(){
    this.updateGamesList.removeGameId = this.selectedGames?.map(g => g.id);
    this.updateGamesList.newName = this.editedName !== this.gamesList.name ? this.editedName : null;
    this.gameApiService.updateGamesList(this.updateGamesList).subscribe({
      next: res => {
        console.log('updateGamesList ok', res);
        this.dialogRef.close();
      },
      error: err => console.error('updateGamesList error', err),
    });
  }

}
