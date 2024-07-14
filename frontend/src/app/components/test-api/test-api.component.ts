import {Component, OnInit} from '@angular/core';
import {JeuxRestControllerService, RawgResponseDto,RawgResultsDto} from "../../openApi";
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

  constructor(private jeuxRestControllerService: JeuxRestControllerService) {
  }

  public test(){
    console.log("nom du jeu: "+this.gameName);
    this.jeuxRestControllerService.getJeuxByName(this.gameName).subscribe((response)=> {
      this.reponse = response.results;
      console.log("bouton test appuyé: ",response.results);
    })
  }

}
