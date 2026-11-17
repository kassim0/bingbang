import {Component, effect} from '@angular/core';
import {takeUntilDestroyed} from "@angular/core/rxjs-interop";
import {catchError, EMPTY, Subject, switchMap} from "rxjs";
import {GamesSearchPopupComponent} from "../games-search-popup/games-search-popup.component";
import {MatDialog} from "@angular/material/dialog";
import {MatSnackBar} from "@angular/material/snack-bar";
import {GameApiService} from "../../services/game-api.service";
import {GamesList, GamesSearchPopupResult} from "../../models/games.model";
import {NewGameList} from "../../models/game-search.models";
import {ApercuGamesListComponent} from "../apercu-games-list/apercu-games-list.component";
import {MatButton} from "@angular/material/button";
import {MyGameListComponent} from "../my-game-list/my-game-list.component";
import {AuthService} from "../../services/auth.service";

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    ApercuGamesListComponent,
    MatButton,
  ],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent {

  gamesLists : GamesList[] = [];
  connectedUser = this.authService.connectedUser;

  /** Vrai une fois les listes reçues au moins une fois : évite d'afficher « aucune liste » pendant le chargement. */
  loaded = false;
  loadFailed = false;

  /**
   * Chaque `next()` demande un rechargement des listes.
   * switchMap annule la requête précédente si une nouvelle demande arrive avant sa réponse :
   * on n'affiche donc jamais une réponse périmée (ex. les listes de l'invité après une inscription).
   */
  private reloadRequests = new Subject<void>();

  constructor(private authService: AuthService,
              public dialog:MatDialog,
              private snackBar: MatSnackBar,
              private gameApiService: GameApiService) {
    this.reloadRequests.pipe(
      switchMap(() => this.gameApiService.getGamesList().pipe(
        // catchError ici, à l'intérieur : une erreur ne doit pas arrêter définitivement les rechargements
        catchError(() => {
          this.loadFailed = true;
          return EMPTY;
        })
      )),
      takeUntilDestroyed(),
    ).subscribe(gamesLists => {
      this.gamesLists = gamesLists;
      this.loaded = true;
      this.loadFailed = false;
    });

    effect(()=>{
      if(this.connectedUser()){
        this.reload();
      }
      else{
        this.gamesLists = [];
        this.loaded = false;
      }
    });
  }

  reload() {
    this.reloadRequests.next();
  }

  openGamesSearchPopup() {
    const dialogRef = this.dialog.open(GamesSearchPopupComponent,{
      width: '560px',
      autoFocus: 'input', // le curseur arrive dans le premier champ, pas sur la croix de fermeture
      data: {}
    })

    dialogRef.afterClosed().subscribe((result?: GamesSearchPopupResult) => {
      if (!result?.addedGames?.length) {
        return;
      }
      const newList: NewGameList = {name: result.listName, games: result.addedGames};
      this.gameApiService.saveGamesList(newList).subscribe({
        next: () => {
          this.snackBar.open('List created', undefined, {duration: 3000});
          this.reload();
        },
        error: () => this.snackBar.open('Could not create the list. Please try again.', undefined, {duration: 5000}),
      });
    })
  }

  openGamesListPopup(gameList : GamesList){
    const dialogRef = this.dialog.open(MyGameListComponent,{
      width: '560px',
      data: {gamesList : gameList}
    })

    dialogRef.afterClosed().subscribe(() => this.reload())
  }

}
