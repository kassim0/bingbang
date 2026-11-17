import {Component, EventEmitter, Input, Output} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {HttpErrorResponse} from '@angular/common/http';
import {MatButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {GameApiService} from '../../services/game-api.service';
import {GamesList} from '../../models/games.model';

/**
 * Commentaire d'un jeu dans une liste : l'afficher, l'écrire, le modifier, le supprimer,
 * et le copier vers une autre liste.
 *
 * Chaque action est enregistrée tout de suite sur le serveur (pas besoin du bouton « save » de la liste).
 * Le composant prévient son parent avec (commentaryChange) pour qu'il garde le texte à jour.
 */
@Component({
  selector: 'app-game-commentary',
  standalone: true,
  imports: [FormsModule, MatButton, MatIcon],
  templateUrl: './game-commentary.component.html',
  styleUrl: './game-commentary.component.scss',
})
export class GameCommentaryComponent {

  /** Même limite que le backend (GameCommentary.TEXT_MAX_LENGTH). */
  readonly maxLength = 2000;

  @Input({required: true}) gamesListId!: number;
  @Input({required: true}) gameId!: number;
  @Input() gameName = '';
  @Input() commentary: string | null | undefined;

  /** Émet le nouveau texte, ou null quand le commentaire est supprimé. */
  @Output() commentaryChange = new EventEmitter<string | null>();

  editing = false;
  draft = '';
  busy = false;
  /** Message affiché sous le commentaire (succès d'une copie, erreur). */
  message = '';

  /** Autres listes de l'utilisateur, chargées quand il clique sur « Copy to… » ; null = panneau fermé. */
  copyTargets: GamesList[] | null = null;

  constructor(private gameApiService: GameApiService) {}

  startEditing() {
    this.draft = this.commentary ?? '';
    this.editing = true;
    this.message = '';
    this.copyTargets = null;
  }

  cancelEditing() {
    this.editing = false;
  }

  save() {
    const text = this.draft.trim();
    if (text.length === 0 || this.busy) {
      return;
    }
    this.busy = true;
    this.gameApiService.saveCommentary(this.gamesListId, this.gameId, text).subscribe({
      next: saved => {
        this.busy = false;
        this.editing = false;
        this.commentary = saved.text;
        this.commentaryChange.emit(saved.text);
      },
      error: () => this.fail('Could not save the comment. Please try again.'),
    });
  }

  delete() {
    if (this.busy) {
      return;
    }
    this.busy = true;
    this.gameApiService.deleteCommentary(this.gamesListId, this.gameId).subscribe({
      next: () => {
        this.busy = false;
        this.commentary = null;
        this.copyTargets = null;
        this.commentaryChange.emit(null);
      },
      error: () => this.fail('Could not delete the comment. Please try again.'),
    });
  }

  /** Ouvre (ou referme) le choix de la liste vers laquelle copier le commentaire. */
  toggleCopyTargets() {
    this.message = '';
    if (this.copyTargets !== null) {
      this.copyTargets = null;
      return;
    }
    this.gameApiService.getGamesList().subscribe({
      next: lists => this.copyTargets = lists.filter(list => list.id !== this.gamesListId),
      error: () => this.fail('Could not load your lists. Please try again.'),
    });
  }

  copyTo(target: GamesList, replace = false) {
    this.busy = true;
    this.gameApiService.copyCommentary(this.gamesListId, this.gameId, target.id, replace).subscribe({
      next: () => {
        this.busy = false;
        this.copyTargets = null;
        this.message = `Comment copied to "${target.name}".`;
      },
      error: (error: HttpErrorResponse) => {
        this.busy = false;
        // 409 : la liste cible a déjà un commentaire sur ce jeu. On ne l'écrase qu'avec l'accord de l'utilisateur.
        if (error.status === 409 && !replace) {
          if (this.askConfirmation(`"${target.name}" already has a comment on this game. Replace it?`)) {
            this.copyTo(target, true);
          }
          return;
        }
        this.fail('Could not copy the comment. Please try again.');
      },
    });
  }

  /** Dans une méthode à part pour pouvoir la remplacer dans les tests. */
  askConfirmation(question: string): boolean {
    return window.confirm(question);
  }

  private fail(message: string) {
    this.busy = false;
    this.message = message;
  }
}
