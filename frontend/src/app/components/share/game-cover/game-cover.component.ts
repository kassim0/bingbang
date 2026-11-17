import {Component, Input} from '@angular/core';

/**
 * Image de remplacement (une manette stylisée), écrite directement dans le code sous forme d'URL "data:" :
 * aucun fichier à télécharger, elle s'affiche donc même sans réseau.
 */
const PLACEHOLDER = 'data:image/svg+xml,' + encodeURIComponent(
  `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 48 48">
     <rect width="48" height="48" fill="#20223a"/>
     <rect x="10" y="18" width="28" height="14" rx="7" fill="none" stroke="#9b9bc0" stroke-width="2"/>
     <path d="M17 22v6M14 25h6" stroke="#9b9bc0" stroke-width="2" stroke-linecap="round"/>
     <circle cx="30" cy="23" r="1.6" fill="#9b9bc0"/><circle cx="33" cy="27" r="1.6" fill="#9b9bc0"/>
   </svg>`);

/**
 * Jaquette d'un jeu en vignette carrée. Si le jeu n'a pas d'image, ou si elle ne se charge pas,
 * une image de remplacement s'affiche (au lieu de l'icône « image cassée » du navigateur).
 *
 * L'image est décorative : le nom du jeu est toujours écrit à côté, donc alt="" pour qu'un lecteur
 * d'écran ne lise pas deux fois la même chose.
 */
@Component({
  selector: 'app-game-cover',
  standalone: true,
  template: `
    <img [src]="displayedUrl" alt="" loading="lazy" decoding="async"
         [attr.width]="size" [attr.height]="size" (error)="onLoadError()">
  `,
  styles: `
    :host {
      display: inline-flex;
      flex-shrink: 0;
    }

    img {
      border-radius: 8px;
      object-fit: cover;
    }
  `,
})
export class GameCoverComponent {

  private _url: string | null | undefined;
  private loadFailed = false;

  @Input()
  set url(value: string | null | undefined) {
    this._url = value;
    this.loadFailed = false; // nouvelle image : on retente
  }

  /** Côté de la vignette, en pixels. */
  @Input() size = 50;

  get displayedUrl(): string {
    return this.loadFailed ? PLACEHOLDER : this.thumbnailUrl ?? PLACEHOLDER;
  }

  /**
   * IGDB sert la même image en plusieurs tailles, la taille fait partie de l'URL.
   * Pour une vignette de 35 à 50 px, "t_cover_small" (90×128) suffit largement : on évite de
   * télécharger "t_cover_big" (264×374), environ huit fois plus lourde.
   */
  get thumbnailUrl(): string | null {
    return this._url ? this._url.replace('/t_cover_big/', '/t_cover_small/') : null;
  }

  onLoadError() {
    this.loadFailed = true;
  }
}
