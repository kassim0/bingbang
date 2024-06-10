import {Component, OnInit} from '@angular/core';
import {JeuxRestControllerService} from "../../openApi";

@Component({
  selector: 'app-test-api',
  standalone: true,
  imports: [],
  templateUrl: './test-api.component.html',
  styleUrl: './test-api.component.scss'
})
export class TestApiComponent implements OnInit{

  constructor(private jeuxRestControllerService: JeuxRestControllerService) {
  }

  ngOnInit() {
    this.jeuxRestControllerService.getJeuxZelda().subscribe((response)=> {
      next: console.log("ngONinit:", response.results);
    })
  }

  public test(){
    this.jeuxRestControllerService.getJeuxZelda().subscribe((response)=> {
      console.log("bouton test appuyé: ",response.results);
    })
  }

}
