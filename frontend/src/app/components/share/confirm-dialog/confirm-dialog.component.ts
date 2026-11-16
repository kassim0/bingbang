import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogModule} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';

export interface ConfirmDialogData {
  title: string;
  message: string;
  /** Texte du bouton qui confirme l'action, ex. « Delete ». */
  confirmLabel: string;
  /** Texte du bouton qui annule. « Cancel » par défaut. */
  cancelLabel?: string;
}

/**
 * Petite popup « Êtes-vous sûr ? ». Se ferme avec `true` si l'utilisateur confirme,
 * `false` (ou rien, s'il clique à côté) sinon.
 *
 * Utilisation :
 *   this.dialog.open(ConfirmDialogComponent, {data: {title, message, confirmLabel}})
 *     .afterClosed().subscribe(confirmed => { if (confirmed) { ... } });
 */
@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [MatDialogModule, MatButtonModule],
  template: `
    <h2 mat-dialog-title data-testid="confirm-title">{{ data.title }}</h2>
    <mat-dialog-content>{{ data.message }}</mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button type="button" data-testid="confirm-cancel" [mat-dialog-close]="false" cdkFocusInitial>
        {{ data.cancelLabel ?? 'Cancel' }}
      </button>
      <button mat-button type="button" class="confirm" data-testid="confirm-ok" [mat-dialog-close]="true">
        {{ data.confirmLabel }}
      </button>
    </mat-dialog-actions>
  `,
  styles: `
    .confirm {
      --mdc-text-button-label-text-color: var(--bb-danger);
      border: 1px solid var(--bb-danger);
    }
  `,
})
export class ConfirmDialogComponent {
  constructor(@Inject(MAT_DIALOG_DATA) public data: ConfirmDialogData) {}
}
