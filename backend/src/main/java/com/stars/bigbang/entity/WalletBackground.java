package com.stars.bigbang.entity;

/**
 * Fonds proposés pour la home. Ce sont des fonds prédéfinis (pas d'image envoyée par l'utilisateur) :
 * rien à stocker ni à modérer. Le dessin de chaque fond est côté frontend (styles.scss) ;
 * le backend ne retient que le nom choisi.
 * Pour ajouter un fond : une valeur ici, une entrée dans wallet-backgrounds.ts et une règle CSS.
 */
public enum WalletBackground {
    COSMIC,
    AURORA,
    SUNSET,
    FOREST,
    OCEAN,
    ARCADE;

    public static final WalletBackground DEFAULT = COSMIC;
}
