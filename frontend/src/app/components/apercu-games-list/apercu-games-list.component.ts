import {Component, EventEmitter, Input, Output} from '@angular/core';
import {GamesList, GamesListEntry} from "../../models/games.model";
import {GameCoverComponent} from "../share/game-cover/game-cover.component";

/** Carte d'aperçu d'une liste sur la home : son nom et ses premiers jeux. */
@Component({
  selector: 'app-apercu-games-list',
  standalone: true,
  imports: [GameCoverComponent],
  templateUrl: './apercu-games-list.component.html',
  styleUrl: './apercu-games-list.component.scss'
})
export class ApercuGamesListComponent {

  /** Nombre de jeux montrés dans l'aperçu (règle du wiki : les 3 premiers). */
  static readonly PREVIEW_SIZE = 3;

  @Input()
  gamesList!: GamesList;

  /** Émis quand l'utilisateur veut ouvrir la liste (clic, ou Entrée/Espace au clavier). */
  @Output()
  open = new EventEmitter<void>();

  get previewGames(): GamesListEntry[] {
    return this.gamesList.games.slice(0, ApercuGamesListComponent.PREVIEW_SIZE);
  }

}
