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

    @Query("SELECT COALESCE(MAX(gl.position), 0) FROM GamesList gl WHERE gl.user.id = :userId")
    Long findMaxOrder(Long userId);

    @Query("""
            SELECT DISTINCT gl FROM GamesList gl
            LEFT JOIN FETCH gl.games entry
            LEFT JOIN FETCH entry.game
            WHERE gl.user.id = :userId
            AND gl.position IS NOT NULL
            ORDER BY gl.position
            """)
    List<GamesList> findAllByUserOrderByPosition(Long userId);

    @Transactional
    @Modifying
    @Query("update GamesList g set g.name = :name where g.id = :id")
    int updateNameById(@Param("id") long id, @Param("name") String name);
}
