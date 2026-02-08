import {Component, Input} from '@angular/core';
import {MatCardModule} from '@angular/material/card';
import {NgIf} from '@angular/common';
import {RawgResultsDto} from '../../models/game.model';

@Component({
  selector: 'app-game-card',
  standalone: true,
  imports: [
    MatCardModule,
    NgIf
  ],
  templateUrl: './game-card.component.html',
  styleUrl: './game-card.component.scss'
})
export class GameCardComponent {

  @Input()
  game: RawgResultsDto | undefined;

  getDefaultImage(): string {
    return 'https://via.placeholder.com/300x200?text=No+Image';
  }
}
