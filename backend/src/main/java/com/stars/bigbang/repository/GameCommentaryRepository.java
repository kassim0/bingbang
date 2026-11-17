package com.stars.bigbang.repository;

import com.stars.bigbang.entity.GameCommentary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface GameCommentaryRepository extends JpaRepository<GameCommentary, Long> {

    @Query("SELECT c FROM GameCommentary c WHERE c.gamesList.id = :listId AND c.game.id = :gameId")
    Optional<GameCommentary> findByListAndGame(@Param("listId") long listId, @Param("gameId") long gameId);

    /** Tous les commentaires de plusieurs listes en une seule requête (pour l'affichage de la home). */
    @Query("SELECT c FROM GameCommentary c WHERE c.gamesList.id IN :listIds")
    List<GameCommentary> findAllByListIds(@Param("listIds") Collection<Long> listIds);
}
