package com.stars.bigbang.repository;

import com.stars.bigbang.entity.GamesList;
import com.stars.bigbang.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface GamesListRepository extends JpaRepository<GamesList,Long> {

    @Query("SELECT COALESCE(MAX(g.position), 1) FROM GamesList g")
    Long findMaxOrder();

    @Query("""
            SELECT DISTINCT gl FROM GamesList gl
            LEFT JOIN FETCH gl.games entry
            LEFT JOIN FETCH entry.game
            WHERE gl.position IS NOT NULL AND gl.user = :user
            ORDER BY gl.position
            """)
    List<GamesList> findAllWithGamesOrderByPositionAndUser(@Param("user") User user);

    Optional<GamesList> findByIdAndUser(Long id, User user);

    @Transactional
    @Modifying
    @Query("update GamesList g set g.name = :name where g.id = :id")
    int updateNameById(@Param("id") long id, @Param("name") String name);
}
