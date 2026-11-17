package com.stars.bigbang.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Lien de partage en lecture seule. Celui qui connaît le `token` peut voir le wallet (ou les listes choisies)
 * du propriétaire, sans pouvoir rien modifier. Révoquer un lien = supprimer cette ligne.
 */
@Entity
@Table(name = "share_link")
@Getter
@Setter
@NoArgsConstructor
public class ShareLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Partie secrète de l'URL : longue et aléatoire, donc impossible à deviner. */
    @Column(nullable = false, unique = true, length = 64)
    private String token;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShareScope scope;

    /** Les commentaires peuvent être personnels : ils ne sont montrés que si le propriétaire l'a demandé. */
    @Column(nullable = false)
    private boolean includeCommentaries;

    /**
     * Identifiants des listes partagées (portée LISTS seulement), enregistrés dans une colonne texte
     * ("12,15,20", voir LongSetConverter). Ce sont de simples nombres, sans clé étrangère vers games_list :
     * si une liste est supprimée, son identifiant reste ici mais ne correspond plus à rien, et la liste
     * disparaît simplement du partage.
     */
    @Convert(converter = LongSetConverter.class)
    @Column(name = "list_ids", nullable = false, length = 2000)
    private Set<Long> listIds = new LinkedHashSet<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
