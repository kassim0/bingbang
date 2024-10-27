import {Component, Input} from '@angular/core';
import {RawgResultsDto} from "../../../../../.src/app/openApi";

@Component({
  selector: 'app-game-item',
  standalone: true,
  imports: [],
  templateUrl: './game-item.component.html',
  styleUrl: './game-item.component.scss'
})
export class GameItemComponent {

  @Input()
  game: RawgResultsDto | undefined;

}
