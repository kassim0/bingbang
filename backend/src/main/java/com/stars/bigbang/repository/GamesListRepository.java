package com.stars.bigbang.repository;

import com.stars.bigbang.entity.GamesList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface GamesListRepository extends JpaRepository<GamesList,Long> {

    @Query("SELECT COALESCE(MAX(g.position), 1) FROM GamesList g")
    Long findMaxOrder();

    @Query("""
            SELECT DISTINCT gl FROM GamesList gl
            LEFT JOIN FETCH gl.games entry
            LEFT JOIN FETCH entry.game
            WHERE gl.position IS NOT NULL
            ORDER BY gl.position
            """)
    List<GamesList> findAllWithGamesOrderByPosition();

    @Transactional
    @Modifying
    @Query("update GamesList g set g.name = ?1 where g.id = ?2")
    int updateNameById(long id, String name);
}
