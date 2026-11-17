/** Noms des fonds, identiques à l'enum WalletBackground du backend. */
export type WalletBackground = 'COSMIC' | 'AURORA' | 'SUNSET' | 'FOREST' | 'OCEAN' | 'ARCADE';

export interface Wallet {
  background: WalletBackground;
}

export const DEFAULT_BACKGROUND: WalletBackground = 'COSMIC';

/**
 * Fonds proposés dans le sélecteur. Le dessin de chacun est dans styles.scss
 * (règle body[data-bb-background="..."]) ; `preview` est un aperçu simplifié pour la pastille du menu.
 */
export const WALLET_BACKGROUNDS: { key: WalletBackground; label: string; preview: string }[] = [
  {key: 'COSMIC', label: 'Cosmic', preview: 'linear-gradient(135deg, #2a1f5c, #0d0e1a 60%, #4a1d3d)'},
  {key: 'AURORA', label: 'Aurora', preview: 'linear-gradient(135deg, #0f766e, #0b1220 60%, #3b82f6)'},
  {key: 'SUNSET', label: 'Sunset', preview: 'linear-gradient(135deg, #f97316, #3b1024 60%, #7c3aed)'},
  {key: 'FOREST', label: 'Forest', preview: 'linear-gradient(135deg, #166534, #0c1a12 60%, #65a30d)'},
  {key: 'OCEAN', label: 'Ocean', preview: 'linear-gradient(135deg, #0369a1, #06131f 60%, #22d3ee)'},
  {key: 'ARCADE', label: 'Arcade', preview: 'linear-gradient(135deg, #db2777, #12051f 60%, #facc15)'},
];
