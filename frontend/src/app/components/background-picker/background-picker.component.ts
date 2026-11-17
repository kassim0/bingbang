import {Component} from '@angular/core';
import {MatIconButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {MatMenu, MatMenuItem, MatMenuTrigger} from '@angular/material/menu';
import {WalletService} from '../../services/wallet.service';
import {WALLET_BACKGROUNDS, WalletBackground} from '../../models/wallet.model';

/** Bouton « palette » qui ouvre le choix du fond de la home. */
@Component({
  selector: 'app-background-picker',
  standalone: true,
  imports: [MatIconButton, MatIcon, MatMenu, MatMenuItem, MatMenuTrigger],
  template: `
    <button mat-icon-button type="button" data-testid="background-picker-trigger"
            aria-label="Choose the background of your games wallet" title="Choose your background"
            [matMenuTriggerFor]="backgroundMenu">
      <mat-icon aria-hidden="true">palette</mat-icon>
    </button>
    <mat-menu #backgroundMenu>
      @for (option of backgrounds; track option.key) {
        <button mat-menu-item type="button" [attr.data-testid]="'background-option-' + option.key"
                [attr.aria-current]="option.key === current() ? 'true' : null" (click)="choose(option.key)">
          <span class="swatch" [style.background]="option.preview"></span>
          {{ option.label }}
          @if (option.key === current()) {
            <mat-icon class="selected" aria-hidden="true">check</mat-icon>
          }
        </button>
      }
    </mat-menu>
  `,
  styles: `
    :host {
      display: flex;
      justify-content: flex-end;
      max-width: 1100px;
      margin: -32px auto 8px;
    }

    .swatch {
      display: inline-block;
      width: 28px;
      height: 28px;
      margin-right: 12px;
      border-radius: 8px;
      border: 1px solid rgba(255, 255, 255, 0.25);
      vertical-align: middle;
    }

    .selected {
      margin: 0 0 0 12px;
    }
  `,
})
export class BackgroundPickerComponent {

  readonly backgrounds = WALLET_BACKGROUNDS;
  readonly current = this.walletService.background;

  constructor(private walletService: WalletService) {}

  choose(background: WalletBackground) {
    this.walletService.setBackground(background);
  }
}
