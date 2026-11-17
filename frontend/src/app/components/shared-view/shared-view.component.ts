import {Component, effect, OnDestroy} from '@angular/core';
import {ActivatedRoute, RouterLink} from '@angular/router';
import {AuthService} from '../../services/auth.service';
import {ShareApiService} from '../../services/share-api.service';
import {WalletService} from '../../services/wallet.service';
import {SharedWallet} from '../../models/share.model';

/**
 * Page d'un lien de partage (/share/:token) : le wallet ou les listes de quelqu'un d'autre,
 * en lecture seule. Il n'y a volontairement aucun bouton de modification ici.
 */
@Component({
  selector: 'app-shared-view',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './shared-view.component.html',
  styleUrl: './shared-view.component.scss',
})
export class SharedViewComponent implements OnDestroy {

  shared: SharedWallet | null = null;
  /** 'loading' tant qu'on attend, 'ready' quand le contenu est là, 'not-found' si le lien ne mène à rien. */
  state: 'loading' | 'ready' | 'not-found' | 'error' = 'loading';

  private requested = false;

  constructor(route: ActivatedRoute,
              authService: AuthService,
              private shareApiService: ShareApiService,
              private walletService: WalletService) {
    const token = route.snapshot.paramMap.get('token') ?? '';
    // La route de lecture demande un utilisateur connecté (un invité suffit) : on attend que
    // AuthService ait fini de connecter le visiteur avant d'appeler le serveur, une seule fois.
    effect(() => {
      if (authService.connectedUser() && !this.requested) {
        this.requested = true;
        this.load(token);
      }
    });
  }

  private load(token: string) {
    this.shareApiService.view(token).subscribe({
      next: shared => {
        this.shared = shared;
        this.state = 'ready';
        // on montre le wallet avec le fond choisi par son propriétaire
        this.walletService.previewBackground.set(shared.background);
      },
      error: error => this.state = error.status === 404 ? 'not-found' : 'error',
    });
  }

  ngOnDestroy() {
    // en quittant la page, le visiteur retrouve son propre fond
    this.walletService.previewBackground.set(null);
  }
}
