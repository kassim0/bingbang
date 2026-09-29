import {Component, Inject, Input} from '@angular/core';
import {MatButton, MatButtonModule} from "@angular/material/button";
import {
  MAT_DIALOG_DATA,
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
import {GamesList} from "../../models/games.model";
import {NgForOf, NgIf} from "@angular/common";
import {MatList, MatListItem} from "@angular/material/list";
import {MatDivider} from "@angular/material/divider";
import {GameItemComponent} from "../share/game-item/game-item.component";
import {MatSnackBar} from "@angular/material/snack-bar";
import {GameApiService} from "../../services/game-api.service";
import {MatIcon} from "@angular/material/icon";
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";

@Component({
  selector: 'app-games-search-popup',
  standalone: true,
  imports: [
    MatFormFieldModule,
    MatInputModule,
    FormsModule,
    MatButtonModule,
    SearchBarComponent, NgForOf, NgIf, MatList, MatListItem, MatDivider, GameItemComponent, MatIcon, MatProgressSpinnerModule,
  ],
  templateUrl: './games-search-popup.component.html',
  styleUrl: './games-search-popup.component.scss'
})
export class GamesSearchPopupComponent {

  reponse : RawgResultsDto[] | undefined;
  addedGames: RawgResultsDto[] = [];
  listNameInput : string = "";
  isSearching : boolean = false;

  /** Liste cible quand la popup sert à ajouter des jeux à une GamesList existante. */
  gamesList : GamesList | null;

  @Input()
  gameNameSearch:string='';

  constructor(
    public dialogRef: MatDialogRef<GamesSearchPopupComponent>,
    private gameApiService: GameApiService,
    @Inject(MAT_DIALOG_DATA) public data: { gamesList?: GamesList }) {
    this.gamesList = data?.gamesList ?? null;
  }

  close() {
    if(this.addedGames.length > 0){
      this.dialogRef.close({addedGames: this.addedGames, listName: this.listNameInput});
      return;
    }
    this.dialogRef.close();
  }

  receiveData(data: string) {
    this.gameNameSearch = data;
    this.isSearching = true;
    this.gameApiService.searchGames(this.gameNameSearch).subscribe({
      next: (reponse) => {
        this.reponse = reponse.results;
        this.isSearching = false;
      },
      error: () => {
        this.isSearching = false;
      }
    });
  }

  onAddGame(game: RawgResultsDto){
    this.addedGames.push(game);
  }

  onRemoveGame(game: RawgResultsDto){
    this.addedGames = this.addedGames.filter(g=>g.id!==game.id);
  }

}
