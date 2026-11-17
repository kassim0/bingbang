import {Component, DestroyRef, Inject} from '@angular/core';
import {takeUntilDestroyed} from "@angular/core/rxjs-interop";
import {MatButtonModule} from "@angular/material/button";
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {FormsModule} from "@angular/forms";
import {SearchBarComponent} from "../share/search-bar/search-bar.component";
import {GameSearchResult} from "../../models/game-search.models";
import {GamesList, GamesSearchPopupResult} from "../../models/games.model";
import {GameItemComponent} from "../share/game-item/game-item.component";
import {GameApiService} from "../../services/game-api.service";
import {MatIcon} from "@angular/material/icon";
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";
import {ConfirmDialogComponent} from "../share/confirm-dialog/confirm-dialog.component";

/**
 * Popup de recherche de jeux. Elle sert à deux choses :
 * - créer une nouvelle liste (pas de `data.gamesList`) : on saisit un nom et on choisit des jeux ;
 * - ajouter des jeux à une liste existante (`data.gamesList` fourni).
 *
 * Elle ne parle pas au serveur pour enregistrer : elle se ferme en renvoyant la sélection
 * (GamesSearchPopupResult) au composant qui l'a ouverte, ou rien si l'utilisateur annule.
 */
@Component({
  selector: 'app-games-search-popup',
  standalone: true,
  imports: [
    MatFormFieldModule,
    MatInputModule,
    FormsModule,
    MatButtonModule,
    SearchBarComponent, GameItemComponent, MatIcon, MatProgressSpinnerModule,
  ],
  templateUrl: './games-search-popup.component.html',
  styleUrl: './games-search-popup.component.scss'
})
export class GamesSearchPopupComponent {

  /** undefined tant qu'aucune recherche n'a abouti (à ne pas confondre avec « aucun résultat » : tableau vide). */
  results : GameSearchResult[] | undefined;
  addedGames: GameSearchResult[] = [];
  listNameInput : string = "";
  isSearching : boolean = false;
  searchFailed : boolean = false;
  searchedText : string = '';

  /** Liste cible quand la popup sert à ajouter des jeux à une GamesList existante. */
  gamesList : GamesList | null;

  constructor(
    public dialogRef: MatDialogRef<GamesSearchPopupComponent>,
    private gameApiService: GameApiService,
    private dialog: MatDialog,
    destroyRef: DestroyRef,
    @Inject(MAT_DIALOG_DATA) public data: { gamesList?: GamesList }) {
    this.gamesList = data?.gamesList ?? null;

    // Cliquer à côté de la popup ou appuyer sur Échap passe par cancel(), comme la croix :
    // on ne perd donc jamais une sélection sans avoir été prévenu.
    this.dialogRef.disableClose = true;
    this.dialogRef.backdropClick()?.pipe(takeUntilDestroyed(destroyRef)).subscribe(() => this.cancel());
    this.dialogRef.keydownEvents()?.pipe(takeUntilDestroyed(destroyRef)).subscribe(event => {
      if (event.key === 'Escape') {
        this.cancel();
      }
    });
  }

  get title(): string {
    return this.gamesList ? `Add games to "${this.gamesList.name}"` : 'New games list';
  }

  /** Texte du bouton de validation : dit ce qui va se passer, avec le nombre de jeux choisis. */
  get confirmLabel(): string {
    const count = this.addedGames.length;
    const games = count === 1 ? '1 game' : `${count} games`;
    if (this.gamesList) {
      return count === 0 ? 'Add games' : `Add ${games}`;
    }
    return count === 0 ? 'Create list' : `Create list with ${games}`;
  }

  /** Valide : ferme la popup en renvoyant la sélection. */
  confirm() {
    if (this.addedGames.length === 0) {
      return;
    }
    const result: GamesSearchPopupResult = {addedGames: this.addedGames, listName: this.listNameInput.trim()};
    this.dialogRef.close(result);
  }

  /** Annule : ferme sans rien renvoyer. Demande confirmation si des jeux étaient sélectionnés. */
  cancel() {
    if (this.addedGames.length === 0) {
      this.dialogRef.close();
      return;
    }
    const count = this.addedGames.length;
    this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Discard your selection?',
        message: `You selected ${count === 1 ? '1 game' : count + ' games'}. Closing now will not save ${count === 1 ? 'it' : 'them'}.`,
        confirmLabel: 'Discard',
        cancelLabel: 'Keep selecting',
      },
    }).afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.dialogRef.close();
      }
    });
  }

  receiveData(searchText: string) {
    this.searchedText = searchText;
    this.isSearching = true;
    this.searchFailed = false;
    this.gameApiService.searchGames(searchText).subscribe({
      next: (results) => {
        this.results = results;
        this.isSearching = false;
      },
      error: () => {
        this.results = undefined;
        this.searchFailed = true;
        this.isSearching = false;
      }
    });
  }

  onAddGame(game: GameSearchResult){
    this.addedGames.push(game);
  }

  onRemoveGame(game: GameSearchResult){
    this.addedGames = this.addedGames.filter(g=>g.source!==game.source || g.externalId!==game.externalId);
  }

  /** Vrai si ce résultat de recherche correspond à un jeu déjà présent dans la liste qu'on complète. */
  isAlreadyInList(game: GameSearchResult): boolean {
    if (!this.gamesList) {
      return false;
    }
    return this.gamesList.games.some(entry =>
      game.source === 'IGDB' ? entry.game.igdbId === game.externalId : entry.game.rawgId === game.externalId);
  }

}
