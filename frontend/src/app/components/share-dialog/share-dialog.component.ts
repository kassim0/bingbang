import {Component, Inject} from '@angular/core';
import {DOCUMENT} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {GamesList} from '../../models/games.model';
import {ShareLink, ShareScope} from '../../models/share.model';
import {ShareApiService} from '../../services/share-api.service';
import {AuthService} from '../../services/auth.service';

/**
 * Fenêtre de partage : créer un lien en lecture seule (tout le wallet ou des listes choisies),
 * voir ses liens, copier leur adresse et les révoquer.
 */
@Component({
  selector: 'app-share-dialog',
  standalone: true,
  imports: [FormsModule, MatButton, MatIcon],
  templateUrl: './share-dialog.component.html',
  styleUrl: './share-dialog.component.scss',
})
export class ShareDialogComponent {

  gamesLists: GamesList[];

  // --- formulaire de création ---
  scope: ShareScope = 'WALLET';
  /** Identifiants des listes cochées (utilisé seulement pour la portée LISTS). */
  selectedListIds = new Set<number>();
  includeCommentaries = false;

  // --- liens existants ---
  links: ShareLink[] = [];
  loading = true;
  busy = false;
  message = '';
  /** Identifiant du lien dont l'adresse vient d'être copiée, pour afficher « Copied ». */
  copiedLinkId: number | null = null;

  readonly isGuest = this.authService.connectedUser()?.guest ?? false;

  constructor(@Inject(MAT_DIALOG_DATA) data: { gamesLists: GamesList[] },
              private dialogRef: MatDialogRef<ShareDialogComponent>,
              private shareApiService: ShareApiService,
              private authService: AuthService,
              @Inject(DOCUMENT) private document: Document) {
    this.gamesLists = data.gamesLists;
    this.shareApiService.findMine().subscribe({
      next: links => {
        this.links = links;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.message = 'Could not load your share links. Please try again.';
      },
    });
  }

  get canCreate(): boolean {
    return !this.busy && (this.scope === 'WALLET' || this.selectedListIds.size > 0);
  }

  toggleList(listId: number) {
    if (this.selectedListIds.has(listId)) {
      this.selectedListIds.delete(listId);
    } else {
      this.selectedListIds.add(listId);
    }
  }

  create() {
    if (!this.canCreate) {
      return;
    }
    this.busy = true;
    this.message = '';
    this.shareApiService.create({
      scope: this.scope,
      listIds: this.scope === 'LISTS' ? [...this.selectedListIds] : undefined,
      includeCommentaries: this.includeCommentaries,
    }).subscribe({
      next: link => {
        this.busy = false;
        this.links = [link, ...this.links]; // le plus récent en premier, comme le serveur
        this.selectedListIds.clear();
      },
      error: () => {
        this.busy = false;
        this.message = 'Could not create the link. Please try again.';
      },
    });
  }

  revoke(link: ShareLink) {
    this.busy = true;
    this.message = '';
    this.shareApiService.revoke(link.id).subscribe({
      next: () => {
        this.busy = false;
        this.links = this.links.filter(l => l.id !== link.id);
      },
      error: () => {
        this.busy = false;
        this.message = 'Could not revoke the link. Please try again.';
      },
    });
  }

  /** Adresse complète à envoyer : celle du site + /share/ + le token du lien. */
  urlOf(link: ShareLink): string {
    return `${this.document.location.origin}/share/${link.token}`;
  }

  describe(link: ShareLink): string {
    const what = link.scope === 'WALLET'
      ? 'Whole wallet'
      : link.listNames.length > 0 ? link.listNames.join(', ') : 'Lists that no longer exist';
    return link.includeCommentaries ? `${what} · with comments` : what;
  }

  copy(link: ShareLink) {
    // navigator.clipboard n'existe que sur une page sécurisée (https ou localhost)
    this.document.defaultView?.navigator.clipboard?.writeText(this.urlOf(link)).then(
      () => this.copiedLinkId = link.id,
      () => this.message = 'Could not copy automatically: select the address and copy it.',
    );
  }

  close() {
    this.dialogRef.close();
  }
}
