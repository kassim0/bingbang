import {Component, effect} from '@angular/core';
import {NgForOf} from '@angular/common';
import {GamesSearchPopupComponent} from "../games-search-popup/games-search-popup.component";
import {MatDialog} from "@angular/material/dialog";
import {GameApiService} from "../../services/game-api.service";
import {GamesList, GamesSearchPopupResult} from "../../models/games.model";
import {NewGameList} from "../../models/game-search.models";
import {ApercuGamesListComponent} from "../apercu-games-list/apercu-games-list.component";
import {MatButton} from "@angular/material/button";
import {MyGameListComponent} from "../my-game-list/my-game-list.component";
import {AuthService} from "../../services/auth.service";
import {BackgroundPickerComponent} from "../background-picker/background-picker.component";
import {ShareButtonComponent} from "../share-dialog/share-button.component";

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    NgForOf,
    ApercuGamesListComponent,
    MatButton,
    BackgroundPickerComponent,
    ShareButtonComponent,
  ],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent {

  gamesLists : GamesList[] = [];
  connectedUser = this.authService.connectedUser;

  constructor(private authService: AuthService,
              public dialog:MatDialog,
              private gameApiService: GameApiService) {
    effect(()=>{
      if(this.connectedUser()){
        this.gameApiService.getGamesList().subscribe(gamesLists => {
          this.gamesLists = gamesLists;
        });
      }
      else{
        this.gamesLists = [];
      }
    });
  }


  OpenGamesSearchPopup() {
    const dialogRef = this.dialog.open(GamesSearchPopupComponent,{
      width: '40%',
      height:'50%',
      data: {}
    })

    dialogRef.afterClosed().subscribe((result?: GamesSearchPopupResult) => {
      // une liste se crée avec un nom, des jeux, ou les deux
      if (!result || (result.addedGames.length === 0 && result.listName.length === 0)) {
        return;
      }
      const newList: NewGameList = {name: result.listName, games: result.addedGames};
      this.gameApiService.saveGamesList(newList).subscribe(() => {
        this.gameApiService.getGamesList().subscribe(gamesList => {
          this.gamesLists = gamesList;
        });
      });
    })
  }

  openMyGaleListPopup(gameList : GamesList){
    const dialogRef = this.dialog.open(MyGameListComponent,{
      width: '40%',
      height:'90%',
      data: {gamesList : gameList}
    })

    dialogRef.afterClosed().subscribe(() => {
      this.gameApiService.getGamesList().subscribe(gamesLists => {
        this.gamesLists = gamesLists;
      })
    })
  }


}
