package com.stars.bigbang.entity;

/** Ce que montre un lien de partage. */
public enum ShareScope {
    /** Tout le gamesWallet : toutes les listes du propriétaire, y compris celles créées après le partage. */
    WALLET,
    /** Une sélection figée de listes (une ou plusieurs). */
    LISTS
}
