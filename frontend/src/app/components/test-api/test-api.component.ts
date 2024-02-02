import { Component } from '@angular/core';

@Component({
  selector: 'app-test-api',
  standalone: true,
  imports: [],
  templateUrl: './test-api.component.html',
  styleUrl: './test-api.component.scss'
})
export class TestApiComponent {

  public test(){
    console.log("bouton appuyé");
  }

}
