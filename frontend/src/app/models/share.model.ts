import {GamesList} from './games.model';
import {WalletBackground} from './wallet.model';

/** WALLET : tout le wallet (y compris les listes créées plus tard). LISTS : une sélection figée de listes. */
export type ShareScope = 'WALLET' | 'LISTS';

export interface CreateShareLink {
  scope: ShareScope;
  listIds?: number[];
  includeCommentaries: boolean;
}

/** Un lien de partage, vu par son propriétaire. */
export interface ShareLink {
  id: number;
  token: string;
  scope: ShareScope;
  includeCommentaries: boolean;
  /** Noms des listes du lien qui existent encore (vide pour WALLET). */
  listNames: string[];
  createdAt: string;
}

/** Ce que voit le visiteur d'un lien de partage. */
export interface SharedWallet {
  ownerName: string;
  scope: ShareScope;
  background: WalletBackground;
  lists: GamesList[];
}
