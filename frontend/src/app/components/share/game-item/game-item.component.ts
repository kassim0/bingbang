import {Component, EventEmitter, Input, Output} from '@angular/core';
import {RawgResultsDto} from "../../../../../.src/app/openApi";
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
  game: RawgResultsDto | undefined;

  @Output()
  addGame = new EventEmitter<RawgResultsDto>();

  onAddGame() {
    if (this.game) {
      this.addGame.emit(this.game);
    }
  }

}
