package com.stars.bigbang.repository;

import com.stars.bigbang.entity.GamesListEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GamesListEntryRepository extends JpaRepository<GamesListEntry, Long> {

    List<GamesListEntry> findByGamesListId(Long gamesListId);

    @Modifying(clearAutomatically = true)
    @Query(value = "DELETE FROM games_list_entry WHERE games_list_id = :gamesListId AND game_id IN :gameIds", nativeQuery = true)
    void deleteGameByGamesListId(@Param("gamesListId") long gamesListId, @Param("gameIds") List<Long> gameIds);
}
