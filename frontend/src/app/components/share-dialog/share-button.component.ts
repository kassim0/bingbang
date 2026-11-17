import {Component, Input} from '@angular/core';
import {MatIconButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {MatDialog} from '@angular/material/dialog';
import {GamesList} from '../../models/games.model';
import {ShareDialogComponent} from './share-dialog.component';

/** Bouton « partager » de la home : ouvre la fenêtre de gestion des liens de partage. */
@Component({
  selector: 'app-share-button',
  standalone: true,
  imports: [MatIconButton, MatIcon],
  template: `
    <button mat-icon-button type="button" data-testid="open-share-dialog"
            aria-label="Share your games wallet" title="Share (read-only link)" (click)="open()">
      <mat-icon aria-hidden="true">share</mat-icon>
    </button>
  `,
  styles: `:host { display: inline-flex; }`,
})
export class ShareButtonComponent {

  @Input() gamesLists: GamesList[] = [];

  constructor(private dialog: MatDialog) {}

  open() {
    this.dialog.open(ShareDialogComponent, {width: '560px', data: {gamesLists: this.gamesLists}});
  }
}
