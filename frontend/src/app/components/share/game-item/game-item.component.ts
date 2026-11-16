import {Component, EventEmitter, Input, Output} from '@angular/core';
import {GameSearchResult} from "../../../models/game-search.models";
import {MatIconButton} from "@angular/material/button";
import {MatIcon} from "@angular/material/icon";
import {GameCoverComponent} from "../game-cover/game-cover.component";

/** Une ligne de résultat de recherche, avec son bouton pour sélectionner / désélectionner le jeu. */
@Component({
  selector: 'app-game-item',
  standalone: true,
  imports: [MatIconButton, MatIcon, GameCoverComponent],
  templateUrl: './game-item.component.html',
  styleUrl: './game-item.component.scss'
})
export class GameItemComponent {

  @Input()
  game: GameSearchResult | undefined;

  @Input()
  gamesSelected : GameSearchResult[] = [];

  /** Vrai si le jeu est déjà dans la liste qu'on est en train de compléter : on ne peut pas l'ajouter deux fois. */
  @Input()
  alreadyInList = false;

  @Output()
  addGame = new EventEmitter<GameSearchResult>();

  @Output()
  removeGame = new EventEmitter<GameSearchResult>();

  onAddGame() {
    if (!this.game || this.alreadyInList) {
      return;
    }
    if (this.isGameSelected()) {
      this.removeGame.emit(this.game);
    } else {
      this.addGame.emit(this.game);
    }
  }

  isGameSelected(): boolean {
    return this.gamesSelected.some(g => g.source === this.game?.source && g.externalId === this.game?.externalId);
  }

  /** Année de sortie, pour distinguer deux jeux du même nom (ex. un jeu et son remake). */
  get releaseYear(): string | null {
    return this.game?.releaseDate ? this.game.releaseDate.slice(0, 4) : null;
  }

  get icon(): string {
    if (this.alreadyInList) {
      return 'playlist_add_check';
    }
    return this.isGameSelected() ? 'check_circle' : 'add';
  }

  /** Texte lu par un lecteur d'écran à la place de l'icône. */
  get buttonLabel(): string {
    const name = this.game?.name ?? '';
    if (this.alreadyInList) {
      return `${name} is already in this list`;
    }
    return this.isGameSelected() ? `Remove ${name} from the selection` : `Add ${name}`;
  }

}
