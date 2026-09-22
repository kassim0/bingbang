import {Component, Input} from '@angular/core';
import {GamesList} from "../../models/games.model";
import {NgIf} from "@angular/common";

@Component({
  selector: 'app-apercu-games-list',
  standalone: true,
  imports: [
    NgIf
  ],
  templateUrl: './apercu-games-list.component.html',
  styleUrl: './apercu-games-list.component.scss'
})
export class ApercuGamesListComponent {

  @Input()
  gamesList!: GamesList;


}
