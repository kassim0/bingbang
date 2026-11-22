package com.stars.bigbang.repository;

import com.stars.bigbang.entity.GamesList;
import com.stars.bigbang.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface GamesListRepository extends JpaRepository<GamesList, Long> {

    @Query("SELECT COALESCE(MAX(gl.position), 0) FROM GamesList gl WHERE gl.user.id = :userId")
    int findMaxOrder(Long userId);

    @Query("""
            SELECT DISTINCT gl FROM GamesList gl
            LEFT JOIN FETCH gl.games entry
            LEFT JOIN FETCH entry.game
            WHERE gl.user.id = :userId
            ORDER BY gl.position
            """)
    List<GamesList> findAllByUserOrderByPosition(Long userId);

    @Transactional
    @Modifying
    @Query("update GamesList gl set gl.name = :name where gl.id = :id")
    int updateNameById(long id, String name);

    /**
     * Rattache toutes les listes de {@code from} à {@code to}, en décalant leurs positions de {@code offset}
     * pour les placer à la suite de celles de {@code to}.
     * Doit être appelée dans une transaction (portée par le service).
     * @return le nombre de listes transférées
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update GamesList gl set gl.user = :to, gl.position = gl.position + :offset where gl.user = :from")
    int transferAllGamesLists(User from, User to, int offset);

    int deleteGamesListByIdAndUserId(long id, long userId);

    boolean existsByIdAndUserId(long id, long userId);
}
