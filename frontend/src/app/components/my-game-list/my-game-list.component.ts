import {Component, DestroyRef, Inject} from '@angular/core';
import {takeUntilDestroyed} from "@angular/core/rxjs-interop";
import {Game, GamesList, GamesSearchPopupResult, UpdateGamesList} from "../../models/games.model";
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {MatIcon} from "@angular/material/icon";
import {MatButton, MatIconButton} from "@angular/material/button";
import {MatSnackBar} from "@angular/material/snack-bar";
import {GameApiService} from "../../services/game-api.service";
import {FormsModule} from "@angular/forms";
import {MatFormField, MatLabel} from "@angular/material/form-field";
import {MatInput} from "@angular/material/input";
import {switchMap} from "rxjs";
import {GamesSearchPopupComponent} from "../games-search-popup/games-search-popup.component";
import {GameCoverComponent} from "../share/game-cover/game-cover.component";
import {ConfirmDialogComponent} from "../share/confirm-dialog/confirm-dialog.component";

/**
 * Popup d'une liste : renommer, retirer des jeux (appliqué au clic sur « Save »),
 * ajouter des jeux (enregistré tout de suite) et supprimer la liste.
 */
@Component({
  selector: 'app-my-game-list',
  standalone: true,
  imports: [
    MatIcon,
    MatIconButton,
    MatButton,
    FormsModule,
    MatFormField,
    MatLabel,
    MatInput,
    GameCoverComponent,
  ],
  templateUrl: './my-game-list.component.html',
  styleUrl: './my-game-list.component.scss'
})
export class MyGameListComponent {

  gamesList : GamesList;
  /** Jeux marqués « à retirer » : ils ne sont réellement retirés qu'au clic sur « Save ». */
  selectedGames : Game[] = [];
  editedName : string;
  saving = false;

  constructor(@Inject(MAT_DIALOG_DATA) public data: { gamesList: GamesList },
              private gameApiService: GameApiService,
              private dialog: MatDialog,
              private snackBar: MatSnackBar,
              private dialogRef: MatDialogRef<MyGameListComponent>,
              destroyRef: DestroyRef) {
    this.gamesList = data.gamesList;
    this.editedName = data.gamesList.name;

    // Cliquer à côté ou appuyer sur Échap passe par close() : on prévient s'il reste des changements non enregistrés.
    this.dialogRef.disableClose = true;
    this.dialogRef.backdropClick()?.pipe(takeUntilDestroyed(destroyRef)).subscribe(() => this.close());
    this.dialogRef.keydownEvents()?.pipe(takeUntilDestroyed(destroyRef)).subscribe(event => {
      if (event.key === 'Escape') {
        this.close();
      }
    });
  }

  private get trimmedName(): string {
    return this.editedName.trim();
  }

  get nameChanged(): boolean {
    return this.trimmedName !== this.gamesList.name;
  }

  /** Vrai s'il y a quelque chose à enregistrer : un nouveau nom ou des jeux à retirer. */
  get hasChanges(): boolean {
    return this.nameChanged || this.selectedGames.length > 0;
  }

  /** Un nom vide est refusé par le serveur : on bloque « Save » avant d'envoyer. */
  get canSave(): boolean {
    return this.hasChanges && this.trimmedName.length > 0 && !this.saving;
  }

  openGamesSearchPopup() {
    const dialogRef = this.dialog.open(GamesSearchPopupComponent, {
      width: '560px',
      autoFocus: 'input', // le curseur arrive dans le premier champ, pas sur la croix de fermeture
      data: {gamesList: this.gamesList}
    });

    dialogRef.afterClosed().subscribe((result?: GamesSearchPopupResult) => {
      if (!result?.addedGames?.length) {
        return;
      }
      const updateGamesList: UpdateGamesList = {
        gamesListId: this.gamesList.id,
        newGames: result.addedGames,
        removeGameId: null,
        newName: null,
      };
      this.gameApiService.updateGamesList(updateGamesList).pipe(
        switchMap(() => this.gameApiService.getGamesList())
      ).subscribe({
        next: gamesLists => {
          this.refreshFrom(gamesLists);
          this.notify(result.addedGames.length === 1 ? 'Game added' : 'Games added');
        },
        error: () => this.notify('Could not add the games. Please try again.'),
      });
    });
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

  /** Ferme la popup ; demande confirmation s'il reste des changements non enregistrés. */
  close() {
    if (!this.hasChanges) {
      this.dialogRef.close();
      return;
    }
    this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Discard your changes?',
        message: 'Your changes to this list are not saved yet.',
        confirmLabel: 'Discard',
        cancelLabel: 'Keep editing',
      },
    }).afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.dialogRef.close();
      }
    });
  }

  saveModifGamesList(){
    if (!this.canSave) {
      return;
    }
    const updateGamesList: UpdateGamesList = {
      gamesListId: this.gamesList.id,
      newGames: null,
      removeGameId: this.selectedGames.map(g => g.id),
      newName: this.nameChanged ? this.trimmedName : null,
    };
    this.saving = true;
    // switchMap : une fois la modification enregistrée, on enchaîne sur le rechargement des listes
    this.gameApiService.updateGamesList(updateGamesList).pipe(
      switchMap(() => this.gameApiService.getGamesList())
    ).subscribe({
      next: gamesLists => {
        this.saving = false;
        this.selectedGames = [];
        this.refreshFrom(gamesLists);
        this.notify('List saved');
      },
      error: () => {
        this.saving = false;
        this.notify('Could not save the list. Please try again.');
      },
    });
  }

  deleteGamesList(){
    this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: `Delete "${this.gamesList.name}"?`,
        message: 'The list and its games will be removed for good. This cannot be undone.',
        confirmLabel: 'Delete list',
      },
    }).afterClosed().subscribe(confirmed => {
      if (!confirmed) {
        return;
      }
      this.gameApiService.deleteGamesList(this.gamesList.id).subscribe({
        next: () => {
          this.notify('List deleted');
          this.dialogRef.close();
        },
        error: () => this.notify('Could not delete the list. Please try again.'),
      });
    });
  }

  /**
   * Remplace la liste affichée par sa version à jour, telle que le serveur vient de la renvoyer.
   * Un nom en cours de modification (pas encore enregistré) est conservé dans le champ :
   * ajouter un jeu ne doit pas effacer ce que l'utilisateur est en train de taper.
   */
  private refreshFrom(gamesLists: GamesList[]) {
    const refreshed = gamesLists.find(gl => gl.id === this.gamesList.id);
    if (!refreshed) {
      return;
    }
    const pendingName = this.nameChanged ? this.editedName : null;
    this.gamesList = refreshed;
    this.editedName = pendingName ?? refreshed.name;
  }

  private notify(message: string) {
    this.snackBar.open(message, undefined, {duration: 3000});
  }

}
