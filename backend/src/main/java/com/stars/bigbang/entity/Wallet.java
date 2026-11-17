package com.stars.bigbang.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * Réglages du gamesWallet d'un utilisateur (un seul par utilisateur). Pour l'instant : le fond de sa home.
 * C'est une table à part, et non une colonne de users, pour ne pas mélanger les réglages d'affichage
 * avec les données de connexion.
 */
@Entity
@Table(name = "wallet")
@Getter
@Setter
@NoArgsConstructor
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ON DELETE CASCADE : si le compte est supprimé, la base supprime son wallet avec lui. */
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    /** Enregistré sous forme de texte ("AURORA") : lisible en base, et insensible à l'ordre de l'enum. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WalletBackground background = WalletBackground.DEFAULT;

    public Wallet(User user) {
        this.user = user;
    }
}
