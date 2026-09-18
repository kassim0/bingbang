import {Component, effect} from '@angular/core';
import {NgForOf, NgIf} from '@angular/common';
import {GamesSearchPopupComponent} from "../games-search-popup/games-search-popup.component";
import {MatDialog} from "@angular/material/dialog";
import {GameApiService} from "../../services/game-api.service";
import {AuthService} from "../../services/auth.service";
import {GamesList, GamesSearchPopupResult} from "../../models/games.model";
import {NewGameList} from "../../models/rawg.models";
import {ApercuGamesListComponent} from "../apercu-games-list/apercu-games-list.component";
import {MatButton} from "@angular/material/button";
import {MyGameListComponent} from "../my-game-list/my-game-list.component";

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    NgForOf,
    NgIf,
    ApercuGamesListComponent,
    MatButton,
  ],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent {

  GamesLists : GamesList[] = [];

  constructor(public dialog:MatDialog,
              private gameApiService: GameApiService,
              public authService: AuthService) {
    /** Les listes de jeux appartiennent à un compte : on les (re)charge à chaque connexion/déconnexion. */
    effect(() => {
      if (this.authService.currentUser()) {
        this.refreshGamesLists();
      } else {
        this.GamesLists = [];
      }
    });
  }

  refreshGamesLists() {
    this.gameApiService.getGamesList().subscribe(gamesLists => {
      this.GamesLists = gamesLists;
    });
  }

  OpenGamesSearchPopup() {
    const dialogRef = this.dialog.open(GamesSearchPopupComponent,{
      width: '40%',
      height:'90%',
      data: {}
    })

    dialogRef.afterClosed().subscribe((result?: GamesSearchPopupResult) => {
      if (!result?.addedGames?.length) {
        return;
      }
      const newList: NewGameList = {name: result.listName, rawgGames: result.addedGames};
      this.gameApiService.saveGamesList(newList).subscribe(() => {
        this.refreshGamesLists();
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
      this.refreshGamesLists();
    })
  }


}
