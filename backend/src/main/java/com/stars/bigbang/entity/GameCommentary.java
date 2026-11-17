package com.stars.bigbang.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Commentaire d'un utilisateur sur un jeu, dans une liste donnée.
 * <p>
 * Il est volontairement stocké à part de GamesListEntry (la ligne « ce jeu est dans cette liste ») :
 * retirer le jeu de la liste supprime l'entrée mais pas le commentaire, qui réapparaît si le jeu est rajouté.
 * La contrainte unique garantit un seul commentaire par jeu et par liste ; le même jeu dans une autre
 * liste a son propre commentaire.
 */
@Entity
@Table(name = "game_commentary",
        uniqueConstraints = @UniqueConstraint(name = "uk_commentary_list_game", columnNames = {"games_list_id", "game_id"}))
@Getter
@Setter
@NoArgsConstructor
public class GameCommentary {

    public static final int TEXT_MAX_LENGTH = 2000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ON DELETE CASCADE : quand la liste est supprimée, la base supprime ses commentaires avec elle. */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "games_list_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private GamesList gamesList;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(nullable = false, length = TEXT_MAX_LENGTH)
    private String text;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public GameCommentary(GamesList gamesList, Game game, String text) {
        this.gamesList = gamesList;
        this.game = game;
        this.text = text;
    }
}
