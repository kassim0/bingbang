import {Component, OnInit} from '@angular/core';
import {RawgResultsDto} from "../../models/rawg.models";
import {GameApiService} from "../../services/game-api.service";
import {NgFor} from "@angular/common";
import {MatFormField} from "@angular/material/form-field";
import {FormsModule} from "@angular/forms";

@Component({
  selector: 'app-test-api',
  standalone: true,
  imports: [NgFor, MatFormField, FormsModule],
  templateUrl: './test-api.component.html',
  styleUrl: './test-api.component.scss'
})
export class TestApiComponent {

  reponse : RawgResultsDto[] | undefined;
  gameName : string='';

  constructor(private gameApiService: GameApiService) {
  }

  public test(){
    console.log("nom du jeu: "+this.gameName);
    this.gameApiService.searchGames(this.gameName).subscribe((response) => {
      this.reponse = response.results;
      console.log("bouton test appuyé: ",response.results);
    });
  }

}
