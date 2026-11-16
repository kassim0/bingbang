import {Component, EventEmitter, Input, Output} from '@angular/core';
import {MatFormField, MatHint, MatLabel, MatSuffix} from "@angular/material/form-field";
import {MatInput} from "@angular/material/input";
import {MatIcon} from "@angular/material/icon";
import {MatIconButton} from "@angular/material/button";
import {FormsModule} from "@angular/forms";

@Component({
  selector: 'app-search-bar',
  standalone: true,
  imports: [
    MatFormField,
    MatInput,
    MatIcon,
    MatIconButton,
    MatLabel,
    MatHint,
    MatSuffix,
    FormsModule
  ],
  templateUrl: './search-bar.component.html',
  styleUrl: './search-bar.component.scss'
})
export class SearchBarComponent {
  /**
   * Émet le texte à rechercher (sans espaces autour) quand l'utilisateur valide.
   * Ne pas le nommer `search` : un <input type="search"> émet déjà un événement natif `search`,
   * que le parent recevrait aussi (avec un objet Event au lieu du texte).
   */
  @Output()
  searchSubmitted :  EventEmitter<string> = new EventEmitter<string>();

  inputResult: string='';

  @Input()
  label: string = '';

  @Input()
  placeholder: string = '';

  sendInput() {
    const text = this.inputResult.trim();
    // une recherche vide ne trouverait rien : inutile d'appeler le serveur
    if (text.length > 0) {
      this.searchSubmitted.emit(text);
    }
  }
}
