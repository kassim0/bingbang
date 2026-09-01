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

    @Query("SELECT COALESCE(MAX(g.position), 0) FROM GamesList g")
    Long findMaxOrder();

    @Modifying(clearAutomatically = true)
    @Query(value = "DELETE FROM games_list_games WHERE games_list_id = :gamesListId AND game_id IN :gameIds", nativeQuery = true)
    void deleteGame(@Param("gamesListId") long gamesListId, @Param("gameIds") List<Long> gameIds);

    @Transactional
    @Modifying
    @Query("update GamesList g set g.name = ?1 where g.id = ?2")
    int updateNameById(long id, String name);

    @Modifying(clearAutomatically = true)
    @Query(value = "INSERT INTO games_list_games (games_list_id,game_id) SELECT :gamesListId, unnest(:gameIds) ", nativeQuery = true)
    void saveGame(@Param("gamesListId") long gamesListId, @Param("gameIds") Long[] gameIds);


}
