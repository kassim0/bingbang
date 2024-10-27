import {Component, EventEmitter, Output} from '@angular/core';
import {MatFormField, MatLabel, MatSuffix} from "@angular/material/form-field";
import {MatInput} from "@angular/material/input";
import {MatIcon} from "@angular/material/icon";
import {FormsModule} from "@angular/forms";

@Component({
  selector: 'app-search-bar',
  standalone: true,
  imports: [
    MatFormField,
    MatInput,
    MatIcon,
    MatLabel,
    MatSuffix,
    FormsModule
  ],
  templateUrl: './search-bar.component.html',
  styleUrl: './search-bar.component.scss'
})
export class SearchBarComponent {
  @Output()
  dataToParent :  EventEmitter<string> = new EventEmitter<string>();

  inputResult: string='';

  sendInput() {
    this.dataToParent.emit(this.inputResult);
  }
}
