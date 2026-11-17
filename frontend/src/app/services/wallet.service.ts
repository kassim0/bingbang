import {DOCUMENT} from '@angular/common';
import {HttpClient} from '@angular/common/http';
import {effect, Inject, Injectable, signal} from '@angular/core';
import {AuthService} from './auth.service';
import {DEFAULT_BACKGROUND, Wallet, WalletBackground} from '../models/wallet.model';

/**
 * Réglages du gamesWallet de l'utilisateur connecté : pour l'instant, le fond de sa home.
 *
 * Le fond choisi est écrit dans un attribut de <body> (data-bb-background="AURORA") ;
 * c'est styles.scss qui dessine le fond correspondant.
 */
@Injectable({providedIn: 'root'})
export class WalletService {

  private base = '/api/wallet';

  background = signal<WalletBackground>(DEFAULT_BACKGROUND);

  /**
   * Fond affiché temporairement à la place de celui de l'utilisateur, ou null.
   * Utilisé par la page de partage, qui montre le wallet de quelqu'un d'autre avec SON fond.
   */
  previewBackground = signal<WalletBackground | null>(null);

  constructor(private http: HttpClient,
              authService: AuthService,
              @Inject(DOCUMENT) private document: Document) {
    // À chaque changement d'utilisateur (invité, connexion, déconnexion), on recharge son fond.
    effect(() => {
      if (authService.connectedUser()) {
        this.load();
      } else {
        this.background.set(DEFAULT_BACKGROUND);
      }
    }, {allowSignalWrites: true});

    // À chaque changement de fond, on met à jour l'attribut de <body>.
    effect(() => this.document.body.setAttribute('data-bb-background', this.previewBackground() ?? this.background()));
  }

  private load() {
    this.http.get<Wallet>(this.base).subscribe({
      next: wallet => this.background.set(wallet.background),
      // en cas d'erreur on garde le fond actuel : ce n'est pas bloquant
      error: () => {},
    });
  }

  /** Applique le fond tout de suite, puis l'enregistre ; en cas d'échec, on revient au fond précédent. */
  setBackground(background: WalletBackground) {
    const previous = this.background();
    this.background.set(background);
    this.http.put<Wallet>(`${this.base}/background`, {background}).subscribe({
      next: wallet => this.background.set(wallet.background),
      error: () => this.background.set(previous),
    });
  }
}
