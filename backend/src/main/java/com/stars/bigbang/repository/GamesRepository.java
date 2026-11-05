package com.stars.bigbang.repository;

import com.stars.bigbang.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GamesRepository extends JpaRepository<Game, Long> {

    Optional<Game> findByRawgId(Integer rawgId);

    Optional<Game> findByIgdbId(Integer igdbId);

    /** La colonne slug n'est pas unique : on prend le plus ancien pour ne jamais échouer sur plusieurs résultats. */
    Optional<Game> findFirstBySlugOrderByIdAsc(String slug);

}
