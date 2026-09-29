import {Component, EventEmitter, Input, Output} from '@angular/core';
import {RawgResultsDto} from "../../../models/rawg.models";
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
  rawgGame: RawgResultsDto | undefined;

  @Input()
  rawgGamesSelected : RawgResultsDto[] = [];

  @Output()
  addGame = new EventEmitter<RawgResultsDto>();

  @Output()
  removeGame = new EventEmitter<RawgResultsDto>();

  onAddGame() {
    if (this.rawgGame) {
      if(this.isGameSelected()) {
        this.removeGame.emit(this.rawgGame);
      }
      else{
        this.addGame.emit(this.rawgGame);
      }

    }
  }

  isGameSelected(): boolean {
    return this.rawgGamesSelected.some(g => g.id === this.rawgGame?.id);
  }

}
