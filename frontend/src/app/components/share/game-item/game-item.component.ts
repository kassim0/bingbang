import {Component, EventEmitter, Input, Output} from '@angular/core';
import {GameSearchResult} from "../../../models/game-search.models";
import {MatIconButton} from "@angular/material/button";
import {MatIcon} from "@angular/material/icon";

@Component({
  selector: 'app-game-item',
  standalone: true,
  imports: [MatIconButton, MatIcon],
  templateUrl: './game-item.component.html',
  styleUrl: './game-item.component.scss'
})
export class GameItemComponent {

  @Input()
  game: GameSearchResult | undefined;

  @Input()
  gamesSelected : GameSearchResult[] = [];

  @Output()
  addGame = new EventEmitter<GameSearchResult>();

  @Output()
  removeGame = new EventEmitter<GameSearchResult>();

  onAddGame() {
    if (this.game) {
      if(this.isGameSelected()) {
        this.removeGame.emit(this.game);
      }
      else{
        this.addGame.emit(this.game);
      }

    }
  }

  isGameSelected(): boolean {
    return this.gamesSelected.some(g => g.source === this.game?.source && g.externalId === this.game?.externalId);
  }

}
